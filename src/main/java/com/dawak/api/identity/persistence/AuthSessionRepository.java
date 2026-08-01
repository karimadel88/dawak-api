package com.dawak.api.identity.persistence;

import com.dawak.api.identity.domain.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {
    Optional<AuthSession> findByRefreshTokenHash(String refreshTokenHash);
    Optional<AuthSession> findByUserIdAndDeviceIdAndRevokedAtIsNull(UUID userId, String deviceId);
    List<AuthSession> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<AuthSession> findByIdAndUserId(UUID id, UUID userId);
}
