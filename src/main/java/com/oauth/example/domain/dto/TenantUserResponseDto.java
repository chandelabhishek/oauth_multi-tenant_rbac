package com.oauth.example.domain.dto;

import com.oauth.example.domain.enums.UserStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


@Getter
@Setter
public class TenantUserResponseDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String countryCode;
    private String authority;
    private UserStatus status;
}
