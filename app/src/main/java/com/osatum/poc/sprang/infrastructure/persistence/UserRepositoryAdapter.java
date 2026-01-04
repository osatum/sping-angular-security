package com.osatum.poc.sprang.infrastructure.persistence;

import com.osatum.poc.sprang.domain.user.AppUser;
import com.osatum.poc.sprang.domain.user.UserRepository;
import com.osatum.poc.sprang.infrastructure.persistence.springdata.UserSpringDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserSpringDataRepository springData;

    @Override
    public Optional<AppUser> findById(Long id) {
        return springData.findById(id);
    }

    @Override
    public Optional<AppUser> findByEmail(String email) {
        return springData.findByEmail(email);
    }

    @Override
    public AppUser save(AppUser user) {
        return springData.save(user);
    }
}

