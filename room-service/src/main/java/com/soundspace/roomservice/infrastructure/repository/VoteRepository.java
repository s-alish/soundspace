package com.soundspace.roomservice.infrastructure.repository;

import com.soundspace.roomservice.domain.model.vote.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    List<Vote> findByRoomId(Long roomId);
}