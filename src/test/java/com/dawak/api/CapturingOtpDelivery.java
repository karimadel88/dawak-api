package com.dawak.api;

import com.dawak.api.identity.application.OtpDeliveryPort;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class CapturingOtpDelivery implements OtpDeliveryPort {
    private final Map<String, String> codes = new ConcurrentHashMap<>();

    @Override
    public void deliver(String phoneNumber, String code, long expiresInSeconds) {
        codes.put(phoneNumber, code);
    }

    String codeFor(String phoneNumber) {
        return codes.get(phoneNumber);
    }
}
