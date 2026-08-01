package com.dawak.api.patient.persistence;

import com.dawak.api.patient.domain.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface CityRepository extends JpaRepository<City, UUID> {
    Optional<City> findByIdAndActiveTrue(UUID id);
    List<City> findAllByActiveTrueOrderByNameEn();
}
