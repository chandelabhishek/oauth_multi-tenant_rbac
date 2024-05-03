package com.oauth.example.domain.model;

import com.oauth.example.domain.enums.TenantType;

import java.util.UUID;

public interface AssignableTenant {
    UUID getTenantId();

    String getName();

    TenantType getType();
}
