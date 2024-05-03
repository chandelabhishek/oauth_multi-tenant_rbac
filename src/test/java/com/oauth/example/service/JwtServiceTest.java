package com.oauth.example.service;

import com.oauth.example.domain.entity.Tenant;
import com.oauth.example.domain.entity.User;
import com.oauth.example.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class, classes = {JwtService.class})
@ActiveProfiles("test")
@SpringBootTest
class JwtServiceTest {
    private final String EMAIL = "test_token@example.com";
    @MockBean
    private JwtEncoder jwtEncoder;
    @MockBean
    private JwtDecoder jwtDecoder;
    @Autowired
    private JwtService jwtService;

    private String bearerToken = "eyJhbGciOiJIUzI1NiJ9.eyJ0ZW5hbnRJZCI6ImNmOWI5ZWIzLTE2MzYtNGJhMS1hYTM0LTNjMGE1MjFkZTE5NSIsInVzZXJJZCI6IjQ3ODJkYzA5LTQ4NDEtNDlhOC1hNmU5LWNhYTlmYmYzMDY3NCIsImlzcyI6Im15c3RpcGF5LmNvbSIsInN1YiI6ImNoYW5kZWxhYmhpc2hlazAwOEBnbWFpbC5jb20iLCJpYXQiOjE2OTMzMDQyODAsImV4cCI6MTY5MzM5MDY4MH0.-pWp8k8OoQpb2FYudI6TmyOMe6nzfSS4Qrrj6QM8Gag";
    private User user;
    private Tenant tenant;

    private Jwt jwt;

    @BeforeEach
    public void setup() {
        tenant = TestUtils.getTenant();
        user = TestUtils.getUser(tenant);
        user.setEmail(EMAIL);
        jwt = mock(Jwt.class);
        var claims = new HashMap<String, Object>();
        claims.put("issuer", "example.com");
        when(jwtEncoder.encode(Mockito.any())).thenReturn(jwt);
        when(jwtDecoder.decode(Mockito.any())).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn(EMAIL);
        when(jwt.getTokenValue()).thenReturn(bearerToken);
        when(jwt.getClaims()).thenReturn(claims);
        when(jwt.getExpiresAt()).thenReturn(Instant.now().minus(100L, ChronoUnit.MILLIS));
        bearerToken = jwtService.generateToken(new HashMap<>(), user);
    }


    /**
     * Method under test: {@link JwtService#extractUsername(String)}
     */
    @Test
    void test_extractUsername() {
        String actualExtractUsernameResult = jwtService.extractUsername(bearerToken);
        assertEquals(EMAIL, actualExtractUsernameResult);
    }

    /**
     * Method under test: {@link JwtService#extractClaim(String)}
     */
    @Test
    void test_extractClaim() {
        assertEquals(jwtService.extractClaim(bearerToken), jwt);
    }

    /**
     * Method under test: {@link JwtService#generateToken(User)}
     */
    @Test
    void test_generateToken() {
        String actualGenerateTokenResult = jwtService.generateToken(user);
        assertEquals(jwtService.extractUsername(actualGenerateTokenResult), EMAIL);
        assertEquals("example.com", jwtService.extractClaim(bearerToken).getClaims().get("issuer"));
    }

    /**
     * Method under test: {@link JwtService#generateRefreshToken(User)}
     */
    @Test
    void test_generateRefreshToken() {
        String actualGenerateRefreshTokenResult = jwtService.generateRefreshToken(user);
        assertEquals(jwtService.extractUsername(actualGenerateRefreshTokenResult), EMAIL);
    }

    /**
     * Method under test: {@link JwtService#isTokenExpired(String)}
     */
    @Test
    void test_isTokenExpired_returns_true() {
        var expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJteXN0aXBheS5jb20iLCJzdWIiOiJ0ZXN0X3Rva2VuQG15c3RpcGF5LmNvbSIsImlhdCI6MTY5Mzg5NDc3NiwiZXhwIjoxNjkzODk0Nzc2fQ.ibrQzm8q-dH31HD6NqJgVi-wVQP6yLaqZbEAboywwe0";
        assertTrue(jwtService.isTokenExpired(expiredToken));
    }
}

