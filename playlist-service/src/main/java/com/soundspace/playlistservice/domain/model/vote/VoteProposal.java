package com.soundspace.playlistservice.domain.model.vote;

import com.soundspace.playlistservice.domain.model.playlist.Track;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "vote_proposals")
@Getter
@Setter
@NoArgsConstructor
public class VoteProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @ManyToOne
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "proposed_at", nullable = false)
    private LocalDateTime proposedAt;

    public enum Status {
        PENDING, ACCEPTED, REJECTED
    }
}
