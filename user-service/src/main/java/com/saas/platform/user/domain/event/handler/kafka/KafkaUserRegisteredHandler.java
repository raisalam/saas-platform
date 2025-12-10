package com.saas.platform.user.domain.event.handler.kafka;

import com.saas.platform.common.events.DomainEventHandler;
import com.saas.platform.common.kafka.KafkaPublisher;
import com.saas.platform.common.kafka.events.user.UserLogin;
import com.saas.platform.common.kafka.events.user.UserRegister;
import com.saas.platform.common.mqtt.MqttService;
import com.saas.platform.user.domain.event.user.UserLoggedInEvent;
import com.saas.platform.user.domain.event.user.UserRegisteredEvent;
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
public class KafkaUserRegisteredHandler implements DomainEventHandler<UserRegisteredEvent> {

    private final KafkaPublisher kafkaPublisher;

    @Override
    public Class<UserRegisteredEvent> eventType() {
        return UserRegisteredEvent.class;
    }

    @Override
    public void handle(UserRegisteredEvent user) {
        log.debug("KafkaUserRegisteredHandler :: handle :: {} - {} - {}", user.getUserId(), user.getFullName(), user.getAndroidId());

        kafkaPublisher.publishAsync(new UserRegister(
                user.getUserId(),
                user.getUsername(),   // FIXED
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getAndroidId(),
                user.getDeviceId(),
                Instant.now()
        ));
    }
}
