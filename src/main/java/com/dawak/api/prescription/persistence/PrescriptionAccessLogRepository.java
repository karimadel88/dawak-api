package com.dawak.api.prescription.persistence;

import com.dawak.api.prescription.domain.PrescriptionAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PrescriptionAccessLogRepository extends JpaRepository<PrescriptionAccessLog, UUID> {}
