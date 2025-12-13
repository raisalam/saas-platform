package com.saas.platform.catalog.domain.event.handler.kafka;

import com.saas.platform.common.events.DomainEventHandler;
import com.saas.platform.common.kafka.KafkaPublisher;
import com.saas.platform.catalog.domain.event.key.KeyUsedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "saas.common.kafka", name = "enabled", havingValue = "true")
public class KafkaKeyUsedHandler implements DomainEventHandler<KeyUsedEvent> {

    private final KafkaPublisher kafkaPublisher;

    @Override
    public Class<KeyUsedEvent> eventType() {
        return KeyUsedEvent.class;
    }

    @Override
    public void handle(KeyUsedEvent user) {
        log.debug("KafkaKeyUsedHandler :: handle :: {} - {} - {}", user.getUserId(), user.getFullName(), user.getAndroidId());


    }
}
