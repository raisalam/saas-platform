package com.saas.platform.user.domain.event.handler.redis;

import com.saas.platform.common.dto.CachedUserDTO;
import com.saas.platform.common.events.DomainEventHandler;
import com.saas.platform.common.redis.RedisService;
import com.saas.platform.user.domain.event.key.UserLoggedInEvent;
import com.saas.platform.user.domain.event.key.UserRechargedEvent;
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
public class RedisUserRechargedHandler implements DomainEventHandler<UserRechargedEvent> {

    private final RedisService redisService;

    @Override
    public Class<UserRechargedEvent> eventType() {
        return UserRechargedEvent.class;
    }

    @Override
    public void handle(UserRechargedEvent event) {
        log.debug("====================================RedisUserRechargedHandler :: handle ::");

        CachedUserDTO parentCachedUserDTO = CachedUserDTO.builder()
                .tenantId(event.getTenantId())
                .userId(event.getParentUserId())
                .balance(event.getParentCurrentAmount())
                .total(event.getParentTotalAmount())
                .role(event.getParentRole())
                .active(event.isParentActive())
                .build();
        redisService.setAsync("tenant/"+event.getTenantId()+"/user/" + parentCachedUserDTO.getUserId(), parentCachedUserDTO);

        CachedUserDTO childCachedUserDTO = CachedUserDTO.builder()
                .tenantId(event.getTenantId())
                .userId(event.getChildUserId())
                .balance(event.getChildCurrentAmount())
                .total(event.getChildTotalAmount())
                .role(event.getChildRole())
                .active(event.isChildActive())
                .build();
        redisService.setAsync("tenant/"+event.getTenantId()+"/user/" + childCachedUserDTO.getUserId(), childCachedUserDTO);
    }
}
