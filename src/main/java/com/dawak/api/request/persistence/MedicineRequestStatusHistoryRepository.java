package com.dawak.api.request.persistence;

import com.dawak.api.request.domain.MedicineRequestStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MedicineRequestStatusHistoryRepository extends JpaRepository<MedicineRequestStatusHistory, UUID> {}
