package com.dawak.api.prescription.persistence;

import com.dawak.api.prescription.domain.PrescriptionAccessGrant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.time.Instant;
import java.util.UUID;

public interface PrescriptionAccessGrantRepository extends JpaRepository<PrescriptionAccessGrant, UUID> {
    Optional<PrescriptionAccessGrant> findByTokenHash(String tokenHash);
    long deleteByExpiresAtBefore(Instant cutoff);
    long deleteByPrescriptionId(UUID prescriptionId);
}
