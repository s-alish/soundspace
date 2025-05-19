package com.soundspace.playlistservice.infrastructure.repository.vote;


import com.soundspace.playlistservice.domain.model.vote.VoteProposal;
import com.soundspace.playlistservice.domain.model.vote.VoteProposal.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VoteProposalRepository extends JpaRepository<VoteProposal, Long> {
    Optional<VoteProposal> findByRoomIdAndStatus(Long roomId, VoteProposal.Status status);

    @Query("SELECT vp.track.id FROM VoteProposal vp WHERE vp.roomId = :roomId AND vp.status IN ('ACCEPTED', 'REJECTED')")
    List<Long> findVotedTrackIdsByRoomId(Long roomId);
}