package com.oauth.example.domain.dto;

import com.oauth.example.domain.enums.TenantType;
import com.oauth.example.domain.model.TenantConfig;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;


@Getter
@Setter
public class TenantDto {
    @NotNull(message = "id is required")
    private UUID id;
    private String name;
    private String email;
    private String phoneNumber;
    private String countryCode;
    private String address;
    private String state;
    private String city;
    private String postalCode;
    private String country;
    private String clientId;
    private TenantConfig config;
    private String status;
    private Boolean hasOwnCredentials;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private TenantType type;
}
