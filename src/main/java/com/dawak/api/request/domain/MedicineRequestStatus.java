package com.dawak.api.request.domain;

public enum MedicineRequestStatus {
    DRAFT,
    SUBMITTED,
    AWAITING_PRESCRIPTION,
    PENDING_PRESCRIPTION_REVIEW,
    READY_FOR_MATCHING,
    MATCHING,
    OFFERS_AVAILABLE,
    OFFER_SELECTED,
    UNFULFILLED,
    EXPIRED,
    CANCELLED,
    COMPLETED
}
