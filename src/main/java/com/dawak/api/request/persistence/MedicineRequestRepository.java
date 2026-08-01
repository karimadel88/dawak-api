package com.dawak.api.request.persistence;

import com.dawak.api.request.domain.MedicineRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;
import com.dawak.api.request.domain.MedicineRequestStatus;

public interface MedicineRequestRepository extends JpaRepository<MedicineRequest, UUID> {
    List<MedicineRequest> findByPatientProfileUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<MedicineRequest> findByIdAndPatientProfileUserId(UUID id, UUID userId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from MedicineRequest r where r.id=:id and r.patientProfile.user.id=:userId")
    Optional<MedicineRequest> findOwnedByIdForUpdate(@Param("id") UUID id, @Param("userId") UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from MedicineRequest r where r.expiresAt <= :cutoff and r.status in :statuses order by r.expiresAt")
    List<MedicineRequest> findExpiredForUpdate(@Param("cutoff") Instant cutoff,
                                               @Param("statuses") List<MedicineRequestStatus> statuses);
}
