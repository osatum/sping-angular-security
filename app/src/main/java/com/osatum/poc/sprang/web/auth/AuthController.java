package com.osatum.poc.sprang.web.auth;

import com.osatum.poc.sprang.auth.AuthService;
import com.osatum.poc.sprang.auth.dto.AuthResponse;
import com.osatum.poc.sprang.auth.dto.LoginRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AuthApplication")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req, HttpServletResponse res) {
        return authService.login(req, res);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshCookie,
            HttpServletResponse res
    ) {

        return authService.refresh(refreshCookie, res);
    }

    @PostMapping("/logout")
    public void logout(
            @CookieValue(name = "refresh_token", required = false) String refreshCookie,
            HttpServletResponse res
    ) {
        authService.logout(refreshCookie, res);
    }
}
