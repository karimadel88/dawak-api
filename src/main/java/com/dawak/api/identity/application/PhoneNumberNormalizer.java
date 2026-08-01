package com.dawak.api.identity.application;

import com.dawak.api.common.api.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class PhoneNumberNormalizer {
    public String normalizeEgyptian(String input) {
        if (input == null) throw invalid();
        String compact = input.replaceAll("[\\s()-]", "");
        String national;
        if (compact.startsWith("+20")) national = compact.substring(3);
        else if (compact.startsWith("20")) national = compact.substring(2);
        else if (compact.startsWith("0")) national = compact.substring(1);
        else national = compact;
        if (!national.matches("1[0125]\\d{8}")) throw invalid();
        return "+20" + national;
    }

    private ApiException invalid() {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PHONE_NUMBER", "Enter a valid Egyptian mobile number.");
    }
}
