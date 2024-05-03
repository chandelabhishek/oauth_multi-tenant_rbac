package com.oauth.example.domain.model;

import com.oauth.example.domain.entity.Tenant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantWithToken {
    private Tenant tenant;
    private String accessToken;
    private String refreshToken;
}
