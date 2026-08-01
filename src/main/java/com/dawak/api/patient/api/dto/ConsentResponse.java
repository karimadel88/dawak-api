package com.dawak.api.patient.api.dto;

import com.dawak.api.patient.domain.ConsentStatusV1;
import com.dawak.api.patient.domain.ConsentTypeV1;

import java.time.Instant;
import java.util.UUID;

public record ConsentResponse(UUID id, ConsentTypeV1 type, String documentVersion,
                              ConsentStatusV1 status, Instant grantedAt) {
}
