package com.saas.platform.user.domain.event.handler.kafka;

import com.saas.platform.common.events.DomainEventHandler;
import com.saas.platform.common.kafka.KafkaPublisher;
import com.saas.platform.common.kafka.events.KafkaEvent;
import com.saas.platform.user.domain.event.key.UserLoggedInEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDateTime;

@Component
@Order(3) //
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "saas.common.kafka", name = "enabled", havingValue = "true")
public class KafkaUserLoggedInHandler implements DomainEventHandler<UserLoggedInEvent> {

    private final KafkaPublisher kafkaPublisher;
    private final ObjectMapper mapper;

       @Override
    public Class<UserLoggedInEvent> eventType() {
        return UserLoggedInEvent.class;
    }

    @Override
    public void handle(UserLoggedInEvent user) {
        log.debug("KafkaUserLoggedInHandler :: handle :: {} - {} - {}", user.getUserId(), user.getBalance(), user.getAndroidId());


        kafkaPublisher.publishAsync(KafkaEvent.builder()
                .tenantId(user.getTenantId())
                .aggerateId(user.getUserId().toString())
                .correlationId(user.getCorrelationId())
                .payload(mapper.writeValueAsString(user))
                .eventType("UserLogin").build());
    }
}
