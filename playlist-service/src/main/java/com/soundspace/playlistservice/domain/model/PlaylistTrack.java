package com.soundspace.playlistservice.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "playlist_tracks")
@Getter
@Setter
public class PlaylistTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    @ManyToOne
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;

    public PlaylistTrack() {}

    public PlaylistTrack(Playlist playlist, Track track) {
        this.playlist = playlist;
        this.track = track;
    }

}
