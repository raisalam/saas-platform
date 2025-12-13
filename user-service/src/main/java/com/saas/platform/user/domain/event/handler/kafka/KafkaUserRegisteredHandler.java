package com.saas.platform.user.domain.event.handler.kafka;

import com.saas.platform.common.events.DomainEventHandler;
import com.saas.platform.common.kafka.KafkaPublisher;
import com.saas.platform.common.kafka.events.KafkaEvent;
import com.saas.platform.user.domain.event.key.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "saas.common.kafka", name = "enabled", havingValue = "true")
public class KafkaUserRegisteredHandler implements DomainEventHandler<UserRegisteredEvent> {

    private final KafkaPublisher kafkaPublisher;
    private final ObjectMapper mapper;

    @Override
    public Class<UserRegisteredEvent> eventType() {
        return UserRegisteredEvent.class;
    }

    @Override
    public void handle(UserRegisteredEvent user) {
        log.debug("KafkaUserRegisteredHandler :: handle :: {} - {} - {}", user.getUserId(), user.getFullName(), user.getAndroidId());

        kafkaPublisher.publishAsync(KafkaEvent.builder()
                .tenantId("T1001")
                .aggerateId(user.getUserId().toString())
                .correlationId("sasas")
                .payload(mapper.writeValueAsString(user)).build());
    }
}
