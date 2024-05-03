package com.oauth.example.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcceptUserInviteDto {
    @NotNull(message = "token is mandatory")
    private String token;
}
