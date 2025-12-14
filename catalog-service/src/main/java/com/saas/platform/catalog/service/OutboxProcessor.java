package com.saas.platform.catalog.service;

import com.saas.platform.catalog.entity.OutboxEvent;
import com.saas.platform.catalog.repository.OutboxEventRepository;
import com.saas.platform.common.kafka.KafkaPublisher;
import com.saas.platform.common.kafka.events.KafkaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxProcessor {

    private static final int MAX_RETRIES = 10;

    private final KafkaPublisher kafkaPublisher;
    private final OutboxEventService outboxEventService;


    @Transactional(value = "transactionManager") // 👈 Specify the bean name
    public void processEvent(OutboxEvent event) {

        try {
            kafkaPublisher.publishAsync(
                    KafkaEvent.builder()
                            .tenantId(event.getTenantId())
                            .eventType(event.getEventType())
                            .correlationId(event.getEventCorrelationId())
                            .aggerateId(event.getAggregateId())
                            .payload(event.getPayload())
                            .build()
            );

            outboxEventService.markSuccess(event);


        } catch (Exception ex) {
            outboxEventService.markFailure(event, ex);
        }

    }
}
