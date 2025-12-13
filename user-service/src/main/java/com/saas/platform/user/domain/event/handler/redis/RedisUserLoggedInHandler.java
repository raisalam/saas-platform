package com.saas.platform.user.domain.event.handler.redis;

import com.saas.platform.common.dto.CachedUserDTO;
import com.saas.platform.common.events.DomainEventHandler;
import com.saas.platform.common.redis.RedisService;
import com.saas.platform.user.domain.event.key.UserLoggedInEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@Order(2) // execute first
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "saas.common.redis", name = "enabled", havingValue = "true")
public class RedisUserLoggedInHandler implements DomainEventHandler<UserLoggedInEvent> {

    private final RedisService redisService;
    private final ObjectMapper mapper;

    @Override
    public Class<UserLoggedInEvent> eventType() {
        return UserLoggedInEvent.class;
    }

    @Override
    public void handle(UserLoggedInEvent user) {
        log.debug("RedisUserLoggedInHandler :: handle :: {} - {} - {}", user.getUserId(), user.getBalance(), user.getAndroidId());
        CachedUserDTO cachedUserDTO = CachedUserDTO.builder()
                .tenantId(user.getTenantId())
                .userId(user.getUserId())
                .balance(user.getBalance())
                .total(user.getTotal())
                .role(user.getRole())
                .active(user.isActive())
                .build();
        redisService.setAsync("tenant/"+user.getTenantId()+"/user/" + user.getUserId(), cachedUserDTO);
    }
}
