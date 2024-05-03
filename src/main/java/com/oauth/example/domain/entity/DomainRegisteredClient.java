package com.oauth.example.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;


@Setter
@Getter
@Entity
@Table(name = "oauth2_registered_client")
public class DomainRegisteredClient {
    @Id
    private String id;
    private String clientId;
    private String clientSecret;

    @JoinColumn(name = "tenant_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Tenant tenant;
    private OffsetDateTime clientSecretExpiresAt;
    private String clientName;
    private String clientAuthenticationMethods;
    private String authorizationGrantTypes;
    private String redirectUris;
    private String postLogoutRedirectUris;
    private String scopes;
    private String clientSettings;
    private String tokenSettings;
    private OffsetDateTime clientIdIssuedAt;
    @ColumnDefault("false")
    private boolean revoked;
    private String createdBy;
}


