package com.dawak.api.prescription.persistence;

import com.dawak.api.prescription.domain.Prescription;
import com.dawak.api.prescription.domain.PrescriptionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {
    List<Prescription> findByStatusAndReviewerIsNullOrderByCreatedAtAsc(PrescriptionStatus status);
    List<Prescription> findTop100ByRetentionUntilBeforeAndStatusNotOrderByRetentionUntilAsc(
            java.time.Instant retentionUntil, PrescriptionStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Prescription p where p.id = :id")
    Optional<Prescription> findByIdForUpdate(@Param("id") UUID id);
}
