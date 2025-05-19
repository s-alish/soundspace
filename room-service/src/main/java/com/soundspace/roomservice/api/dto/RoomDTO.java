package com.soundspace.roomservice.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomDTO {
    @NotBlank(message = "Name is mandatory")
    private String name;
}
