package com.saas.platform.catalog.domain.event.handler.redis;

import com.saas.platform.common.events.DomainEventHandler;
import com.saas.platform.common.redis.RedisService;
import com.saas.platform.catalog.domain.event.key.KeyGeneratedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2) // execute first
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "saas.common.redis", name = "enabled", havingValue = "true")
public class RedisKeyGeneratedHandler implements DomainEventHandler<KeyGeneratedEvent> {

    private final RedisService redisService;

    @Override
    public Class<KeyGeneratedEvent> eventType() {
        return KeyGeneratedEvent.class;
    }

    @Override
    public void handle(KeyGeneratedEvent user) {
        log.debug("RedisKeyGeneratedHandler :: handle :: {} - {}", user.getUserId(), user.getBalance());
        // redisService.setAsync( "user/" + user.getUserId(),
         //       String.valueOf(user.getBalance() - user.getTotalCost()));
    }
}
