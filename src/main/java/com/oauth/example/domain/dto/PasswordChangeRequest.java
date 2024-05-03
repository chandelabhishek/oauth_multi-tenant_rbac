package com.oauth.example.domain.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeRequest {
    @NotNull(message = "oldPassword is required")
    private String oldPassword;
    @NotNull(message = "newPassword is required")
    private String newPassword;
}
