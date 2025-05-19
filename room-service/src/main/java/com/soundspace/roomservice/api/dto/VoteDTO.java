package com.soundspace.roomservice.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteDTO {
    @NotNull(message = "Track ID is mandatory")
    private Long trackId;
}
