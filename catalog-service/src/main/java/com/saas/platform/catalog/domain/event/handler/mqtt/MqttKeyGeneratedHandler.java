package com.saas.platform.catalog.domain.event.handler.mqtt;

import com.saas.platform.common.events.DomainEventHandler;
import com.saas.platform.common.mqtt.MqttService;
import com.saas.platform.catalog.domain.event.key.KeyGeneratedEvent;
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
public class MqttKeyGeneratedHandler implements DomainEventHandler<KeyGeneratedEvent> {

    private final MqttService mqttService;

    @Override
    public Class<KeyGeneratedEvent> eventType() {
        return KeyGeneratedEvent.class;
    }

    @Override
    public void handle(KeyGeneratedEvent user) {
        log.debug("MqttKeyGeneratedHandler :: handle :: {} - {}", user.getUserId(), user.getBalance());

        String message = String.format("""
                        {
                          "type": "BalanceUpdated",
                          "balance": %s
                        }
                        """,
                user.getBalance() - user.getTotalCost());

        mqttService.publishAsync("user/" + user.getUserId() + "/events", message, 1, true);
    }
}
