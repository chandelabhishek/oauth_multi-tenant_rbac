package com.oauth.example.service;

import com.oauth.example.domain.dto.AuthResponse;
import com.oauth.example.domain.dto.LoginRequest;
import com.oauth.example.domain.dto.RecoverPasswordRequest;
import com.oauth.example.domain.dto.SignUpRequest;
import com.oauth.example.domain.entity.Tenant;
import com.oauth.example.domain.entity.Token;
import com.oauth.example.domain.entity.User;
import com.oauth.example.domain.enums.TokenType;
import com.oauth.example.domain.exception.NotFoundException;
import com.oauth.example.domain.mapper.UserMapper;
import com.oauth.example.repository.*;
import com.oauth.example.service.email.EmailService;
import com.oauth.example.utils.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {AuthService.class})
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthServiceTest {
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String REFRESH_TOKEN = "refreshToken";
    private final String EMAIL = "test_token@example.com";
    @MockBean
    private AuthenticationManager authenticationManager;
    @Autowired
    private AuthService authService;
    @MockBean
    private CryptoService cryptoService;
    @MockBean
    private EmailService emailService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private ModelMapper modelMapper;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @MockBean
    private TenantRepository tenantRepository;
    @MockBean
    private TokenRepository tokenRepository;
    @MockBean
    private UserMapper userMapper;
    @MockBean
    private UserService userService;

    @MockBean
    private RoleRepository roleRepository;
    @MockBean
    private UserTenantRepository userTenantRepository;

    @MockBean
    private RegisteredClientRepository registeredClientRepository;
    private User user;
    private Tenant tenant;
    private Token token;

    @BeforeAll
    public void setup() {
        tenant = TestUtils.getTenant();
        user = TestUtils.getUser(tenant);
        user.setEmail(EMAIL);
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
    }

    /**
     * Method under test: {@link AuthService#register(SignUpRequest)}
     */
//    @Test
//    void test_register_success() {
//        var refreshToken = "some_refresh_token";
//        var accessToken = "some_access_token";
//
//        when(passwordEncoder.encode(Mockito.<CharSequence>any())).thenReturn("secret");
//        when(jwtService.generateToken(Mockito.any(), Mockito.any())).thenReturn(accessToken);
//        when(jwtService.generateRefreshToken(Mockito.any())).thenReturn(refreshToken);
//        when(userService.save(Mockito.any(SignUpRequest.class))).thenReturn(user);
//        SignUpRequest request = new SignUpRequest();
//        request.setCountryCode("GB");
//        request.setEmail("jane.doe@example.org");
//        request.setFirstName("Jane");
//        request.setLastName("Doe");
//        request.setPassword("password");
//        request.setPhoneNumber("6625550144");
//        assertEquals(authService.register(request), new SignupResponse(accessToken));
//        verify(modelMapper).map(Mockito.<Object>any(), Mockito.<Class<User>>any());
//        verify(passwordEncoder).encode(Mockito.<CharSequence>any());
//    }

    /**
     * Method under test: {@link AuthService#generateToken(User)}
     */
    @Test
    void test_generateToken_when_active_tenant_present() {
        when(tokenRepository.save(Mockito.any())).thenReturn(token);
        doNothing().when(tokenRepository).updateRevokedAndExpiredByUser(anyBoolean(), anyBoolean(), Mockito.any());
        when(jwtService.generateRefreshToken(Mockito.any())).thenReturn("ABC123");
        when(jwtService.generateToken(Mockito.any(), Mockito.any())).thenReturn("ABC123");

        AuthResponse actualGenerateTokenResult = authService.generateToken(user);
        assertEquals("ABC123", actualGenerateTokenResult.getAccessToken());
        assertEquals("ABC123", actualGenerateTokenResult.getRefreshToken());
        verify(tokenRepository).save(Mockito.any());
//        verify(tokenRepository).updateRevokedAndExpiredByUser(anyBoolean(), anyBoolean(), Mockito.<User>any());
        verify(jwtService).generateRefreshToken(Mockito.any());
        verify(jwtService).generateToken(Mockito.any(), Mockito.any());
    }

    @Test
    void test_generateToken_when_tenant_not_present() {
        when(tokenRepository.save(Mockito.any())).thenReturn(token);
        doNothing().when(tokenRepository).updateRevokedAndExpiredByUser(anyBoolean(), anyBoolean(), Mockito.any());
        when(jwtService.generateRefreshToken(Mockito.any())).thenReturn("ABC123");
        when(jwtService.generateToken(Mockito.any(), Mockito.any())).thenReturn("ABC123");

        User user = TestUtils.getUser(null);

        AuthResponse actualGenerateTokenResult = authService.generateToken(user);
        assertEquals("ABC123", actualGenerateTokenResult.getAccessToken());
        assertEquals("ABC123", actualGenerateTokenResult.getRefreshToken());
        verify(tokenRepository).save(Mockito.any());
//        verify(tokenRepository).updateRevokedAndExpiredByUser(anyBoolean(), anyBoolean(), Mockito.<User>any());
//        verify(jwtService).generateRefreshToken(Mockito.<User>any());
        verify(jwtService).generateToken(Mockito.any(), Mockito.any());
    }

    /**
     * Method under test: {@link AuthService#authenticate(LoginRequest)}
     */
    @Test
    void test_authenticate_success() throws AuthenticationException {
        when(userService.findByEmail(Mockito.any())).thenReturn(Optional.of(user));

        when(tokenRepository.save(Mockito.any())).thenReturn(token);
        doNothing().when(tokenRepository).updateRevokedAndExpiredByUser(anyBoolean(), anyBoolean(), Mockito.any());
        when(jwtService.generateRefreshToken(Mockito.any())).thenReturn("ABC123");
        when(jwtService.generateToken(Mockito.any(), Mockito.any())).thenReturn("ABC123");
        when(authenticationManager.authenticate(Mockito.any()))
                .thenReturn(new BearerTokenAuthenticationToken("ABC123"));
        AuthResponse actualAuthenticateResult = authService
                .authenticate(new LoginRequest("jane.doe@example.org", "iloveyou"));
        assertEquals("ABC123", actualAuthenticateResult.getAccessToken());
        assertEquals("ABC123", actualAuthenticateResult.getRefreshToken());
        verify(userService).findByEmail(Mockito.any());
        verify(tokenRepository).save(Mockito.any());
//        verify(tokenRepository).updateRevokedAndExpiredByUser(anyBoolean(), anyBoolean(), Mockito.<User>any());
        verify(jwtService).generateRefreshToken(Mockito.any());
        verify(jwtService).generateToken(Mockito.any(), Mockito.any());
        verify(authenticationManager).authenticate(Mockito.any());
    }

    @Test
    void test_authenticate_throws_AccessDeniedException_when_user_not_found() throws AuthenticationException {
        when(userService.findByEmail(Mockito.any())).thenReturn(Optional.empty());
        assertThrows(BadCredentialsException.class, () -> authService
                .authenticate(new LoginRequest("jane.doe@example.org", "password")));
        verify(userService).findByEmail(Mockito.any());
    }

    @Test
    void test_authenticate_throws_AccessDeniedException_when_password_did_not_match() throws AuthenticationException {
        when(userService.findByEmail(Mockito.any())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(Mockito.any())).thenThrow(BadCredentialsException.class);
        assertThrows(BadCredentialsException.class, () -> authService
                .authenticate(new LoginRequest("jane.doe@example.org", "password")));
        verify(userService).findByEmail(Mockito.any());
    }

    /**
     * Method under test: {@link AuthService#triggerForgotPasswordFlow(String)}
     */
    @Test
    void test_triggerForgotPasswordFlow_success() throws Exception {
        Optional<User> ofResult = Optional.of(user);
        when(userService.findByEmail(Mockito.any())).thenReturn(ofResult);
        doNothing().when(emailService).sendForgotPasswordEmail(Mockito.any(), Mockito.any());
        when(cryptoService.encrypt(Mockito.any())).thenReturn("Encrypt");
        authService.triggerForgotPasswordFlow("jane.doe@example.org");
        verify(userService).findByEmail(Mockito.any());
        verify(emailService).sendForgotPasswordEmail(Mockito.any(), Mockito.any());
        verify(cryptoService).encrypt(Mockito.any());
    }

    /**
     * Method under test: {@link AuthService#triggerForgotPasswordFlow(String)}
     */
    @Test
    void test_triggerForgotPasswordFlow_throws_NotFoundException_when_user_not_found() throws Exception {
        when(userService.findByEmail(Mockito.any())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> authService.triggerForgotPasswordFlow("jane.doe@example.org"));
        verify(userService).findByEmail(Mockito.any());
    }

    /**
     * Method under test: {@link AuthService#recoverPassword(RecoverPasswordRequest)}
     */
    @Test
    void test_recoverPassword_success() throws NotFoundException {
        when(userService.save(Mockito.<User>any())).thenReturn(user);
        when(userService.findByEmail(Mockito.any())).thenReturn(Optional.of(user));
        doNothing().when(tokenRepository).updateRevokedAndExpiredByUser(anyBoolean(), anyBoolean(), Mockito.any());
        when(cryptoService.decrypt(Mockito.any())).thenReturn("Decrypt");
        when(passwordEncoder.encode(Mockito.any())).thenReturn("secret");

        RecoverPasswordRequest recoverPasswordRequest = new RecoverPasswordRequest();
        recoverPasswordRequest.setNewPassword("password");
        recoverPasswordRequest.setToken("ABC123");
        authService.recoverPassword(recoverPasswordRequest);
        verify(userService).save(Mockito.<User>any());
        verify(userService).findByEmail(Mockito.any());
        verify(tokenRepository).updateRevokedAndExpiredByUser(anyBoolean(), anyBoolean(), Mockito.any());
        verify(cryptoService).decrypt(Mockito.any());
        verify(passwordEncoder).encode(Mockito.any());
    }

    /**
     * Method under test: {@link AuthService#recoverPassword(RecoverPasswordRequest)}
     */
    @Test
    void test_recoverPassword_throws_NotFoundException_when_user_is_not_found() throws NotFoundException {
        when(userService.findByEmail(Mockito.any())).thenReturn(Optional.empty());
        when(cryptoService.decrypt(Mockito.any())).thenReturn("Decrypt");

        RecoverPasswordRequest recoverPasswordRequest = new RecoverPasswordRequest();
        recoverPasswordRequest.setNewPassword("pass");
        recoverPasswordRequest.setToken("ABC123");
        assertThrows(NotFoundException.class, () -> authService.recoverPassword(recoverPasswordRequest));
        verify(userService).findByEmail(Mockito.any());
        verify(cryptoService).decrypt(Mockito.any());
    }

//    /**
//     * Method under test: {@link AuthenticationService#recoverPassword(RecoverPasswordRequest)}
//     */
//    @Test
//    void test_recoverPassword_throws_NotFoundException_when_token_cannot_be_decrypted() throws NotFoundException {
//        when(userService.findByEmail(Mockito.<String>any())).thenReturn(Optional.empty());
//        when(cryptoService.decrypt(Mockito.<String>any())).thenReturn("Decrypt");
//
//        RecoverPasswordRequest recoverPasswordRequest = new RecoverPasswordRequest();
//        recoverPasswordRequest.setNewPassword("pass");
//        recoverPasswordRequest.setToken("ABC123");
//        assertThrows(NotFoundException.class, () -> authenticationService.recoverPassword(recoverPasswordRequest));
//        verify(userService).findByEmail(Mockito.<String>any());
//        verify(cryptoService).decrypt(Mockito.<String>any());
//    }

    /**
     * Method under test: {@link AuthService#encodePassword(String)}
     */
    @Test
    void test_encodePassword_success() {
        when(passwordEncoder.encode(Mockito.any())).thenReturn("secret");
        assertEquals("secret", authService.encodePassword("password"));
        verify(passwordEncoder).encode(Mockito.any());
    }

    /**
     * Method under test: {@link AuthService#encodePassword(String)}
     */
    @Test
    void test_encodePassword_throws_IllegalStateException_when_encrypted_password_is_null() {
        when(passwordEncoder.encode(Mockito.any())).thenThrow(new IllegalStateException("foo"));
        assertThrows(IllegalStateException.class, () -> authService.encodePassword(null));
        verify(passwordEncoder).encode(Mockito.any());
    }
}

