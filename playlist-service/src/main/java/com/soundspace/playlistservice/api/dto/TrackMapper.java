package com.soundspace.playlistservice.api.dto;

import com.soundspace.playlistservice.domain.model.playlist.Track;

public class TrackMapper {

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
