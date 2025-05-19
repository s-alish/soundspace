package com.soundspace.playlistservice.infrastructure.repository.vote;

import com.soundspace.playlistservice.domain.model.vote.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    List<Vote> findByProposalId(Long proposalId);
}
