package com.dawak.api.patient.persistence;

import com.dawak.api.patient.domain.Area;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AreaRepository extends JpaRepository<Area, UUID> {
    Optional<Area> findByIdAndCityIdAndActiveTrue(UUID id, UUID cityId);
    List<Area> findAllByCityIdAndActiveTrueOrderByNameEn(UUID cityId);
}
