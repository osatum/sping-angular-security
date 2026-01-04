package com.osatum.poc.sprang.domain.user;

import java.util.Optional;

public interface UserRepository {
    Optional<AppUser> findById(Long id);
    Optional<AppUser> findByEmail(String email);
    AppUser save(AppUser user);
}
