package com.dawak.api.identity.persistence;

import com.dawak.api.identity.domain.OtpChallenge;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, UUID> {
    long countByPhoneNumberAndCreatedAtAfter(String phoneNumber, Instant createdAfter);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OtpChallenge o where o.id = :id")
    Optional<OtpChallenge> findByIdForUpdate(@Param("id") UUID id);
}
