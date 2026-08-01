package com.dawak.api.patient.api;

import com.dawak.api.common.api.ApiException;
import com.dawak.api.patient.api.dto.LocationResponse;
import com.dawak.api.patient.persistence.AreaRepository;
import com.dawak.api.patient.persistence.CityRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/locations")
public class LocationController {
    private final CityRepository cities;
    private final AreaRepository areas;

    public LocationController(CityRepository cities, AreaRepository areas) {
        this.cities = cities;
        this.areas = areas;
    }

    @GetMapping("/cities")
    @Transactional(readOnly = true)
    List<LocationResponse> cities() {
        return cities.findAllByActiveTrueOrderByNameEn().stream()
                .map(city -> new LocationResponse(city.getId(), city.getCode(), city.getNameAr(), city.getNameEn()))
                .toList();
    }

    @GetMapping("/cities/{cityId}/areas")
    @Transactional(readOnly = true)
    List<LocationResponse> areas(@PathVariable UUID cityId) {
        if (cities.findByIdAndActiveTrue(cityId).isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "CITY_NOT_FOUND", "Supported city not found.");
        }
        return areas.findAllByCityIdAndActiveTrueOrderByNameEn(cityId).stream()
                .map(area -> new LocationResponse(area.getId(), area.getCode(), area.getNameAr(), area.getNameEn()))
                .toList();
    }
}
