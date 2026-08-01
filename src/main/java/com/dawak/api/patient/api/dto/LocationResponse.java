package com.dawak.api.patient.api.dto;

import java.util.UUID;

public record LocationResponse(UUID id, String code, String nameAr, String nameEn) {
}
