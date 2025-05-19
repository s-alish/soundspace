package com.soundspace.playlistservice.api.dto;

import com.soundspace.playlistservice.domain.model.playlist.Track;

public class TrackMapper {

    public static Track toEntity(TrackDTO dto) {
        if (dto == null) {
            return null;
        }

        Track track = new Track();
        track.setId(dto.getId());
        track.setTitle(dto.getTitle());
        track.setArtist(dto.getArtist());
        track.setDuration(dto.getDuration());
        return track;
    }

    public static TrackDTO toDTO(Track entity) {
        if (entity == null) {
            return null;
        }

        TrackDTO dto = new TrackDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setArtist(entity.getArtist());
        dto.setDuration(entity.getDuration());
        return dto;
    }
}
