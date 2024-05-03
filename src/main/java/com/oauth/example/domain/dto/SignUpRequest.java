package com.oauth.example.domain.dto;

import com.oauth.example.domain.annotations.PhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequest {
    @NotNull(message = "firstName is required")
    @Size(min = 2, message = "{validation.name.size.too_short}")
    @Size(max = 200, message = "{validation.name.size.too_long}")
    private String firstName;
    @NotNull(message = "lastName is required")
    @Size(min = 2, message = "{validation.name.size.too_short}")
    @Size(max = 200, message = "{validation.name.size.too_long}")
    private String lastName;
    @NotNull(message = "email is required")
    @Email(message = "email is not valid")
    private String email;
    @PhoneNumber()
    private String phoneNumber;
    private String countryCode;
    private String password;
}
