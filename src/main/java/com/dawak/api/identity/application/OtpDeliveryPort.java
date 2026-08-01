package com.dawak.api.identity.application;

public interface OtpDeliveryPort {
    void deliver(String phoneNumber, String code, long expiresInSeconds);
}
