package com.soundspace.playlistservice.infrastructure.repository.vote;

import com.soundspace.playlistservice.domain.model.vote.Vote;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    List<Vote> findByProposalId(Long proposalId);

    @Query("SELECT v FROM Vote v WHERE v.proposal.id = :proposalId AND v.userId = :userId")
    Vote findByProposalIdAndUserId(@Param("proposalId") Long proposalId, @Param("userId") Long userId);
}
