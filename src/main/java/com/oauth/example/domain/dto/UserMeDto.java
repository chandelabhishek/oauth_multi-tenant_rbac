package com.oauth.example.domain.dto;

import com.oauth.example.domain.entity.Tenant;
import com.oauth.example.domain.enums.UserStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class UserMeDto {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String countryCode;
    private OffsetDateTime lastPasswordChangedAt;
    private Map<String, Object> config;
    private UserStatus status;
    private List<String> currentRoles;
    private Tenant tenant;

}
