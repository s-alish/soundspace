package com.soundspace.playlistservice.infrastructure.repository;

import com.soundspace.playlistservice.domain.model.Playlist;
import com.soundspace.playlistservice.domain.model.PlaylistTrack;
import com.soundspace.playlistservice.domain.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, Long> {
    boolean existsByPlaylistAndTrack(Playlist playlist, Track track);
    List<PlaylistTrack> findByPlaylist(Playlist playlist);
}