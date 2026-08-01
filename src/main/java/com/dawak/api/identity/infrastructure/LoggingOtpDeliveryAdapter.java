package com.dawak.api.identity.infrastructure;

import com.dawak.api.identity.application.OtpDeliveryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "dawak.auth.otp-delivery", havingValue = "log")
public class LoggingOtpDeliveryAdapter implements OtpDeliveryPort {
    private static final Logger log = LoggerFactory.getLogger(LoggingOtpDeliveryAdapter.class);

    @Override
    public void deliver(String phoneNumber, String code, long expiresInSeconds) {
        log.warn("LOCAL OTP delivery for {}: code={} expiresIn={}s. Configure a production SMS adapter before deployment.",
                mask(phoneNumber), code, expiresInSeconds);
    }

    private String mask(String phone) {
        return phone.length() < 5 ? "***" : phone.substring(0, 4) + "*****" + phone.substring(phone.length() - 2);
    }
}
