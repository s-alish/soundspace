package com.soundspace.playlistservice.infrastructure.client;

import com.soundspace.playlistservice.infrastructure.config.FeignConfig;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", configuration = FeignConfig.class)
public interface UserServiceClient {
    @GetMapping("/users/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);

    @GetMapping("/users/by-email/{email}")
    UserDTO getUserByEmail(@PathVariable("email") String email);

    @Data
    class UserDTO {
        private Long id;
        private String email;
        private String name;
    }
}