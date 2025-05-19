package com.soundspace.playlistservice.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VoteResultMessage {
    private Long trackId;
    private String title;
    private boolean accepted;

}
