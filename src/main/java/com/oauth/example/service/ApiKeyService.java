package com.oauth.example.service;

import com.oauth.example.domain.dto.ApiKeysDto;
import com.oauth.example.domain.entity.User;
import com.oauth.example.domain.exception.NotFoundException;
import com.oauth.example.domain.mapper.RegisteredClientToDomainRegisteredClient;
import com.oauth.example.repository.RegisteredClientRepository;
import com.oauth.example.util.RandomStringGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiKeyService {
    private final RandomStringGenerator randomStringGenerator;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository jdbcRegisteredClientRepository;
    private final RegisteredClientRepository registeredClientRepository;
    private final RegisteredClientToDomainRegisteredClient registeredClientMapper;

    @Transactional
    public ApiKeysDto createApiKeys(User user) {
        var tenant = user.getCurrentTenant().orElseThrow(() -> new NotFoundException("user not found"));
        var pass = randomStringGenerator.clientSecretGenerator();
        var registeredClient = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId(randomStringGenerator.clientIdGenerator())
                .clientSecret(passwordEncoder.encode(pass))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantTypes(grantTypes -> grantTypes.add(
                        AuthorizationGrantType.CLIENT_CREDENTIALS))
                .redirectUri("http://127.0.0.1:8082/login/oauth2/code/spring")
                .scopes(scopes -> scopes.addAll(Set.of("user.read", "user.write", OidcScopes.OPENID)))
                .clientName(tenant.getId().toString())
                .build();

        var domainRegisteredClient = registeredClientMapper.mapRegisteredClientToDomainRegisteredClient(registeredClient);
        domainRegisteredClient.setTenant(tenant);
        domainRegisteredClient.setCreatedBy(user.getId().toString());
        registeredClientRepository.save(domainRegisteredClient);
        return ApiKeysDto.builder()
                .clientId(registeredClient.getClientId())
                .clientSecret(pass)
                .build();
    }
}
