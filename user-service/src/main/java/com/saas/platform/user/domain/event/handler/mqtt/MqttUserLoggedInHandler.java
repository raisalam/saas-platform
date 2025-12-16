package com.saas.platform.user.domain.event.handler.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.platform.common.events.DomainEventHandler;
import com.saas.platform.common.mqtt.MqttEvent;
import com.saas.platform.common.mqtt.MqttService;
import com.saas.platform.user.domain.event.key.UserLoggedInEvent;
import com.saas.platform.user.dto.BalanceUpdatedPayload;
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

    private final ObjectMapper objectMapper;

    private final MqttService mqttService;

    @Override
    public Class<UserLoggedInEvent> eventType() {
        return UserLoggedInEvent.class;
    }

    @Override
    public void handle(UserLoggedInEvent user) {
        log.debug("MqttUserLoggedInHandler :: handle :: {} - {} - {}", user.getUserId(), user.getBalance(), user.getAndroidId());

        BalanceUpdatedPayload payload =
                new BalanceUpdatedPayload(user.getBalance(), false);
        MqttEvent<BalanceUpdatedPayload> event =
                MqttEvent.<BalanceUpdatedPayload>builder()
                        .type("BalanceUpdated")
                        .payload(payload)
                        .correlationId(user.getCorrelationId()) // use same correlationId
                        .version(1)
                        .build();

        try {
            mqttService.publishAsync( "user/" + user.getUserId()+"/events", objectMapper.writeValueAsString(event), 1, true);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
