package com.soundspace.playlistservice.domain.model.playlist;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "playlists")
@Getter
@Setter
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    public Playlist() {}

    public Playlist(Long roomId) {
        this.roomId = roomId;
    }

}