package com.osatum.poc.sprang.infrastructure.security;

import com.osatum.poc.sprang.domain.user.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Value("${spring.h2.console.enabled:false}")
    private boolean h2ConsoleEnabled;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(reg -> {
                    reg.requestMatchers(
                                    "/api/auth/**",
                                    "/v3/api-docs/**",
                                    "/swagger-ui.html",
                                    "/swagger-ui/**"
                            )
                            .permitAll();

                    if (h2ConsoleEnabled) {
                        reg.requestMatchers("/h2-console/**").permitAll();
                    }

                    reg.requestMatchers("/api/admin/**").hasRole("ADMIN");
                    reg.anyRequest().authenticated();
                })
                .headers(headers -> {
                    // H2-console działa w iframe; włącz tylko gdy console jest włączone (dev)
                    if (h2ConsoleEnabled) {
                        headers.frameOptions(frame -> frame.disable());
                    }
                })
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    OncePerRequestFilter jwtFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                    throws ServletException, IOException {
                try {
                    String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
                    if (auth != null && auth.startsWith("Bearer ")) {
                        String token = auth.substring(7);
                        var claims = jwtService.parse(token);

                        Long userId = Long.valueOf(claims.getSubject());
                        var user = userRepository.findById(userId).orElse(null);

                        if (user != null) {
                            var authorities = Arrays.stream(user.getRoles().split(","))
                                    .map(String::trim)
                                    .filter(s -> !s.isBlank())
                                    .map(SimpleGrantedAuthority::new)
                                    .toList();

                            var authentication = new UsernamePasswordAuthenticationToken(
                                    user.getEmail(), null, authorities
                            );
                            org.springframework.security.core.context.SecurityContextHolder.getContext()
                                    .setAuthentication(authentication);
                        }
                    }
                    chain.doFilter(request, response);
                } catch (JwtException | IllegalArgumentException e) {
                    // Token zły / wygasły / niezgodny z issuerem -> traktuj jak niezalogowany.
                    log.debug("JWT rejected: {}", e.getMessage());
                    chain.doFilter(request, response);
                }

            }
        };
    }
}
