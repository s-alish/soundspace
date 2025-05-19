package com.soundspace.roomservice.api.controller;

import com.soundspace.roomservice.api.dto.RoomDTO;
import com.soundspace.roomservice.api.dto.TrackDTO;
import com.soundspace.roomservice.api.dto.VoteDTO;
import com.soundspace.roomservice.application.service.RoomService;
import com.soundspace.roomservice.domain.model.room.Room;
import com.soundspace.roomservice.domain.model.vote.Vote;
import com.soundspace.roomservice.domain.model.playback.PlaybackState;
import com.soundspace.roomservice.infrastructure.client.PlaylistServiceClient;
import com.soundspace.roomservice.infrastructure.client.UserServiceClient;
import com.soundspace.roomservice.infrastructure.component.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rooms")
@Tag(name = "Room Management", description = "APIs for managing rooms")
public class RoomController {

    private static final Logger logger = LoggerFactory.getLogger(RoomController.class);
    private final RoomService roomService;
    private final JwtUtil jwtUtil;
    private final UserServiceClient userServiceClient;
    private final PlaylistServiceClient playlistServiceClient;


    @Autowired
    public RoomController(RoomService roomService, JwtUtil jwtUtil, UserServiceClient userServiceClient, PlaylistServiceClient playlistServiceClient) {
        this.roomService = roomService;
        this.jwtUtil = jwtUtil;
        this.userServiceClient = userServiceClient;
        this.playlistServiceClient = playlistServiceClient;
    }

    @PostMapping
    @Operation(summary = "Create a room", description = "Creates a new room")
    @ApiResponse(responseCode = "201", description = "Room created successfully")
    public ResponseEntity<Room> createRoom(@Valid @RequestBody RoomDTO roomDTO,
                                           @RequestHeader("Authorization") String authHeader) {
        logger.info("Received POST /rooms request with room name: {}", roomDTO.getName());
        logger.debug("Authorization header: {}", authHeader);
        String token = authHeader.substring(7);
        logger.debug("Extracted token: {}", token);
        String email = jwtUtil.getEmailFromToken(token);
        logger.info("Extracted email from token: {}", email);
        Long userId = getUserIdByEmail(email);
        logger.info("Fetched userId: {} for email: {}", userId, email);
        Room room = roomService.createRoom(roomDTO.getName(), userId);
        logger.info("Room created successfully with id: {}", room.getId());
        return new ResponseEntity<>(room, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get room details", description = "Retrieves room details by ID")
    @ApiResponse(responseCode = "200", description = "Room details retrieved")
    public ResponseEntity<Room> getRoom(@PathVariable Long id) {
        Room room = roomService.getRoom(id);
        return ResponseEntity.ok(room);
    }

    @PostMapping("/{id}/join")
    @Operation(summary = "Join a room", description = "Allows a user to join a room")
    @ApiResponse(responseCode = "200", description = "User joined room")
    public ResponseEntity<Void> joinRoom(@PathVariable Long id,
                                         @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtUtil.getEmailFromToken(token);
        Long userId = getUserIdByEmail(email);
        roomService.joinRoom(id, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/votes")
    @Operation(summary = "Vote for a track", description = "Allows a user to vote for a track")
    @ApiResponse(responseCode = "200", description = "Vote recorded")
    public ResponseEntity<Vote> vote(@PathVariable Long id,
                                     @Valid @RequestBody VoteDTO voteDTO,
                                     @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtUtil.getEmailFromToken(token);
        Long userId = getUserIdByEmail(email);
        Vote vote = roomService.vote(id, userId, voteDTO.getTrackId());
        return ResponseEntity.ok(vote);
    }

    @GetMapping("/{id}/state")
    @Operation(summary = "Get playback state", description = "Retrieves the current playback state")
    @ApiResponse(responseCode = "200", description = "Playback state retrieved")
    public ResponseEntity<PlaybackState> getPlaybackState(@PathVariable Long id) {
        PlaybackState state = roomService.getPlaybackState(id);
        return ResponseEntity.ok(state);
    }

    @GetMapping("/{id}/users")
    @Operation(summary = "Get users in room", description = "Retrieves list of users in a room")
    @ApiResponse(responseCode = "200", description = "List of users retrieved")
    public ResponseEntity<List<UserServiceClient.UserDTO>> getUsersInRoom(@PathVariable Long id) {
        List<UserServiceClient.UserDTO> users = roomService.getUsersInRoom(id);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}/playlist")
    @Operation(summary = "Get playlist for room", description = "Retrieves the playlist tracks for a room")
    @ApiResponse(responseCode = "200", description = "Playlist retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Room not found")
    public ResponseEntity<List<PlaylistServiceClient.TrackDTO>> getPlaylist(@PathVariable Long id,
                                                                            @RequestHeader("Authorization") String authHeader) {
        logger.info("Received GET /rooms/{}/playlist request", id);
        logger.debug("Authorization header: {}", authHeader);
        String token = authHeader.substring(7);
        String email = jwtUtil.getEmailFromToken(token);
        logger.info("Extracted email from token: {}", email);
        Long userId = getUserIdByEmail(email);
        logger.info("Fetched userId: {} for email: {}", userId, email);

        // Validate room existence
        roomService.getRoom(id);
        List<PlaylistServiceClient.TrackDTO> tracks = playlistServiceClient.getTracksByRoomId(id);
        logger.debug("Retrieved {} tracks for room {}", tracks.size(), id);
        return ResponseEntity.ok(tracks);
    }


    @PostMapping("/{id}/play")
    @Operation(summary = "Play track", description = "Starts playback of a track in the room (admin only)")
    @ApiResponse(responseCode = "200", description = "Playback started")
    public ResponseEntity<PlaybackState> play(@PathVariable Long id,
                                              @RequestBody Map<String, Long> body,
                                              @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtUtil.getEmailFromToken(token);
        Long userId = getUserIdByEmail(email);
        PlaybackState state = roomService.play(id, body.get("trackId"), userId);
        return ResponseEntity.ok(state);
    }

    @PostMapping("/{id}/pause")
    @Operation(summary = "Pause playback", description = "Pauses playback in the room (admin only)")
    @ApiResponse(responseCode = "200", description = "Playback paused")
    public ResponseEntity<PlaybackState> pause(@PathVariable Long id,
                                               @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtUtil.getEmailFromToken(token);
        Long userId = getUserIdByEmail(email);
        PlaybackState state = roomService.pause(id, userId);
        return ResponseEntity.ok(state);
    }

    private Long getUserIdByEmail(String email) {
        logger.debug("Fetching user by email: {}", email);
        UserServiceClient.UserDTO user = userServiceClient.getUserByEmail(email);
        if (user == null || user.getId() == null) {
            logger.error("User not found for email: {}", email);
            throw new RuntimeException("User not found");
        }
        logger.info("Fetched user: id={}, email={}, name={}", user.getId(), user.getEmail(), user.getName());
        return user.getId();
    }


}