package com.soundspace.playlistservice.infrastructure.client;

import com.soundspace.playlistservice.infrastructure.config.FeignConfig;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "room-service", configuration = FeignConfig.class)
public interface RoomServiceClient {
    @GetMapping("/rooms/{id}")
    RoomDTO getRoom(@PathVariable("id") Long id);

    @Data
    class RoomDTO {
        private Long id;
        private String name;
        private Long adminId;
    }
}



