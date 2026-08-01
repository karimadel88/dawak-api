package com.dawak.api.common.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dawak.logging.http")
public record HttpLoggingProperties(boolean includeBodies, int maxPayloadLength) {
    public HttpLoggingProperties {
        if (maxPayloadLength <= 0) {
            maxPayloadLength = 8192;
        }
    }
}
