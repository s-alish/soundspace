package com.soundspace.playlistservice.api.dto;

import lombok.Data;
import lombok.Getter;

@Data
public class TrackDTO {
    private Long id;
    private String title;
    private String artist;
    private Integer duration;
}
