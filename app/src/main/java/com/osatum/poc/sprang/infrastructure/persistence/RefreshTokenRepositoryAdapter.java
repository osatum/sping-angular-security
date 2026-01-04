package com.osatum.poc.sprang.infrastructure.persistence;

import com.osatum.poc.sprang.domain.auth.RefreshToken;
import com.osatum.poc.sprang.domain.auth.RefreshTokenRepository;
import com.osatum.poc.sprang.infrastructure.persistence.springdata.RefreshTokenSpringDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenSpringDataRepository springData;

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return springData.findByTokenHash(tokenHash);
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        return springData.save(token);
    }
}
