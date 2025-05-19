package com.soundspace.playlistservice.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VoteProposalMessage {
    private Long trackId;
    private String title;
    private String artist;
    private Long proposalId;
}
