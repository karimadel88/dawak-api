package com.dawak.api.prescription.application;

import com.dawak.api.common.api.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class PrescriptionFileInspector {
    private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};

    private final MalwareScanner malwareScanner;

    public PrescriptionFileInspector(MalwareScanner malwareScanner) { this.malwareScanner = malwareScanner; }

    public String inspect(byte[] content, String declaredType, String expectedChecksum) {
        String actualChecksum = sha256(content);
        if (!MessageDigest.isEqual(actualChecksum.getBytes(StandardCharsets.US_ASCII),
                expectedChecksum.getBytes(StandardCharsets.US_ASCII))) {
            throw invalid("PRESCRIPTION_CHECKSUM_MISMATCH", "Uploaded content checksum does not match the upload intent.");
        }
        String detected = detect(content);
        if (!detected.equals(declaredType)) {
            throw invalid("PRESCRIPTION_FILE_SIGNATURE_INVALID", "File signature does not match its declared content type.");
        }
        MalwareScanner.ScanResult scan = malwareScanner.scan(content);
        if (!scan.clean()) throw invalid("PRESCRIPTION_MALWARE_DETECTED",
                "The uploaded file failed malware scanning: " + scan.signature());
        return detected;
    }

    public static String sha256(byte[] value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value)); }
        catch (Exception exception) { throw new IllegalStateException(exception); }
    }

    private String detect(byte[] content) {
        if (startsWith(content, "%PDF-".getBytes(StandardCharsets.US_ASCII))) return "application/pdf";
        if (content.length >= 3 && (content[0] & 0xff) == 0xff && (content[1] & 0xff) == 0xd8 && (content[2] & 0xff) == 0xff)
            return "image/jpeg";
        if (startsWith(content, PNG)) return "image/png";
        throw invalid("PRESCRIPTION_FILE_SIGNATURE_INVALID", "Only genuine PDF, JPEG, and PNG files are supported.");
    }

    private boolean startsWith(byte[] content, byte[] signature) {
        if (content.length < signature.length) return false;
        for (int i = 0; i < signature.length; i++) if (content[i] != signature[i]) return false;
        return true;
    }

    private ApiException invalid(String code, String message) {
        return new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, code, message);
    }
}
