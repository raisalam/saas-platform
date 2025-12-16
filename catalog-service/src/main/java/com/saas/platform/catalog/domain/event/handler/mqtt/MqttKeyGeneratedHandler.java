package com.saas.platform.catalog.domain.event.handler.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.platform.catalog.dto.BalanceUpdatedPayload;
import com.saas.platform.common.events.DomainEventHandler;
import com.saas.platform.common.mqtt.MqttEvent;
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
    private final ObjectMapper objectMapper;


    @Override
    public Class<KeyGeneratedEvent> eventType() {
        return KeyGeneratedEvent.class;
    }

    @Override
    public void handle(KeyGeneratedEvent user) {
        log.debug("MqttKeyGeneratedHandler :: handle :: {} - {}", user.getUserId(), user.getBalance());

        BalanceUpdatedPayload payload =
                new BalanceUpdatedPayload(user.getBalance() - user.getTotalCost(), false);
        MqttEvent<BalanceUpdatedPayload> event =
                MqttEvent.<BalanceUpdatedPayload>builder()
                        .type("BalanceUpdated")
                        .payload(payload)
                        .correlationId(user.getCorrelationId()) // use same correlationId
                        .version(1)
                        .build();

        try {
            mqttService.publishAsync("user/" + user.getUserId() + "/events", objectMapper.writeValueAsString(event), 1, true);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
  }
}
