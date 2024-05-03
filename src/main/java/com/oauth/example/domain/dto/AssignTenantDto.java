package com.oauth.example.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AssignTenantDto {
    @NotNull(message = "tenantId cannot be null")
    private UUID tenantId;
}
