package com.soundspace.playlistservice.api.controller;

import com.soundspace.playlistservice.api.dto.TrackDTO;
import com.soundspace.playlistservice.application.service.PlaylistService;
import com.soundspace.playlistservice.domain.model.Track;
import com.soundspace.playlistservice.infrastructure.client.RoomServiceClient;
import com.soundspace.playlistservice.infrastructure.client.UserServiceClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/playlists")
@RequiredArgsConstructor
@Validated
public class PlaylistController {

    private final PlaylistService playlistService;
    private final RoomServiceClient roomServiceClient;
    private final UserServiceClient userServiceClient;

    @PostMapping("/{id}/tracks")
    @Operation(summary = "Add a track to a room's playlist", description = "Adds a new track to the playlist associated with the specified room.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Track added successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Room or user not found")
    })
    public ResponseEntity<TrackDTO> addTrackToPlaylist(
            @PathVariable Long id,
            @Valid @RequestBody TrackDTO trackDTO) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserServiceClient.UserDTO user = userServiceClient.getUserByEmail(email);
        Long userId = user.getId();

        RoomServiceClient.RoomDTO room = roomServiceClient.getRoom(id);
        if (!Objects.equals(room.getAdminId(), userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        TrackDTO addedTrack = playlistService.addTrackToPlaylist(id, trackDTO);
        return ResponseEntity.ok(addedTrack);
    }

    @GetMapping("/{id}/tracks")
    @Operation(summary = "Get tracks in a room's playlist", description = "Retrieves all tracks in the playlist associated with the specified room.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tracks retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Room or playlist not found")
    })
    public ResponseEntity<List<TrackDTO>> getTracksFromPlaylist(@PathVariable Long id) {
        List<TrackDTO> tracks = playlistService.getTracksFromPlaylist(id);
        return ResponseEntity.ok(tracks);
    }

    public static Track toEntity(TrackDTO dto) {
        Track track = new Track();
        track.setTitle(dto.getTitle());
        track.setArtist(dto.getArtist());
        track.setDuration(dto.getDuration());
        return track;
    }

    public static TrackDTO toDTO(Track track) {
        TrackDTO dto = new TrackDTO();
        dto.setId(track.getId());
        dto.setTitle(track.getTitle());
        dto.setArtist(track.getArtist());
        dto.setDuration(track.getDuration());
        return dto;
    }
}
