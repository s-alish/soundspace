package com.soundspace.playlistservice.infrastructure.repository;

import com.soundspace.playlistservice.domain.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrackRepository extends JpaRepository<Track, Long> {
    Optional<Track> findByTitleAndArtist(String title, String artist);

}