package com.saas.platform.catalog.domain.event.handler.kafka;

import com.saas.platform.catalog.dto.KeyItemResponse;
import com.saas.platform.catalog.entity.OutboxEvent;
import com.saas.platform.catalog.repository.OutboxEventRepository;
import com.saas.platform.catalog.service.OutboxEventService;
import com.saas.platform.common.events.DomainEventHandler;
import com.saas.platform.common.kafka.KafkaPublisher;
import com.saas.platform.common.kafka.events.KafkaEvent;
import com.saas.platform.catalog.domain.event.key.KeyGeneratedEvent;
import com.saas.platform.db.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDateTime;

@Component
@Order(3) //
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "saas.common.kafka", name = "enabled", havingValue = "true")
public class KafkaKeyGeneratedHandler implements DomainEventHandler<KeyGeneratedEvent> {

    private final KafkaPublisher kafkaPublisher;

    private final OutboxEventService outboxEventService;
    private final ObjectMapper mapper;

    @Override
    public Class<KeyGeneratedEvent> eventType() {
        return KeyGeneratedEvent.class;
    }

    @Override
    public void handle(KeyGeneratedEvent event) {
        log.debug("KafkaKeyGeneratedHandler :: handle :: {} - {}", event.getUserId(), event.getBalance());
        TenantContext.setTenantId(event.getTenantId());
        TenantContext.setMicroservice("catalog");
        for (OutboxEvent pending : outboxEventService.getEvents("PENDING")) {
            kafkaPublisher.publishAsync(KafkaEvent.builder()
                    .tenantId(pending.getTenantId())
                    .aggerateId(pending.getAggregateId())
                    .correlationId(pending.getEventCorrelationId())
                            .eventType("KeyGenerated")
                    .payload(pending.getPayload()).build());
            pending.setStatus("PUBLISHED");
            pending.setPublishedAt(LocalDateTime.now());
            pending.setEventType("KeyGenerated");
            outboxEventService.markPublished(pending.getId());

        }
    }
}
