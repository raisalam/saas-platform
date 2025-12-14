package com.saas.platform.catalog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Aggregate Context (For Tracing/Ordering)
    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType; // e.g., "Seller", "CatalogItem"

    @Column(name = "aggregate_id", nullable = false, length = 100)
    private String aggregateId;   // The specific ID of the entity (e.g., Seller ID)

    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    // Event Details
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;     // e.g., "KeyGenerated"

    @Column(name = "topic", nullable = false, length = 100)
    private String topic;         // Destination topic/queue name (e.g., "balance-deduction")

    @Column(name = "event_correlation_id", nullable = false, unique = true, length = 36)
    private String eventCorrelationId; // UUID for consumer deduplication

    // Payload (Use @Lob for large JSON payloads)
    @Lob
    @Column(name = "payload", nullable = false)
    private String payload;       // JSON-serialized event data

    // Processing State
    @Column(name = "status", nullable = false, length = 20)
    private String status;        // PENDING, SENT, FAILED

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "last_error", length = 500)
    private String lastError;     // Error message from the last failed attempt

    // Timestamps
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt; // When it was successfully SENT

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;


    @Version
    private Long version;
    public void markForRetry(String error, int backoffSeconds) {
        this.retryCount++;
        this.lastError = error;
        this.nextRetryAt = LocalDateTime.now().plusSeconds(backoffSeconds);
        this.status = "PENDING";
    }

    public void markFailed(String error) {
        this.retryCount++;
        this.lastError = error;
        this.status = "FAILED";
    }

    public void markPublished() {
        this.status = "PUBLISHED";
        this.publishedAt = LocalDateTime.now();
    }
}