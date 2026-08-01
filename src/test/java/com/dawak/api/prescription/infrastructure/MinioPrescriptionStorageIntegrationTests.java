package com.dawak.api.prescription.infrastructure;

import com.dawak.api.common.api.ApiException;
import com.dawak.api.prescription.config.PrescriptionProperties;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.containers.wait.strategy.Wait;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers(disabledWithoutDocker = true)
class MinioPrescriptionStorageIntegrationTests {
    private static final String ACCESS_KEY = "test_minio";
    private static final String SECRET_KEY = "test_minio_secret";

    @Container
    static final GenericContainer<?> MINIO = new GenericContainer<>(
            DockerImageName.parse("quay.io/minio/minio:RELEASE.2025-04-22T22-12-26Z"))
            .withEnv("MINIO_ROOT_USER", ACCESS_KEY)
            .withEnv("MINIO_ROOT_PASSWORD", SECRET_KEY)
            .withCommand("server", "/data")
            .withExposedPorts(9000)
            .waitingFor(Wait.forHttp("/minio/health/live").forPort(9000));

    @Test
    void storesOnlyEncryptedContentAndSupportsReadAndDelete() throws Exception {
        String endpoint = "http://" + MINIO.getHost() + ":" + MINIO.getMappedPort(9000);
        var properties = properties(endpoint);
        var storage = new MinioPrescriptionStorage(properties, new PrescriptionEncryption(properties));
        storage.verifyBucket();
        byte[] plaintext = "%PDF-1.7 private prescription".getBytes(StandardCharsets.US_ASCII);

        storage.write("test-object", plaintext);
        assertThat(storage.read("test-object")).isEqualTo(plaintext);

        var rawClient = MinioClient.builder().endpoint(endpoint).credentials(ACCESS_KEY, SECRET_KEY).build();
        byte[] raw;
        try (var input = rawClient.getObject(GetObjectArgs.builder().bucket("prescription-test")
                .object("quarantine/test-object.bin").build())) {
            raw = input.readAllBytes();
        }
        assertThat(raw).isNotEqualTo(plaintext);
        assertThat(new String(raw, StandardCharsets.ISO_8859_1)).doesNotContain("private prescription");

        storage.delete("test-object");
        assertThatThrownBy(() -> storage.read("test-object")).isInstanceOf(ApiException.class);
    }

    private PrescriptionProperties properties(String endpoint) {
        return new PrescriptionProperties(10_485_760, Duration.ofMinutes(15), Duration.ofMinutes(2),
                Duration.ofDays(365), "/tmp/unused", "integration-encryption-secret", "minio", "local",
                endpoint, ACCESS_KEY, SECRET_KEY, "prescription-test", true,
                "localhost", 3310, Duration.ofSeconds(1), Duration.ofSeconds(2));
    }
}
