package com.soundspace.roomservice.infrastructure.repository;

import com.soundspace.roomservice.domain.model.playback.PlaybackState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlaybackStateRepository extends JpaRepository<PlaybackState, Long> {
    Optional<PlaybackState> findByRoomId(Long roomId);
}