package com.saas.platform.user.domain.event.handler.mqtt;

import com.saas.platform.common.events.DomainEventHandler;
import com.saas.platform.common.mqtt.MqttService;
import com.saas.platform.user.domain.event.key.UserLoggedInEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1) // execute first
@Slf4j
@ConditionalOnProperty(prefix = "saas.common.mqtt", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class MqttUserLoggedInHandler implements DomainEventHandler<UserLoggedInEvent> {

    private final MqttService mqttService;

    @Override
    public Class<UserLoggedInEvent> eventType() {
        return UserLoggedInEvent.class;
    }

    @Override
    public void handle(UserLoggedInEvent user) {
        log.debug("MqttUserLoggedInHandler :: handle :: {} - {} - {}", user.getUserId(), user.getBalance(), user.getAndroidId());

        String message = String.format("""
        {
          "type": "BalanceUpdated",
          "balance": %s
        }
        """,
                user.getBalance().toString()
        );

        mqttService.publishAsync( "user/" + user.getUserId()+"/events", message, 1, true);
    }
}
