package com.soundspace.playlistservice.api.controller;

import com.soundspace.playlistservice.application.service.PlaylistService;
import com.soundspace.playlistservice.domain.model.vote.Vote;
import com.soundspace.playlistservice.infrastructure.client.UserServiceClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rooms/{roomId}")
@RequiredArgsConstructor
public class VoteController {

    private static final Logger logger = LoggerFactory.getLogger(VoteController.class);
    private final PlaylistService playlistService;
    private final UserServiceClient userServiceClient;

    @PostMapping("/propose")
    @Operation(summary = "Propose a track for voting", description = "Proposes a random track for voting.")
    @ApiResponse(responseCode = "200", description = "Track proposed successfully")
    public ResponseEntity<Void> proposeTrack(@PathVariable Long roomId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = getUserIdByEmail(email);
        logger.info("Request to propose track for room {} by user {}", roomId, userId);
        playlistService.proposeTrack(roomId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/vote")
    @Operation(summary = "Vote on a proposed track", description = "Allows a user to vote 'PLAY' or 'SKIP' on a proposed track.")
    @ApiResponse(responseCode = "200", description = "Vote recorded successfully")
    public ResponseEntity<Void> vote(
            @PathVariable Long roomId,
            @RequestBody VoteRequest voteRequest) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = getUserIdByEmail(email);
        logger.info("Request to vote for room {} by user {}, proposal {}, type {}",
                roomId, userId, voteRequest.getProposalId(), voteRequest.getVoteType());
        playlistService.vote(roomId, userId, voteRequest.getProposalId(), voteRequest.getVoteType());
        return ResponseEntity.ok().build();
    }

    static class VoteRequest {
        private Long proposalId;
        private Vote.VoteType voteType;

        public Long getProposalId() { return proposalId; }
        public void setProposalId(Long proposalId) { this.proposalId = proposalId; }
        public Vote.VoteType getVoteType() { return voteType; }
        public void setVoteType(Vote.VoteType voteType) { this.voteType = voteType; }
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