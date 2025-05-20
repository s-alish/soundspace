package com.soundspace.playlistservice.application.service;


import com.soundspace.playlistservice.api.dto.VoteProposalMessage;
import com.soundspace.playlistservice.api.dto.VoteResultMessage;
import com.soundspace.playlistservice.domain.model.playlist.Playlist;
import com.soundspace.playlistservice.domain.model.playlist.PlaylistTrack;
import com.soundspace.playlistservice.domain.model.playlist.Track;
import com.soundspace.playlistservice.domain.model.queue.Queue;
import com.soundspace.playlistservice.domain.model.queue.QueueTrack;
import com.soundspace.playlistservice.domain.model.vote.Vote;
import com.soundspace.playlistservice.domain.model.vote.VoteProposal;
import com.soundspace.playlistservice.infrastructure.repository.PlaylistRepository;
import com.soundspace.playlistservice.infrastructure.repository.PlaylistTrackRepository;
import com.soundspace.playlistservice.infrastructure.repository.TrackRepository;
import com.soundspace.playlistservice.infrastructure.repository.queue.QueueRepository;
import com.soundspace.playlistservice.infrastructure.repository.queue.QueueTrackRepository;
import com.soundspace.playlistservice.infrastructure.repository.vote.VoteProposalRepository;
import com.soundspace.playlistservice.infrastructure.repository.vote.VoteRepository;
import com.soundspace.playlistservice.infrastructure.client.RoomServiceClient;
import com.soundspace.playlistservice.infrastructure.client.UserServiceClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistService.class);

    private final PlaylistRepository playlistRepository;
    private final TrackRepository trackRepository;
    private final PlaylistTrackRepository playlistTrackRepository;
    private final QueueRepository queueRepository;
    private final QueueTrackRepository queueTrackRepository;
    private final VoteProposalRepository voteProposalRepository;
    private final VoteRepository voteRepository;
    private final RoomServiceClient roomServiceClient;
    private final UserServiceClient userServiceClient;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public Track addTrackToPlaylist(Long roomId, Long userId, Track track) {
        logger.info("Adding track to playlist: roomId={}, userId={}, trackTitle={}", roomId, userId, track.getTitle());
        validateUserAndRoom(userId, roomId);

        Playlist playlist = playlistRepository.findByRoomId(roomId)
                .orElseGet(() -> {
                    Playlist newPlaylist = new Playlist();
                    newPlaylist.setRoomId(roomId);
                    return playlistRepository.save(newPlaylist);
                });

        Track savedTrack = trackRepository.save(track);
        PlaylistTrack playlistTrack = new PlaylistTrack();
        playlistTrack.setPlaylist(playlist);
        playlistTrack.setTrack(savedTrack);
        playlistTrackRepository.save(playlistTrack);
        return savedTrack;
    }

    @Transactional(readOnly = true)
    public List<Track> getTracksForRoom(Long roomId, Long userId) {
        logger.info("Fetching tracks for room {} by user {}", roomId, userId);
        validateUserAndRoom(userId, roomId);

        Playlist playlist = playlistRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Playlist not found for room"));
        return playlistTrackRepository.findByPlaylistId(playlist.getId()).stream()
                .map(PlaylistTrack::getTrack)
                .collect(Collectors.toList());
    }

    @Transactional
    public Track addTrackToQueue(Long roomId, Long userId, Long trackId) {
        logger.info("Adding track to queue: roomId={}, userId={}, trackId={}", roomId, userId, trackId);
        validateUserAndRoom(userId, roomId);

        // Validate that the track exists in the room's playlist
        Playlist playlist = playlistRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Playlist not found for room"));
        Track track = playlistTrackRepository.findByPlaylistId(playlist.getId()).stream()
                .map(PlaylistTrack::getTrack)
                .filter(t -> t.getId().equals(trackId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Track with ID " + trackId + " not found in playlist for room " + roomId));

        Queue queue = queueRepository.findByRoomId(roomId)
                .orElseGet(() -> {
                    Queue newQueue = new Queue();
                    newQueue.setRoomId(roomId);
                    return queueRepository.save(newQueue);
                });

        List<QueueTrack> existingTracks = queueTrackRepository.findByQueueIdOrderByPositionAsc(queue.getId());
        int nextPosition = existingTracks.isEmpty() ? 0 : existingTracks.get(existingTracks.size() - 1).getPosition() + 1;

        QueueTrack queueTrack = new QueueTrack();
        queueTrack.setQueue(queue);
        queueTrack.setTrack(track);
        queueTrack.setPosition(nextPosition);
        queueTrackRepository.save(queueTrack);
        return track;
    }

    @Transactional
    public void shuffleQueue(Long roomId, Long userId) {
        logger.info("Shuffling queue for room {} by user {}", roomId, userId);
        validateUserAndRoom(userId, roomId);

        Queue queue = queueRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Queue not found for room"));

        List<QueueTrack> queueTracks = queueTrackRepository.findByQueueIdOrderByPositionAsc(queue.getId());
        if (queueTracks.isEmpty()) {
            logger.warn("Queue is empty for room {}", roomId);
            return;
        }

        Collections.shuffle(queueTracks);
        for (int i = 0; i < queueTracks.size(); i++) {
            queueTracks.get(i).setPosition(i);
        }
        queueTrackRepository.saveAll(queueTracks);
        logger.debug("Shuffled {} tracks in queue for room {}", queueTracks.size(), roomId);
    }

    @Transactional(readOnly = true)
    public List<Track> getQueue(Long roomId, Long userId) {
        logger.info("Fetching queue for room {} by user {}", roomId, userId);
        validateUserAndRoom(userId, roomId);

        Queue queue = queueRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Queue not found for room"));

        return queueTrackRepository.findByQueueIdOrderByPositionAsc(queue.getId()).stream()
                .map(QueueTrack::getTrack)
                .collect(Collectors.toList());
    }

    @Transactional
    public void proposeTrack(Long roomId, Long userId, Long trackId) {
        logger.info("Proposing track for room {} by user {}, trackId {}", roomId, userId, trackId);
        validateUserAndRoom(userId, roomId);

        if (voteProposalRepository.findByRoomIdAndStatus(roomId, VoteProposal.Status.PENDING).isPresent()) {
            logger.warn("Active proposal already exists for room {}", roomId);
            throw new RuntimeException("Active vote proposal already exists");
        }

        List<Track> availableTracks = getAvailableTracks(roomId);
        Track proposedTrack = availableTracks.stream()
                .filter(t -> t.getId().equals(trackId))
                .findFirst()
                .orElseThrow(() -> {
                    logger.warn("Track {} is not available for proposal in room {}", trackId, roomId);
                    return new RuntimeException("Track not available for proposal");
                });

        VoteProposal proposal = new VoteProposal();
        proposal.setRoomId(roomId);
        proposal.setTrack(proposedTrack);
        proposal.setStatus(VoteProposal.Status.PENDING);
        proposal.setProposedAt(LocalDateTime.now());
        voteProposalRepository.save(proposal);

        messagingTemplate.convertAndSend(
                "/room/" + roomId + "/vote",
                new VoteProposalMessage(proposedTrack.getId(), proposedTrack.getTitle(), proposedTrack.getArtist(), proposal.getId())
        );
        logger.info("Proposed track {} for room {}", proposedTrack.getId(), roomId);
    }

    @Transactional
    public void vote(Long roomId, Long userId, Long proposalId, Vote.VoteType voteType) {
        logger.info("Recording vote for room {}, user {}, proposal {}, type {}", roomId, userId, proposalId, voteType);
        validateUserAndRoom(userId, roomId);

        VoteProposal proposal = voteProposalRepository.findById(proposalId)
                .filter(p -> p.getRoomId().equals(roomId) && p.getStatus() == VoteProposal.Status.PENDING)
                .orElseThrow(() -> {
                    logger.warn("Invalid or inactive proposal {} for room {}", proposalId, roomId);
                    return new RuntimeException("Invalid or inactive proposal");
                });

        if (voteRepository.findByProposalIdAndUserId(proposalId, userId) != null) {
            logger.warn("User {} has already voted for proposal {} in room {}", userId, proposalId, roomId);
            throw new RuntimeException("User has already voted for this proposal");
        }

        Vote vote = new Vote();
        vote.setProposal(proposal);
        vote.setUserId(userId);
        vote.setVoteType(voteType);
        vote.setVotedAt(LocalDateTime.now());
        voteRepository.save(vote);
        logger.debug("Vote recorded: user={}, proposal={}, type={}", userId, proposalId, voteType);
    }

    @Transactional
    @Scheduled(fixedRate = 30000)
    public void resolveProposals() {
        List<VoteProposal> pendingProposals = voteProposalRepository.findAll().stream()
                .filter(p -> p.getStatus() == VoteProposal.Status.PENDING)
                .filter(p -> p.getProposedAt().isBefore(LocalDateTime.now().minusSeconds(30)))
                .collect(Collectors.toList());

        for (VoteProposal proposal : pendingProposals) {
            List<Vote> votes = voteRepository.findByProposalId(proposal.getId());
            long playVotes = votes.stream().filter(v -> v.getVoteType() == Vote.VoteType.PLAY).count();
            long skipVotes = votes.stream().filter(v -> v.getVoteType() == Vote.VoteType.SKIP).count();

            if (playVotes >= skipVotes) {
                proposal.setStatus(VoteProposal.Status.ACCEPTED);
                addTrackToQueue(proposal.getRoomId(), null, proposal.getTrack().getId());
                logger.info("Proposal {} accepted for room {}, track {} added to queue", proposal.getId(), proposal.getRoomId(), proposal.getTrack().getId());
                messagingTemplate.convertAndSend(
                        "/room/" + proposal.getRoomId() + "/vote-result",
                        new VoteResultMessage(proposal.getTrack().getId(), proposal.getTrack().getTitle(), true)
                );
            } else {
                proposal.setStatus(VoteProposal.Status.REJECTED);
                logger.info("Proposal {} rejected for room {}, proposing new track", proposal.getId(), proposal.getRoomId());
                messagingTemplate.convertAndSend(
                        "/room/" + proposal.getRoomId() + "/vote-result",
                        new VoteResultMessage(proposal.getTrack().getId(), proposal.getTrack().getTitle(), false)
                );
                proposeTrack(proposal.getRoomId(), null, null);
            }
            voteProposalRepository.save(proposal);
        }
    }

    private List<Track> getAvailableTracks(Long roomId) {
        Playlist playlist = playlistRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Playlist not found for room"));

        List<Long> queuedTrackIds = queueTrackRepository.findByQueueIdOrderByPositionAsc(
                queueRepository.findByRoomId(roomId).map(Queue::getId).orElse(0L)
        ).stream().map(qt -> qt.getTrack().getId()).collect(Collectors.toList());

        List<Long> votedTrackIds = voteProposalRepository.findVotedTrackIdsByRoomId(roomId);

        RoomServiceClient.PlaybackStateDTO playbackState = roomServiceClient.getPlaybackState(roomId);
        Long currentTrackId = playbackState != null ? playbackState.getTrackId() : null;

        return playlistTrackRepository.findByPlaylistId(playlist.getId()).stream()
                .map(PlaylistTrack::getTrack)
                .filter(t -> !queuedTrackIds.contains(t.getId()))
                .filter(t -> !votedTrackIds.contains(t.getId()))
                .filter(t -> currentTrackId == null || !t.getId().equals(currentTrackId))
                .collect(Collectors.toList());
    }

    private void validateUserAndRoom(Long userId, Long roomId) {
        if (userId != null) {
            try {
                userServiceClient.getUserById(userId);
            } catch (Exception e) {
                logger.error("Failed to validate user {}: {}", userId, e.getMessage());
                throw new RuntimeException("User not found");
            }
        }
        try {
            roomServiceClient.getRoom(roomId);
        } catch (Exception e) {
            logger.error("Failed to validate room {}: {}", roomId, e.getMessage());
            throw new RuntimeException("Room not found");
        }
    }
}