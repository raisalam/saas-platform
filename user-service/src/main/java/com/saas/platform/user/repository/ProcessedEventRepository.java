package com.saas.platform.user.repository;

import com.saas.platform.user.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {

    /**
     * Checks if an event with the given correlationId has been processed 
     * for a specific tenant and consumer group.
     */
    Optional<ProcessedEvent> findByCorrelationIdAndTenantIdAndConsumerGroup(
        String correlationId, 
        String tenantId, 
        String consumerGroup
    );
}