package com.oauth.example.service;

import com.oauth.example.domain.dto.*;
import com.oauth.example.domain.entity.*;
import com.oauth.example.domain.enums.Authority;
import com.oauth.example.domain.enums.TokenType;
import com.oauth.example.domain.enums.UserStatus;
import com.oauth.example.domain.exception.NotFoundException;
import com.oauth.example.repository.*;
import com.oauth.example.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    public static final Logger logger = LogManager.getLogger(AuthService.class);
    private final UserService userService;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final CryptoService cryptoService;
    private final RoleRepository roleRepository;
    private final UserTenantRepository userTenantRepository;
    private final RegisteredClientRepository registeredClientRepository;
    private final TenantRepository tenantRepository;

    @Value("${app.frontendUrl}")
    private String frontendUrl;

    public SignupResponse register(SignUpRequest signUpRequest) {
        var user = userService.save(signUpRequest);
        var tokens = generateToken(user);
        return new SignupResponse(tokens.getAccessToken());
    }

    private Map<String, Object> extraClaims(User user) {
        var extraClaims = new HashMap<String, Object>();
        var activeTenant = user.getCurrentTenant();
        String tenantId = activeTenant.isPresent() ? activeTenant.get().getId().toString() : "";
        extraClaims.put("tenantId", tenantId);
        extraClaims.put("userId", user.getId());
        extraClaims.put("scope", String.join("", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()));
        return extraClaims;
    }

    public AuthResponse generateToken(User user) {
        var jwtToken = jwtService.generateToken(this.extraClaims(user), user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(user, jwtToken);
        var auth = new AuthResponse();
        auth.setAccessToken(jwtToken);
        auth.setRefreshToken(refreshToken);
        return auth;
    }

    public AuthResponse authenticate(LoginRequest request) {
        var user = userService.findByEmail(request.email()).orElseThrow(() -> new BadCredentialsException("username or password is incorrect"));
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );
        if (user.getActiveUserTenants().size() == 1) { // if there's just 1 tenant/agency this user is tagged to then pick it automatically
            return assignTenantAndGenerateToken(user, user.getUserTenants().iterator().next().getTenant().getId());
        }

        var tokens = generateToken(user);
        return AuthResponse.builder()
                .accessToken(tokens.getAccessToken())
                .refreshToken(tokens.getRefreshToken())
                .build();
    }

    public AuthResponse authenticateAdmin(LoginRequest request) {
        var user = userService.findByEmail(request.email()).orElseThrow(() -> new BadCredentialsException("username or password is incorrect"));
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );
        var superAdminRole = roleRepository.findByName(Authority.SCOPE_SYSTEM_ADMIN.name());
        var userTenant = userTenantRepository.findUserTenantByUserIdAndRoleId(user.getId(), superAdminRole.getId());
        user.setCurrentTenant(userTenant.getTenant());
        var tokens = generateToken(user);
        return AuthResponse.builder()
                .accessToken(tokens.getAccessToken())
                .refreshToken(tokens.getRefreshToken())
                .build();
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        tokenRepository.updateRevokedAndExpiredByUser(true, true, user);
    }

//    public void refreshToken(
//            HttpServletRequest request,
//            HttpServletResponse response
//    ) throws IOException {
//        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
//        final String refreshToken;
//        final String userEmail;
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            return;
//        }
//        refreshToken = authHeader.substring(7);
//        userEmail = jwtService.extractUsername(refreshToken);
//        if (userEmail != null) {
//            var user = userService.findByEmail(userEmail);
//            user.orElseThrow(IllegalStateException::new);
//            var userDetails = userMapper.mapUserToUserDetails(user.get());
//            if (jwtService.isTokenValid(refreshToken, userDetails)) {
//                var accessToken = jwtService.generateToken(user.get());
//                revokeAllUserTokens(user.get());
//                saveUserToken(user.get(), accessToken);
//                var authResponse = AuthenticationResponse.builder()
//                        .accessToken(accessToken)
//                        .refreshToken(refreshToken)
//                        .build();
//                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
//            }
//        }
//    }

    public void triggerForgotPasswordFlow(String email) throws NotFoundException, IOException {
        var user = userService.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));
        var resetLink = String.format("%s?token=%s", frontendUrl, cryptoService.encrypt(email));
        emailService.sendForgotPasswordEmail(resetLink, user);
    }

    public void recoverPassword(RecoverPasswordRequest recoverPasswordRequest) throws NotFoundException {
        var email = cryptoService.decrypt(recoverPasswordRequest.getToken());
        var user = userService.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));
        user.setPassword(passwordEncoder.encode(recoverPasswordRequest.getNewPassword()));
        user.setLastPasswordChangedAt(OffsetDateTime.now());
        userService.save(user);
        revokeAllUserTokens(user);
    }

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    private void findAndSetCurrentTenantFromUser(User user, UUID tenantId) {
        // get the all the userTenant records with given tenantId
        var desiredUserTenantRecords = user.getUserTenants()
                .stream()
                .filter(userTenant -> tenantId.equals(userTenant.getTenant().getId())
                        && UserStatus.ACTIVE.equals(userTenant.getStatus())
                ).toList();
        // get all the roles for this user for given tenantId
        var roles = desiredUserTenantRecords.stream().map(UserTenant::getRole);

        // get the tenant With current TenantId and set it as current user
        var currentTenant = desiredUserTenantRecords.stream().findFirst().orElseThrow(() -> new NotFoundException("Tenant not found"));

        user.setCurrentTenant(currentTenant.getTenant());
        user.setCurrentRoleNames(roles.map(Role::getName).toList());
    }

    public void assignTenant(User user, UUID tenantId) throws NotFoundException {
        if (user.getUserTenants().isEmpty()) {
            // if user does not have any user tenants right now, then fetch and assign
            var userTenants = userTenantRepository.findIdByUserIdAndTenantId(user.getId(), tenantId).orElseThrow(() -> new NotFoundException("Tenant Not found"));
            user.setCurrentTenant(userTenants.get(0).getTenant());
            user.setCurrentRoleNames(userTenants.stream().map(userTenant -> userTenant.getRole().getName()).toList());
        } else {
            // if user has tenants the filter and assign
            findAndSetCurrentTenantFromUser(user, tenantId);
        }
    }

    public AuthResponse assignTenantAndGenerateToken(User user, UUID tenantId) throws NotFoundException {
        assignTenant(user, tenantId);
        return generateToken(user);
    }

    @Bean
    @Transactional
    public OAuth2TokenCustomizer<JwtEncodingContext> accessTokenCustomizer() {
        return context -> {
            var userName = context.getPrincipal().getName();
            DomainRegisteredClient clientDetails = registeredClientRepository.findByClientId(userName).orElseThrow(() -> new NotFoundException("not found!!"));
            var tenant = clientDetails.getTenant();
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                context.getClaims().claims(claims -> {
                    claims.put("userId", clientDetails.getCreatedBy());
                    claims.put("tenantId", tenant.getId());
                    claims.put("scope", Authority.SCOPE_TENANT_ADMIN.name().substring(6));
                });
            }
        };
    }
}
