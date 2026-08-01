package com.dawak.api.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpLoggingFilter extends OncePerRequestFilter {
    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String REQUEST_ID_MDC_KEY = "requestId";
    private static final Pattern SAFE_REQUEST_ID = Pattern.compile("[A-Za-z0-9._-]{1,100}");
    private static final Logger log = LoggerFactory.getLogger(HttpLoggingFilter.class);

    private final HttpLoggingProperties properties;
    private final SensitiveDataSanitizer sanitizer;

    public HttpLoggingFilter(HttpLoggingProperties properties, SensitiveDataSanitizer sanitizer) {
        this.properties = properties;
        this.sanitizer = sanitizer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestId = resolveRequestId(request.getHeader(REQUEST_ID_HEADER));
        var cachedRequest = new ContentCachingRequestWrapper(request, properties.maxPayloadLength());
        var cachedResponse = new ContentCachingResponseWrapper(response);
        long startedAt = System.nanoTime();

        MDC.put(REQUEST_ID_MDC_KEY, requestId);
        cachedResponse.setHeader(REQUEST_ID_HEADER, requestId);
        try {
            chain.doFilter(cachedRequest, cachedResponse);
        } finally {
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
            logExchange(cachedRequest, cachedResponse, requestId, durationMs);
            cachedResponse.copyBodyToResponse();
            MDC.remove(REQUEST_ID_MDC_KEY);
        }
    }

    private void logExchange(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response,
                             String requestId, long durationMs) {
        String requestBody = body(request.getContentType(), request.getContentAsByteArray());
        String responseBody = body(response.getContentType(), response.getContentAsByteArray());
        log.info("HTTP exchange requestId={} method={} path={} status={} durationMs={} requestBody={} responseBody={}",
                requestId, request.getMethod(), request.getRequestURI(), response.getStatus(), durationMs,
                requestBody, responseBody);
    }

    private String body(String contentType, byte[] content) {
        if (!properties.includeBodies()) {
            return "[DISABLED]";
        }
        if (content.length == 0) {
            return "";
        }
        if (!isJson(contentType)) {
            return "[NON-JSON PAYLOAD OMITTED]";
        }
        return sanitizer.sanitizeJson(content, properties.maxPayloadLength());
    }

    private boolean isJson(String contentType) {
        if (contentType == null) {
            return false;
        }
        try {
            MediaType mediaType = MediaType.parseMediaType(contentType);
            return MediaType.APPLICATION_JSON.includes(mediaType) || mediaType.getSubtype().endsWith("+json");
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private String resolveRequestId(String supplied) {
        return supplied != null && SAFE_REQUEST_ID.matcher(supplied).matches()
                ? supplied
                : UUID.randomUUID().toString();
    }
}
