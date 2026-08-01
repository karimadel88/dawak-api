package com.dawak.api.identity.infrastructure;

import com.dawak.api.common.api.ApiException;
import com.dawak.api.identity.application.OtpDeliveryPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "dawak.auth.otp-delivery", havingValue = "disabled", matchIfMissing = true)
public class DisabledOtpDeliveryAdapter implements OtpDeliveryPort {
    @Override
    public void deliver(String phoneNumber, String code, long expiresInSeconds) {
        throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "OTP_DELIVERY_UNAVAILABLE",
                "Verification delivery is temporarily unavailable.");
    }
}
