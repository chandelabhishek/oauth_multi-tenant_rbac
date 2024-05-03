package com.oauth.example.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AddExistingUserDto {
    @NotNull(message = "userId cannot be blank")
    private UUID userId;
}
