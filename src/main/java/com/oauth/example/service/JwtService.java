package com.oauth.example.service;

import com.oauth.example.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder encoder;
    private final JwtDecoder jwtDecoder;
    @Value("${jwt.secret}")
    private String secretKey;
    @Value("${jwt.token.expiration}")
    private long jwtExpiration;
    @Value("${jwt.refreshToken.expiration}")
    private long refreshExpiration;
    @Value("${jwt.issuer}")
    private String issuer;

    public String extractUsername(String token) {
        return extractClaim(token).getSubject();
    }

    public Jwt extractClaim(String token) {
        return jwtDecoder.decode(token);
    }

    public String generateToken(User user) {
        return generateToken(new HashMap<>(), user);
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            User user
    ) {
        return buildToken(extraClaims, user, jwtExpiration);
    }

    public String generateRefreshToken(
            User user
    ) {
        return buildToken(new HashMap<>(), user, refreshExpiration);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            User user,
            long expiration
    ) {
        var claimSet = JwtClaimsSet.builder()
                .issuer(issuer)
                .claims(jwtClaim -> jwtClaim.putAll(extraClaims))
                .subject(user.getEmail())
                .issuedAt(Instant.now())
                .expiresAt(new Date(System.currentTimeMillis() + expiration).toInstant())
                .build();

        return this.encoder.encode(JwtEncoderParameters.from(claimSet)).getTokenValue();
    }

    public boolean isTokenExpired(String token) {
        return Instant.now().isAfter(extractExpiration(token));
    }

    private Instant extractExpiration(String token) {
        return extractClaim(token).getExpiresAt();
    }

    public Jwt decode(String token) {
        return jwtDecoder.decode(token);
    }
}