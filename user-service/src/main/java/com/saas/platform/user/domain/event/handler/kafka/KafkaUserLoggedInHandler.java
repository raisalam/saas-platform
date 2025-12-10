package com.saas.platform.user.domain.event.handler.kafka;

import com.saas.platform.common.events.DomainEventHandler;
import com.saas.platform.common.firebase.FirebaseService;
import com.saas.platform.common.kafka.KafkaPublisher;
import com.saas.platform.common.kafka.events.user.UserLogin;
import com.saas.platform.user.domain.event.user.UserLoggedInEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Order(3) //
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "saas.common.kafka", name = "enabled", havingValue = "true")
public class KafkaUserLoggedInHandler implements DomainEventHandler<UserLoggedInEvent> {

    private final KafkaPublisher kafkaPublisher;

       @Override
    public Class<UserLoggedInEvent> eventType() {
        return UserLoggedInEvent.class;
    }

    @Override
    public void handle(UserLoggedInEvent user) {
        log.debug("KafkaUserLoggedInHandler :: handle :: {} - {} - {}", user.getUserId(), user.getBalance(), user.getAndroidId());

        kafkaPublisher.publishAsync(new UserLogin(
                user.getUserId(),
                user.getAndroidId(),
                "IpAddress",
                "UserAgent",
                Instant.now()));
    }
}
