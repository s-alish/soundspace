package com.soundspace.roomservice.domain.model.playback;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "playback_states")
@Getter
@Setter
@NoArgsConstructor
public class PlaybackState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false, unique = true)
    private Long roomId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlaybackStateEnum state;

    @Column(name = "current_track_id")
    private Long currentTrackId;

    public enum PlaybackStateEnum {
        PLAYING, PAUSED
    }
}