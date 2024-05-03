package com.oauth.example.service;

import com.oauth.example.domain.entity.DomainRegisteredClient;
import com.oauth.example.domain.entity.Tenant;
import com.oauth.example.domain.entity.User;
import com.oauth.example.domain.exception.NotFoundException;
import com.oauth.example.domain.mapper.RegisteredClientToDomainRegisteredClient;
import com.oauth.example.domain.model.TenantConfig;
import com.oauth.example.repository.RegisteredClientRepository;
import com.oauth.example.util.RandomStringGenerator;
import com.oauth.example.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {ApiKeyService.class})
@ExtendWith(SpringExtension.class)
class ApiKeyServiceTest {
    @Autowired
    private ApiKeyService apiKeyService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private RandomStringGenerator randomStringGenerator;

    @MockBean
    private RegisteredClientRepository registeredClientRepository;

    @MockBean
    private org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository registeredClientRepository2;

    @MockBean
    private RegisteredClientToDomainRegisteredClient registeredClientToDomainRegisteredClient;

    /**
     * Method under test: {@link ApiKeyService#createApiKeys(User)}
     */
    @Test
    void testCreateApiKeys_User_Not_Found_Exception() {
        // Arrange
        when(randomStringGenerator.clientIdGenerator()).thenReturn("Client Id Generator");
        when(randomStringGenerator.clientSecretGenerator()).thenReturn("Client Secret Generator");
        when(registeredClientToDomainRegisteredClient
                .mapRegisteredClientToDomainRegisteredClient(Mockito.any()))
                .thenThrow(new NotFoundException("An error occurred"));
        when(passwordEncoder.encode(Mockito.any())).thenReturn("secret");

        TenantConfig config = new TenantConfig();
        config.setActiveProviderId("42");

        TenantConfig config2 = new TenantConfig();
        config2.setActiveProviderId("42");
        User user = TestUtils.getUser();

        // Act and Assert
        assertThrows(NotFoundException.class, () -> apiKeyService.createApiKeys(user));
        verify(registeredClientToDomainRegisteredClient)
                .mapRegisteredClientToDomainRegisteredClient(Mockito.any());
        verify(randomStringGenerator).clientIdGenerator();
        verify(randomStringGenerator).clientSecretGenerator();
        verify(passwordEncoder).encode(Mockito.any());
    }

    /**
     * Method under test: {@link ApiKeyService#createApiKeys(User)}
     */
    @Test
    void testCreateApiKeys2() {
        // Arrange
        when(randomStringGenerator.clientIdGenerator()).thenReturn("Client Id Generator");
        when(randomStringGenerator.clientSecretGenerator()).thenReturn("Client Secret Generator");
        when(passwordEncoder.encode(Mockito.any())).thenReturn("secret");
        var domainRegisteredClient = Mockito.mock(DomainRegisteredClient.class);
        when(registeredClientToDomainRegisteredClient
                .mapRegisteredClientToDomainRegisteredClient(Mockito.any()))
                .thenReturn(domainRegisteredClient);

        TenantConfig config = new TenantConfig();
        config.setActiveProviderId("42");

        TenantConfig config2 = new TenantConfig();
        config2.setActiveProviderId("42");

        Tenant tenant = TestUtils.getTenant();
        User user = TestUtils.getUser(tenant);

        // Act and Assert
        apiKeyService.createApiKeys(user);
        verify(randomStringGenerator).clientIdGenerator();
        verify(randomStringGenerator).clientSecretGenerator();
        verify(passwordEncoder).encode(Mockito.any());
    }
}
