package com.oauth.example.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientCredentialLoginRequest {
    private String clientId;
    private String clientSecret;
}
