package com.saas.platform.catalog.domain.event.handler.kafka;

import com.saas.platform.catalog.dto.KeyItemResponse;
import com.saas.platform.catalog.entity.OutboxEvent;
import com.saas.platform.catalog.repository.OutboxEventRepository;
import com.saas.platform.catalog.service.OutboxEventService;
import com.saas.platform.catalog.service.OutboxProcessor;
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
import java.util.List;

@Component
@Order(3) //
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "saas.common.kafka", name = "enabled", havingValue = "true")
public class KafkaKeyGeneratedHandler implements DomainEventHandler<KeyGeneratedEvent> {
    private final OutboxEventRepository outboxRepository;
    private final OutboxProcessor processor;
    private final OutboxEventService outboxEventService;
    private final ObjectMapper mapper;

    @Override
    public Class<KeyGeneratedEvent> eventType() {
        return KeyGeneratedEvent.class;
    }

    @Override
    public void handle(KeyGeneratedEvent localEvent) {
        log.debug("KafkaKeyGeneratedHandler :: handle :: {} - {}", localEvent.getUserId(), localEvent.getBalance());
        TenantContext.setTenantId(localEvent.getTenantId());
        TenantContext.setMicroservice("catalog");
        List<OutboxEvent> events =
                outboxEventService.lockAndFetch(50);

        for (OutboxEvent event : events) {
            processor.processEvent(event);
        }
    }
}
