package com.soundspace.playlistservice.api.controller;

import com.soundspace.playlistservice.api.dto.QueueTrackRequestDTO;
import com.soundspace.playlistservice.api.dto.TrackDTO;
import com.soundspace.playlistservice.api.dto.TrackMapper;
import com.soundspace.playlistservice.application.service.PlaylistService;
import com.soundspace.playlistservice.domain.model.playlist.Track;
import com.soundspace.playlistservice.infrastructure.client.UserServiceClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rooms/{roomId}")
@RequiredArgsConstructor
public class PlaylistController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistController.class);
    private final PlaylistService playlistService;
    private final UserServiceClient userServiceClient;

    @PostMapping("/tracks")
    @Operation(summary = "Add a track to a room's playlist", description = "Adds a new track to the playlist.")
    @ApiResponse(responseCode = "200", description = "Track added successfully")
    public ResponseEntity<TrackDTO> addTrackToPlaylist(
            @PathVariable Long roomId,
            @RequestBody TrackDTO trackDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = getUserIdByEmail(email);
        logger.info("Request to add track to playlist for room {} by user {}", roomId, userId);

        Track track = TrackMapper.toEntity(trackDTO);
        Track savedTrack = playlistService.addTrackToPlaylist(roomId, userId, track);
        return ResponseEntity.ok(TrackMapper.toDTO(savedTrack));
    }

    @GetMapping("/tracks")
    @Operation(summary = "Get tracks in a room's playlist", description = "Retrieves all tracks in the playlist.")
    @ApiResponse(responseCode = "200", description = "Tracks retrieved successfully")
    public ResponseEntity<List<TrackDTO>> getTracksForRoom(@PathVariable Long roomId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = getUserIdByEmail(email);
        logger.info("Request to get playlist tracks for room {} by user {}", roomId, userId);

        List<Track> tracks = playlistService.getTracksForRoom(roomId, userId);
        List<TrackDTO> trackDTOs = tracks.stream()
                .map(TrackMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(trackDTOs);
    }

    @PostMapping("/queue")
    @Operation(summary = "Add a track to a room's queue", description = "Adds an existing playlist track to the queue.")
    @ApiResponse(responseCode = "200", description = "Track added successfully")
    public ResponseEntity<TrackDTO> addTrackToQueue(
            @PathVariable Long roomId,
            @RequestBody QueueTrackRequestDTO requestDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = getUserIdByEmail(email);
        logger.info("Request to add track to queue for room {} by user {}, trackId {}", roomId, userId, requestDTO.getTrackId());

        Track savedTrack = playlistService.addTrackToQueue(roomId, userId, requestDTO.getTrackId());
        return ResponseEntity.ok(TrackMapper.toDTO(savedTrack));
    }

    @PostMapping("/queue/shuffle")
    @Operation(summary = "Shuffle a room's queue", description = "Shuffles the tracks in the queue.")
    @ApiResponse(responseCode = "200", description = "Queue shuffled successfully")
    public ResponseEntity<Void> shuffleQueue(@PathVariable Long roomId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = getUserIdByEmail(email);
        logger.info("Request to shuffle queue for room {} by user {}", roomId, userId);

        playlistService.shuffleQueue(roomId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/queue")
    @Operation(summary = "Get a room's queue", description = "Retrieves all tracks in the queue.")
    @ApiResponse(responseCode = "200", description = "Queue retrieved successfully")
    public ResponseEntity<List<TrackDTO>> getQueue(@PathVariable Long roomId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = getUserIdByEmail(email);
        logger.info("Request to get queue for room {} by user {}", roomId, userId);

        List<Track> tracks = playlistService.getQueue(roomId, userId);
        List<TrackDTO> trackDTOs = tracks.stream()
                .map(TrackMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(trackDTOs);
    }

    private Long getUserIdByEmail(String email) {
        try {
            UserServiceClient.UserDTO user = userServiceClient.getUserByEmail(email);
            if (user == null || user.getId() == null) {
                logger.error("User not found for email: {}", email);
                throw new RuntimeException("User not found");
            }
            return user.getId();
        } catch (Exception e) {
            logger.error("Failed to fetch user ID for email {}: {}", email, e.getMessage());
            throw new RuntimeException("Unable to resolve user ID", e);
        }
    }
}