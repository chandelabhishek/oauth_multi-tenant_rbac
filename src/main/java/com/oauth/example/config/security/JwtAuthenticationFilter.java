package com.oauth.example.config.security;

import com.oauth.example.domain.exception.NotFoundException;
import com.oauth.example.domain.exception.TokenInvalidException;
import com.oauth.example.domain.model.UserPrincipal;
import com.oauth.example.repository.TokenRepository;
import com.oauth.example.service.AuthService;
import com.oauth.example.service.JwtService;
import com.oauth.example.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserService userService;
    private final TokenRepository tokenRepository;
    private final AuthService authService;


    private UserPrincipal getUserPrincipalAndAssignTenantIfApplicable(UUID userId, String jwt) {
        var tenantId = jwtService.extractClaim(jwt).getClaimAsString("tenantId");
        if (StringUtils.isBlank(tenantId) || StringUtils.isEmpty(tenantId)) {
            return userService.findUserDetailsById(userId);
        }
        var userPrincipal = userService.findUserDetailsByUserIdAndTenantId(userId, UUID.fromString(tenantId));
        authService.assignTenant(userPrincipal.getUser(), UUID.fromString(tenantId));
        return userPrincipal;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException, AccessDeniedException, NotFoundException {
        System.out.println(request.getHeader("Authorization"));
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        final String jwt = authHeader.substring(7);

        Objects.requireNonNullElse(SecurityContextHolder.getContext().getAuthentication(),
                tokenRepository.findByToken(jwt)
                        .map(t -> !t.isExpired() && !t.isRevoked())
                        .orElseThrow(() -> new TokenInvalidException("Token is invalid or expired")));


        final var userId = jwtService.extractClaim(jwt).getClaimAsString("userId");
        UserPrincipal userPrincipal = getUserPrincipalAndAssignTenantIfApplicable(UUID.fromString(userId), jwt);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userPrincipal,
                null,
                userPrincipal.getAuthorities()
        );
        authenticationToken.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        filterChain.doFilter(request, response);
    }
}
