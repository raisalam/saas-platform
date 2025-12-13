package com.saas.platform.user.service;

import com.saas.platform.db.TenantContext;
import com.saas.platform.user.entity.ProcessedEvent;
import com.saas.platform.user.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final ProcessedEventRepository processedEventRepository;
    
    // NOTE: This service must use the same Transaction Manager as the UserService/Kafka Listener

    /**
     * Checks if the event has already been processed based on the unique correlation ID.
     * * @param correlationId The unique ID from the event header.
     * @param tenantId The current tenant ID.
     * @param consumerGroup The name of the consumer processing this event (e.g., "BALANCE_DEDUCTION").
     * @return true if the event has been processed, false otherwise.
     */
    // This check can run without an active transaction, but it's safer to include it 
    // within the transactional listener method.
    @Transactional(value = "tenantTransactionManager", propagation = Propagation.REQUIRED)
    public boolean isProcessed(String correlationId, String tenantId, String consumerGroup) {
        System.out.println("Is processing start");
        return processedEventRepository.findByCorrelationIdAndTenantIdAndConsumerGroup(
            correlationId, tenantId, consumerGroup
        ).isPresent();
    }

    /**
     * Marks the event as processed. This must be called inside the main Kafka listener 
     * transaction to ensure atomicity with the balance update.
     * * @param correlationId The unique ID from the event header.
     * @param tenantId The current tenant ID.
     * @param consumerGroup The name of the consumer processing this event.
     */
    // The @Transactional annotation is redundant here if called from a transactional listener,
    // but ensures that the INSERT is rolled back if the balance update fails.
    // The listener method should handle the primary transaction boundary.
    @Transactional(value = "tenantTransactionManager")
    public void markAsProcessed(String correlationId, String tenantId, String consumerGroup, String eventType) {
        ProcessedEvent event = ProcessedEvent.builder()
            .correlationId(correlationId)
            .tenantId(tenantId)
            .consumerGroup(consumerGroup)
            .eventType(eventType) // Or pass eventType as a parameter
            .processedAt(LocalDateTime.now())
            .build();
        
        try {
            processedEventRepository.save(event);
            log.info("Successfully marked event {} as processed.", correlationId);
        } catch (Exception e) {
            e.printStackTrace();
            // This shouldn't happen if the isProcessed() check is done first, 
            // but acts as a final fail-safe for race conditions.
            log.warn("Race condition detected exception: Event {} was already processed. Ignoring save.", correlationId);
            // Re-throw if the caller needs to explicitly handle the failure or allow rollback
            // throw new RuntimeException("Idempotency violation.", e);
        }
    }
}