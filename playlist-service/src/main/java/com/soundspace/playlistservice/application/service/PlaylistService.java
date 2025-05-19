package com.soundspace.playlistservice.application.service;

import com.soundspace.playlistservice.api.dto.TrackDTO;
import com.soundspace.playlistservice.domain.model.Playlist;
import com.soundspace.playlistservice.domain.model.PlaylistTrack;
import com.soundspace.playlistservice.domain.model.Track;
import com.soundspace.playlistservice.infrastructure.repository.PlaylistRepository;
import com.soundspace.playlistservice.infrastructure.repository.PlaylistTrackRepository;
import com.soundspace.playlistservice.infrastructure.repository.TrackRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final TrackRepository trackRepository;
    private final PlaylistTrackRepository playlistTrackRepository;

    public TrackDTO addTrackToPlaylist(Long roomId, TrackDTO trackDTO) {
        // Find or create Playlist for the given room
        Playlist playlist = playlistRepository.findByRoomId(roomId)
                .orElseGet(() -> playlistRepository.save(new Playlist(roomId)));

        // Check if track already exists to avoid duplicates (based on title + artist)
        Track track = trackRepository.findByTitleAndArtist(trackDTO.getTitle(), trackDTO.getArtist())
                .orElseGet(() -> {
                    Track newTrack = new Track(trackDTO.getTitle(), trackDTO.getArtist(), trackDTO.getDuration());
                    return trackRepository.save(newTrack);
                });

        // Prevent duplicate association between Playlist and Track
        boolean alreadyExists = playlistTrackRepository.existsByPlaylistAndTrack(playlist, track);
        if (!alreadyExists) {
            PlaylistTrack playlistTrack = new PlaylistTrack(playlist, track);
            playlistTrackRepository.save(playlistTrack);
        }

        // Return DTO
        return toDto(track);
    }

    public List<TrackDTO> getTracksFromPlaylist(Long roomId) {
        return playlistRepository.findByRoomId(roomId)
                .map(playlist ->
                        playlistTrackRepository.findByPlaylist(playlist).stream()
                                .map(playlistTrack -> toDto(playlistTrack.getTrack()))
                                .toList()
                )
                .orElse(Collections.emptyList());
    }

    private TrackDTO toDto(Track track) {
        TrackDTO dto = new TrackDTO();
        dto.setId(track.getId());
        dto.setTitle(track.getTitle());
        dto.setArtist(track.getArtist());
        dto.setDuration(track.getDuration());
        return dto;
    }
}

