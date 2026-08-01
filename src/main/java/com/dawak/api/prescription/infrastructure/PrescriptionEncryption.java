package com.dawak.api.prescription.infrastructure;

import com.dawak.api.prescription.config.PrescriptionProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

@Component
public class PrescriptionEncryption {
    private static final int NONCE_LENGTH = 12;
    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    public PrescriptionEncryption(PrescriptionProperties properties) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(properties.encryptionSecret().getBytes(StandardCharsets.UTF_8));
            this.key = new SecretKeySpec(hash, "AES");
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot initialize prescription encryption", exception);
        }
    }

    public byte[] encrypt(byte[] content) {
        try {
            byte[] nonce = new byte[NONCE_LENGTH];
            random.nextBytes(nonce);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, nonce));
            byte[] encrypted = cipher.doFinal(content);
            return ByteBuffer.allocate(nonce.length + encrypted.length).put(nonce).put(encrypted).array();
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot encrypt prescription", exception);
        }
    }

    public byte[] decrypt(byte[] envelope) {
        try {
            if (envelope.length <= NONCE_LENGTH) throw new IllegalArgumentException("Invalid encrypted object");
            byte[] nonce = new byte[NONCE_LENGTH];
            byte[] encrypted = new byte[envelope.length - NONCE_LENGTH];
            System.arraycopy(envelope, 0, nonce, 0, NONCE_LENGTH);
            System.arraycopy(envelope, NONCE_LENGTH, encrypted, 0, encrypted.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, nonce));
            return cipher.doFinal(encrypted);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot decrypt prescription", exception);
        }
    }
}
