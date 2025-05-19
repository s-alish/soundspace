package com.soundspace.playlistservice.infrastructure.repository.queue;

import com.soundspace.playlistservice.domain.model.queue.Queue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QueueRepository extends JpaRepository<Queue, Long> {
    Optional<Queue> findByRoomId(Long roomId);
}
