package com.dawak.api.common.logging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Component
public class SensitiveDataSanitizer {
    static final String REDACTED = "[REDACTED]";

    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "authorization", "accesstoken", "refreshtoken", "token", "password", "secret",
            "otp", "challengeid", "phonenumber", "phone", "email", "firstname", "lastname",
            "birthyear", "ipaddress", "deviceid", "devicename"
    );

    private final ObjectMapper objectMapper;

    public SensitiveDataSanitizer() {
        this.objectMapper = JsonMapper.builder().build();
    }

    public String sanitizeJson(byte[] content, int maxPayloadLength) {
        if (content.length == 0) {
            return "";
        }
        if (content.length > maxPayloadLength) {
            return "[PAYLOAD OMITTED: " + content.length + " bytes]";
        }
        try {
            JsonNode root = objectMapper.readTree(content);
            redact(root);
            return objectMapper.writeValueAsString(root);
        } catch (Exception ignored) {
            return "[NON-JSON PAYLOAD OMITTED]";
        }
    }

    private void redact(JsonNode node) {
        if (node instanceof ObjectNode object) {
            object.properties().forEach(entry -> {
                if (isSensitive(entry.getKey(), entry.getValue())) {
                    object.put(entry.getKey(), REDACTED);
                } else {
                    redact(entry.getValue());
                }
            });
        } else if (node instanceof ArrayNode array) {
            array.forEach(this::redact);
        }
    }

    private boolean isSensitive(String fieldName, JsonNode value) {
        String normalized = fieldName.replace("_", "").replace("-", "").toLowerCase(Locale.ROOT);
        return (normalized.equals("code") && value.isTextual() && value.textValue().matches("\\d{4,10}"))
                || SENSITIVE_FIELDS.contains(normalized)
                || normalized.endsWith("password")
                || normalized.endsWith("secret")
                || normalized.endsWith("token");
    }
}
