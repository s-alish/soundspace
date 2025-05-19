package com.soundspace.playlistservice.infrastructure.repository.queue;


import com.soundspace.playlistservice.domain.model.queue.QueueTrack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QueueTrackRepository extends JpaRepository<QueueTrack, Long> {
    List<QueueTrack> findByQueueIdOrderByPositionAsc(Long queueId);
    void deleteByQueueId(Long queueId);
}