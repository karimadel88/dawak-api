package com.dawak.api.patient.persistence;

import com.dawak.api.patient.domain.ConsentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConsentRecordRepository extends JpaRepository<ConsentRecord, UUID> {
    List<ConsentRecord> findAllByUserIdOrderByGrantedAtDesc(UUID userId);
}
