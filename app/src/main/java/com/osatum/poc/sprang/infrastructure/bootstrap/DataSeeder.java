package com.osatum.poc.sprang.infrastructure.bootstrap;

import com.osatum.poc.sprang.domain.user.AppUser;
import com.osatum.poc.sprang.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        userRepository.findByEmail("user@test.com").orElseGet(() ->
                userRepository.save(AppUser.builder()
                        .email("user@test.com")
                        .passwordHash(passwordEncoder.encode("password"))
                        .roles("ROLE_USER")
                        .build())
        );

        userRepository.findByEmail("admin@test.com").orElseGet(() ->
                userRepository.save(AppUser.builder()
                        .email("admin@test.com")
                        .passwordHash(passwordEncoder.encode("password"))
                        .roles("ROLE_USER,ROLE_ADMIN")
                        .build())
        );
    }
}
