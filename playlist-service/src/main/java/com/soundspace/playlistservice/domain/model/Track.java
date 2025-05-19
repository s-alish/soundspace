package com.soundspace.playlistservice.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tracks")
@Getter
@Setter
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column
    private String artist;

    @Column
    private Integer duration;

    public Track() {}

    public Track(String title, String artist, Integer duration) {
        this.title = title;
        this.artist = artist;
        this.duration = duration;
    }

}
