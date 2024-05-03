package com.oauth.example.domain.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ApiKeysDto {
    private String clientId;
    private String clientSecret;
}
