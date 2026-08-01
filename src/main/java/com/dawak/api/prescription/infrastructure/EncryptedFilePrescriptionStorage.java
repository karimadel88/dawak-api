package com.dawak.api.prescription.infrastructure;

import com.dawak.api.common.api.ApiException;
import com.dawak.api.prescription.application.PrescriptionStorage;
import com.dawak.api.prescription.config.PrescriptionProperties;
import org.springframework.http.HttpStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
@ConditionalOnProperty(prefix = "dawak.prescription", name = "storage-type", havingValue = "filesystem")
public class EncryptedFilePrescriptionStorage implements PrescriptionStorage {
    private final Path root;
    private final PrescriptionEncryption encryption;

    public EncryptedFilePrescriptionStorage(PrescriptionProperties properties, PrescriptionEncryption encryption) {
        try {
            this.root = Path.of(properties.storagePath()).toAbsolutePath().normalize();
            Files.createDirectories(root);
            this.encryption = encryption;
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot initialize private prescription storage", exception);
        }
    }

    @Override
    public void write(String storageKey, byte[] content) {
        try {
            Path target = resolve(storageKey);
            byte[] envelope = encryption.encrypt(content);
            Path temporary = Files.createTempFile(root, "upload-", ".tmp");
            Files.write(temporary, envelope);
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception exception) {
            throw storageFailure("Could not store prescription", exception);
        }
    }

    @Override
    public byte[] read(String storageKey) {
        try {
            return encryption.decrypt(Files.readAllBytes(resolve(storageKey)));
        } catch (Exception exception) {
            throw storageFailure("Could not read prescription", exception);
        }
    }

    @Override
    public void delete(String storageKey) {
        try { Files.deleteIfExists(resolve(storageKey)); }
        catch (Exception exception) { throw storageFailure("Could not delete prescription", exception); }
    }

    private Path resolve(String storageKey) {
        Path path = root.resolve(storageKey + ".bin").normalize();
        if (!path.getParent().equals(root)) throw new IllegalArgumentException("Invalid storage key");
        return path;
    }

    private ApiException storageFailure(String message, Exception cause) {
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "PRESCRIPTION_STORAGE_UNAVAILABLE", message, cause);
    }
}
