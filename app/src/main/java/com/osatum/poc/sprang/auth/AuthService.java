package com.osatum.poc.sprang.auth;

import com.osatum.poc.sprang.auth.dto.AuthResponse;
import com.osatum.poc.sprang.auth.dto.LoginRequest;
import com.osatum.poc.sprang.domain.auth.RefreshToken;
import com.osatum.poc.sprang.domain.auth.RefreshTokenRepository;
import com.osatum.poc.sprang.domain.user.UserRepository;
import com.osatum.poc.sprang.infrastructure.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.security.jwt.refresh-ttl-days}")
    private long refreshTtlDays;

    @Value("${app.security.cookie.secure:false}")
    private boolean refreshCookieSecure;

    @Value("${app.security.cookie.same-site:Lax}")
    private String refreshCookieSameSite;

    public AuthResponse login(LoginRequest req, HttpServletResponse res) {
        var user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        issueRefreshCookie(user.getId(), res);
        return new AuthResponse(jwtService.createAccessToken(user));
    }


    public AuthResponse refresh(String refreshCookie, HttpServletResponse res) {
        if (refreshCookie == null || refreshCookie.isBlank()) {
            throw new IllegalArgumentException("No refresh token");
        }

        String hash = sha256Base64(refreshCookie);

        var rt = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (rt.isRevoked() || rt.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        // rotacja
        rt.setRevoked(true);
        refreshTokenRepository.save(rt);

        var user = rt.getUser();
        issueRefreshCookie(user.getId(), res);

        return new AuthResponse(jwtService.createAccessToken(user));
    }

    public void logout(String refreshCookie, HttpServletResponse res) {
        if (refreshCookie != null && !refreshCookie.isBlank()) {
            String hash = sha256Base64(refreshCookie);
            refreshTokenRepository.findByTokenHash(hash).ifPresent(rt -> {
                rt.setRevoked(true);
                refreshTokenRepository.save(rt);
            });
        }
        clearRefreshCookie(res);
    }

    private void issueRefreshCookie(Long userId, HttpServletResponse res) {
        var user = userRepository.findById(userId).orElseThrow();

        String raw = UUID.randomUUID() + "." + UUID.randomUUID();
        String hash = sha256Base64(raw);

        var token = RefreshToken.builder()
                .user(user)
                .tokenHash(hash)
                .expiresAt(Instant.now().plus(refreshTtlDays, ChronoUnit.DAYS))
                .revoked(false)
                .createdAt(Instant.now())
                .build();
        refreshTokenRepository.save(token);

        ResponseCookie cookie = ResponseCookie.from("refresh_token", raw)
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite(refreshCookieSameSite)
                .path("/api/auth")
                .maxAge(refreshTtlDays * 24L * 60L * 60L)
                .build();

        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse res) {
        // spójnie z issueRefreshCookie (path/samesite/secure)
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite(refreshCookieSameSite)
                .path("/api/auth")
                .maxAge(0)
                .build();
        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private static String sha256Base64(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
