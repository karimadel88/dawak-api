package com.dawak.api.prescription.infrastructure;

import com.dawak.api.common.api.ApiException;
import com.dawak.api.prescription.application.PrescriptionStorage;
import com.dawak.api.prescription.config.PrescriptionProperties;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;

@Component
@ConditionalOnProperty(prefix = "dawak.prescription", name = "storage-type", havingValue = "minio")
public class MinioPrescriptionStorage implements PrescriptionStorage {
    private final MinioClient client;
    private final PrescriptionEncryption encryption;
    private final String bucket;
    private final boolean createBucket;

    public MinioPrescriptionStorage(PrescriptionProperties properties, PrescriptionEncryption encryption) {
        this.client = MinioClient.builder().endpoint(properties.minioEndpoint())
                .credentials(properties.minioAccessKey(), properties.minioSecretKey()).build();
        this.bucket = properties.minioBucket();
        this.createBucket = properties.minioCreateBucket();
        this.encryption = encryption;
    }

    @PostConstruct
    void verifyBucket() {
        try {
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists && createBucket) client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            else if (!exists) throw new IllegalStateException("Prescription bucket does not exist: " + bucket);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot initialize private MinIO prescription bucket", exception);
        }
    }

    @Override
    public void write(String key, byte[] content) {
        try {
            byte[] encrypted = encryption.encrypt(content);
            client.putObject(PutObjectArgs.builder().bucket(bucket).object(objectName(key))
                    .stream(new ByteArrayInputStream(encrypted), encrypted.length, -1)
                    .contentType("application/octet-stream").build());
        } catch (Exception exception) { throw unavailable("Could not store prescription", exception); }
    }

    @Override
    public byte[] read(String key) {
        try (var input = client.getObject(GetObjectArgs.builder().bucket(bucket).object(objectName(key)).build())) {
            return encryption.decrypt(input.readAllBytes());
        } catch (Exception exception) { throw unavailable("Could not read prescription", exception); }
    }

    @Override
    public void delete(String key) {
        try { client.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectName(key)).build()); }
        catch (Exception exception) { throw unavailable("Could not delete prescription", exception); }
    }

    private String objectName(String key) { return "quarantine/" + key + ".bin"; }
    private ApiException unavailable(String message, Exception cause) {
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "PRESCRIPTION_STORAGE_UNAVAILABLE", message);
    }
}
