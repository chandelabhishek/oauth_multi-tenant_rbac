package com.oauth.example.domain.dto;

import com.oauth.example.domain.annotations.PhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    @NotNull(message = "firstName is required")
    private String firstName;
    @NotNull(message = "lastName is required")
    private String lastName;
    @NotNull
    @Email
    private String email;
    @NotNull
    @PhoneNumber
    private String phoneNumber;
    @NotNull
    private String countryCode;
    @NotNull
    private String password;
    private String secureCode;
}
