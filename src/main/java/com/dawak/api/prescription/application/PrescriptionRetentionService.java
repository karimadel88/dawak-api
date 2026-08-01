package com.dawak.api.prescription.application;

import com.dawak.api.audit.application.AuditService;
import com.dawak.api.common.web.RequestMetadata;
import com.dawak.api.prescription.domain.PrescriptionStatus;
import com.dawak.api.prescription.persistence.PrescriptionAccessGrantRepository;
import com.dawak.api.prescription.persistence.PrescriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class PrescriptionRetentionService {
    private static final Logger log = LoggerFactory.getLogger(PrescriptionRetentionService.class);
    private static final RequestMetadata SYSTEM_METADATA = new RequestMetadata(null, null);
    private final PrescriptionRepository prescriptions;
    private final PrescriptionAccessGrantRepository grants;
    private final PrescriptionStorage storage;
    private final AuditService audit;

    public PrescriptionRetentionService(PrescriptionRepository prescriptions,
                                        PrescriptionAccessGrantRepository grants,
                                        PrescriptionStorage storage, AuditService audit) {
        this.prescriptions = prescriptions; this.grants = grants; this.storage = storage; this.audit = audit;
    }

    @Scheduled(cron = "${dawak.prescription.retention-cleanup-cron}")
    @Transactional
    public void cleanExpiredData() { cleanExpiredData(Instant.now()); }

    @Transactional
    public CleanupResult cleanExpiredData(Instant cutoff) {
        long expiredGrants = grants.deleteByExpiresAtBefore(cutoff);
        var expired = prescriptions.findTop100ByRetentionUntilBeforeAndStatusNotOrderByRetentionUntilAsc(
                cutoff, PrescriptionStatus.DELETED);
        for (var prescription : expired) {
            String storageKey = prescription.getStorageKey();
            storage.delete(storageKey);
            grants.deleteByPrescriptionId(prescription.getId());
            prescription.deleteForRetention(cutoff);
            audit.record(null, "PRESCRIPTION_RETENTION_DELETED", "PRESCRIPTION", prescription.getId(),
                    "SUCCESS", SYSTEM_METADATA, "retentionUntil=" + prescription.getRetentionUntil());
        }
        if (expiredGrants > 0 || !expired.isEmpty()) {
            log.info("Prescription retention cleanup completed deletedPrescriptions={} deletedAccessGrants={}",
                    expired.size(), expiredGrants);
        }
        return new CleanupResult(expired.size(), expiredGrants);
    }

    public record CleanupResult(int deletedPrescriptions, long deletedAccessGrants) {}
}
