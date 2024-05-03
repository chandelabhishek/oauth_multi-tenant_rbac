package com.oauth.example.domain.mapper;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth.example.domain.entity.DomainRegisteredClient;
import com.oauth.example.repository.RegisteredClientRepository;
import org.modelmapper.ModelMapper;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class RegisteredClientToDomainRegisteredClient {
    private final ModelMapper modelMapper;
    private ObjectMapper objectMapper;

    public RegisteredClientToDomainRegisteredClient(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        this.objectMapper = new ObjectMapper();
    }

    public DomainRegisteredClient mapRegisteredClientToDomainRegisteredClient(RegisteredClient registeredClient) {
        objectMapper = new ObjectMapper();
        List<String> clientAuthenticationMethods = new ArrayList<>(registeredClient.getClientAuthenticationMethods().size());
        registeredClient.getClientAuthenticationMethods().forEach(clientAuthenticationMethod ->
                clientAuthenticationMethods.add(clientAuthenticationMethod.getValue()));

        List<String> authorizationGrantTypes = new ArrayList<>(registeredClient.getAuthorizationGrantTypes().size());
        registeredClient.getAuthorizationGrantTypes().forEach(authorizationGrantType ->
                authorizationGrantTypes.add(authorizationGrantType.getValue()));


        ClassLoader classLoader = RegisteredClientRepository.class.getClassLoader();
        List<Module> securityModules = SecurityJackson2Modules.getModules(classLoader);
        objectMapper.registerModules(securityModules);
        objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());


        var propertyMapper = modelMapper.typeMap(RegisteredClient.class, DomainRegisteredClient.class);
        var domainClient = propertyMapper.map(registeredClient);
        domainClient.setClientSettings(writeMap(registeredClient.getClientSettings().getSettings()));
        domainClient.setTokenSettings(writeMap(registeredClient.getTokenSettings().getSettings()));

        domainClient.setClientAuthenticationMethods(StringUtils.collectionToCommaDelimitedString(clientAuthenticationMethods));
        domainClient.setAuthorizationGrantTypes(StringUtils.collectionToCommaDelimitedString(authorizationGrantTypes));
        domainClient.setRedirectUris(StringUtils.collectionToCommaDelimitedString(registeredClient.getRedirectUris()));
        domainClient.setPostLogoutRedirectUris(StringUtils.collectionToCommaDelimitedString(registeredClient.getPostLogoutRedirectUris()));
        domainClient.setScopes(StringUtils.collectionToCommaDelimitedString(registeredClient.getScopes()));
        return domainClient;
    }

    private String writeMap(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }

}
