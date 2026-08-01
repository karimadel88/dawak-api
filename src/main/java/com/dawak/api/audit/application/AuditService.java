package com.dawak.api.audit.application;

import com.dawak.api.audit.domain.AuditEvent;
import com.dawak.api.audit.persistence.AuditEventRepository;
import com.dawak.api.common.web.RequestMetadata;
import com.dawak.api.identity.domain.User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuditService {
    private final AuditEventRepository repository;

    public AuditService(AuditEventRepository repository) { this.repository = repository; }

    public void record(User actor, String type, String aggregateType, UUID aggregateId,
                       String outcome, RequestMetadata metadata, String details) {
        repository.save(new AuditEvent(actor, type, aggregateType, aggregateId, outcome,
                metadata.ipAddress(), metadata.userAgent(), details, Instant.now()));
    }
}
