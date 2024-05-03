package com.oauth.example.service;

import com.oauth.example.domain.entity.Tenant;
import com.oauth.example.domain.entity.Token;
import com.oauth.example.domain.entity.User;
import com.oauth.example.domain.enums.TokenType;
import com.oauth.example.repository.TokenRepository;
import com.oauth.example.utils.TestUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.ResponseFacade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {LogoutService.class})
@ExtendWith(SpringExtension.class)
class LogoutServiceTest {
    @Autowired
    private LogoutService logoutService;

    @MockBean
    private TokenRepository tokenRepository;

    /**
     * Method under test: {@link LogoutService#logout(HttpServletRequest, HttpServletResponse, Authentication)}
     */
    @Test
    void test_logout() throws IOException {
        HttpServletRequestWrapper request = mock(HttpServletRequestWrapper.class);
        when(request.getHeader(Mockito.any())).thenReturn("https://example.org/example");
        Response response = new Response();
        BearerTokenAuthenticationToken authentication = new BearerTokenAuthenticationToken("ABC123");
        logoutService.logout(request, response, authentication);
        verify(request).getHeader(Mockito.any());
        HttpServletResponse response2 = response.getResponse();
        assertTrue(response2 instanceof ResponseFacade);
        assertSame(response.getOutputStream(), response2.getOutputStream());
        assertFalse(authentication.isAuthenticated());
    }

    /**
     * Method under test: {@link LogoutService#logout(HttpServletRequest, HttpServletResponse, Authentication)}
     */
    @Test
    void test_logout_success() {
        Tenant currentTenant = TestUtils.getTenant();

        User user = TestUtils.getUser(currentTenant);

        Token token = new Token();
        token.setCreatedAt(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        token.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
        token.setDeletedAt(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        token.setExpired(true);
        token.setId(UUID.randomUUID());
        token.setRevoked(true);
        token.setToken("ABC123");
        token.setTokenType(TokenType.BEARER);
        token.setUpdatedAt(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        token.setUpdatedBy("2020-03-01");
        token.setUser(user);
        Optional<Token> ofResult = Optional.of(token);

        when(tokenRepository.save(Mockito.any())).thenReturn(token);
        when(tokenRepository.findByToken(Mockito.any())).thenReturn(ofResult);
        HttpServletRequestWrapper request = mock(HttpServletRequestWrapper.class);
        when(request.getHeader(Mockito.any())).thenReturn("Bearer ");
        Response response = new Response();
        logoutService.logout(request, response, new BearerTokenAuthenticationToken("ABC123"));
        verify(tokenRepository).save(Mockito.any());
        verify(tokenRepository).findByToken(Mockito.any());
        verify(request).getHeader(Mockito.any());
    }
}

