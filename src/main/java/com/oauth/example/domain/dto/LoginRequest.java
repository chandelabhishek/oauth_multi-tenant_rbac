package com.oauth.example.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(@NotNull(message = "Email is Mandatory") @Email(message = "Invalid emailId") String email,
                           @NotNull(message = "password is required") String password) {
    public LoginRequest() {
        this(null, null);
    }
}