package com.saas.platform.user.domain.event.handler.redis;

import com.saas.platform.common.events.DomainEventHandler;
import com.saas.platform.common.kafka.KafkaPublisher;
import com.saas.platform.common.mqtt.MqttService;
import com.saas.platform.common.redis.RedisService;
import com.saas.platform.user.domain.event.user.UserLoggedInEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2) // execute first
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "saas.common.redis", name = "enabled", havingValue = "true")
public class RedisUserLoggedInHandler implements DomainEventHandler<UserLoggedInEvent> {

    private final RedisService redisService;

    @Override
    public Class<UserLoggedInEvent> eventType() {
        return UserLoggedInEvent.class;
    }

    @Override
    public void handle(UserLoggedInEvent user) {
        log.debug("RedisUserLoggedInHandler :: handle :: {} - {} - {}", user.getUserId(), user.getBalance(), user.getAndroidId());
        redisService.setAsync("user/" + user.getUserId(), user.getBalance().toString());
    }
}
