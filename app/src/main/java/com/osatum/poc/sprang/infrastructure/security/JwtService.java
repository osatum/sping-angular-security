package com.osatum.poc.sprang.infrastructure.security;

import com.osatum.poc.sprang.domain.user.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey key;
    private final String issuer;
    private final long accessTtlMinutes;
    private final String rawSecret;

    public JwtService(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.issuer}") String issuer,
            @Value("${app.security.jwt.access-ttl-minutes}") long accessTtlMinutes
    ) {
        this.rawSecret = secret;
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.accessTtlMinutes = accessTtlMinutes;
    }

    @PostConstruct
    void validateConfig() {
        if (rawSecret == null || rawSecret.isBlank()) {
            throw new IllegalStateException("JWT secret is empty. Set app.security.jwt.secret (e.g. env APP_JWT_SECRET).");
        }
        if ("CHANGE_ME_TO_A_LONG_RANDOM_SECRET_32CHARS_MIN".equals(rawSecret)) {
            throw new IllegalStateException("JWT secret is still a placeholder. Set a real secret (32+ chars) via env/secret manager.");
        }
        if (rawSecret.length() < 32) {
            throw new IllegalStateException("JWT secret too short. Use 32+ characters (preferably much longer).");
        }
    }

    public String createAccessToken(AppUser user) {
        Instant now = Instant.now();
        Instant exp = now.plus(accessTtlMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .issuer(issuer)
                .subject(user.getId().toString())
                .claims(Map.of(
                        "email", user.getEmail(),
                        "roles", user.getRoles()
                ))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
