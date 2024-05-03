package com.oauth.example.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecoverPasswordRequest {
    @NotNull(message = "token can not be blank")
    private String token;
    @NotNull(message = "newPassword can not be blank")
    private String newPassword;
}


