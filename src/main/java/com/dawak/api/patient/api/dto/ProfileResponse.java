package com.dawak.api.patient.api.dto;

import java.time.Instant;
import java.util.UUID;

public record ProfileResponse(
        UUID id,
        String phoneNumber,
        String firstName,
        String lastName,
        String email,
        Integer birthYear,
        String preferredLanguage,
        UUID cityId,
        String cityNameAr,
        String cityNameEn,
        UUID areaId,
        String areaNameAr,
        String areaNameEn,
        Instant updatedAt
) {
}
