package com.soundspace.roomservice.api.dto;

import lombok.Data;

@Data
public class TrackDTO {
    private Long id;
    private String name;
    private String artist;
    private Integer duration;
}
