package com.osatum.poc.sprang.infrastructure.persistence.springdata;

import com.osatum.poc.sprang.domain.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSpringDataRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
}
