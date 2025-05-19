package com.soundspace.playlistservice.infrastructure.repository;

import com.soundspace.playlistservice.domain.model.playlist.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    Optional<Playlist> findByRoomId(Long roomId);
}
