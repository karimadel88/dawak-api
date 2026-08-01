package com.dawak.api.common.web;

import jakarta.servlet.http.HttpServletRequest;

public record RequestMetadata(String ipAddress, String userAgent) {
    public static RequestMetadata from(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        String ip = forwarded == null || forwarded.isBlank()
                ? request.getRemoteAddr()
                : forwarded.split(",", 2)[0].trim();
        return new RequestMetadata(truncate(ip, 64), truncate(request.getHeader("User-Agent"), 500));
    }

    private static String truncate(String value, int max) {
        if (value == null) return null;
        return value.length() <= max ? value : value.substring(0, max);
    }
}
