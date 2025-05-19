package com.soundspace.roomservice.application.service;

import com.soundspace.roomservice.domain.exception.RoomNotFoundException;
import com.soundspace.roomservice.domain.model.playback.PlaybackState;
import com.soundspace.roomservice.domain.model.playback.PlaybackStateContext;
import com.soundspace.roomservice.domain.model.room.Room;
import com.soundspace.roomservice.domain.model.room.RoomUser;
import com.soundspace.roomservice.domain.model.vote.SimpleVoteStrategy;
import com.soundspace.roomservice.domain.model.vote.Vote;
import com.soundspace.roomservice.domain.model.vote.VoteStrategy;
import com.soundspace.roomservice.infrastructure.client.PlaylistServiceClient;
import com.soundspace.roomservice.infrastructure.client.UserServiceClient;
import com.soundspace.roomservice.infrastructure.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {
    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);
    private final RoomRepository roomRepository;
    private final RoomUserRepository roomUserRepository;
    private final VoteRepository voteRepository;
    private final PlaybackStateRepository playbackStateRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserServiceClient userServiceClient; // Поле для Feign-клиента
    private final VoteStrategy voteStrategy;
    private final PlaylistServiceClient playlistServiceClient;

    public RoomService(RoomRepository roomRepository, RoomUserRepository roomUserRepository,
                       VoteRepository voteRepository, PlaybackStateRepository playbackStateRepository,
                       SimpMessagingTemplate messagingTemplate, UserServiceClient userServiceClient, PlaylistServiceClient playlistServiceClient) {
        this.roomRepository = roomRepository;
        this.roomUserRepository = roomUserRepository;
        this.voteRepository = voteRepository;
        this.playbackStateRepository = playbackStateRepository;
        this.messagingTemplate = messagingTemplate;
        this.userServiceClient = userServiceClient;
        this.voteStrategy = new SimpleVoteStrategy();
        this.playlistServiceClient = playlistServiceClient;
    }

    public Room createRoom(String name, Long adminId) {
        logger.info("Creating room: name={}, adminId={}", name, adminId);
        Room room = new Room();
        room.setName(name);
        room.setAdminId(adminId);
        Room savedRoom = roomRepository.save(room);
        logger.info("Room saved with id: {}", savedRoom.getId());

        PlaybackState playbackState = new PlaybackState();
        playbackState.setRoomId(savedRoom.getId());
        playbackState.setState(PlaybackState.PlaybackStateEnum.PAUSED);
        playbackStateRepository.save(playbackState);
        logger.debug("Initialized playback state for room: id={}", savedRoom.getId());

        joinRoom(room.getId(), adminId);
        return savedRoom;
    }

    public Room getRoom(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
    }

    public void joinRoom(Long roomId, Long userId) {
        if (!roomRepository.existsById(roomId)) {
            throw new RuntimeException("Room not found");
        }
        if (roomUserRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new RuntimeException("User already in room");
        }

        RoomUser roomUser = new RoomUser();
        roomUser.setRoomId(roomId);
        roomUser.setUserId(userId);
        roomUserRepository.save(roomUser);
    }

    public Vote vote(Long roomId, Long userId, Long trackId) {
        if (!roomRepository.existsById(roomId)) {
            throw new RuntimeException("Room not found");
        }
        if (!roomUserRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new RuntimeException("User not in room");
        }

        Vote vote = new Vote();
        vote.setRoomId(roomId);
        vote.setUserId(userId);
        vote.setTrackId(trackId);
        Vote savedVote = voteRepository.save(vote);

        // Уведомление через WebSocket
        messagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/votes", savedVote);

        // Выбор следующего трека
        List<Vote> votes = voteRepository.findByRoomId(roomId);
        Long nextTrackId = voteStrategy.selectNextTrack(votes);
        if (nextTrackId != null) {
            updatePlaybackState(roomId, nextTrackId);
        }

        return savedVote;
    }

    private void updatePlaybackState(Long roomId, Long trackId) {
        PlaybackState playbackState = playbackStateRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Playback state not found"));
        PlaybackStateContext context = new PlaybackStateContext();
        context.play(trackId);
        playbackState.setState(context.getCurrentState());
        playbackState.setCurrentTrackId(context.getCurrentTrackId());
        playbackStateRepository.save(playbackState);

        // Уведомление через WebSocket
        messagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/state", playbackState);
    }

    public PlaybackState getPlaybackState(Long roomId) {
        return playbackStateRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));
    }

    public List<UserServiceClient.UserDTO> getUsersInRoom(Long roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new RoomNotFoundException(roomId);
        }
        List<RoomUser> roomUsers = roomUserRepository.findByRoomId(roomId);
        return roomUsers.stream()
                .map(ru -> userServiceClient.getUserById(ru.getUserId()))
                .collect(Collectors.toList());
    }

    public PlaybackState play(Long roomId, Long trackId, Long userId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));
        if (!room.getAdminId().equals(userId)) {
            throw new RuntimeException("Only admin can control playback");
        }
        PlaybackState playbackState = playbackStateRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));
        PlaybackStateContext context = new PlaybackStateContext();
        context.play(trackId);
        playbackState.setState(context.getCurrentState());
        playbackState.setCurrentTrackId(context.getCurrentTrackId());
        playbackStateRepository.save(playbackState);
        messagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/state", playbackState);
        return playbackState;
    }

    public PlaybackState pause(Long roomId, Long userId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));
        if (!room.getAdminId().equals(userId)) {
            throw new RuntimeException("Only admin can control playback");
        }
        PlaybackState playbackState = playbackStateRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));
        PlaybackStateContext context = new PlaybackStateContext();
        context.pause();
        playbackState.setState(context.getCurrentState());
        playbackState.setCurrentTrackId(null);
        playbackStateRepository.save(playbackState);
        messagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/state", playbackState);
        return playbackState;
    }

    public List<PlaylistServiceClient.TrackDTO> getTracksForRoom(Long roomId) {

        if (!roomRepository.existsById(roomId)) {
            throw new RoomNotFoundException(roomId);
        }
        return playlistServiceClient.getTracksByRoomId(roomId);
    }
}
