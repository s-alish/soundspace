package com.soundspace.roomservice.infrastructure.client;

import com.soundspace.roomservice.infrastructure.config.FeignConfig;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "playlist-service", configuration = FeignConfig.class)
public interface PlaylistServiceClient {
    @GetMapping("/rooms/{id}/tracks")
    List<TrackDTO> getTracksByRoomId(@PathVariable("id") Long roomId);

    @Data
    static class TrackDTO {
        private Long id;
        private String title;
        private String artist;
        private Integer duration;
    }
}

