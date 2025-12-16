package com.saas.platform.user.domain.event.handler.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.platform.common.events.DomainEventHandler;
import com.saas.platform.common.mqtt.MqttEvent;
import com.saas.platform.common.mqtt.MqttService;
import com.saas.platform.user.domain.event.key.UserLoggedInEvent;
import com.saas.platform.user.domain.event.key.UserRechargedEvent;
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
public class MqttUserRechargedHandler implements DomainEventHandler<UserRechargedEvent> {

    private final ObjectMapper objectMapper;

    private final MqttService mqttService;

    @Override
    public Class<UserRechargedEvent> eventType() {
        return UserRechargedEvent.class;
    }

    public void handle(UserRechargedEvent event) {
        log.debug(
                "MqttUserRechargedHandler :: handle :: parent={} child={} parentAmt={} childAmt={}",
                event.getParentUserId(),
                event.getChildUserId(),
                event.getParentRechargeAmount(),
                event.getChildRechargeAmount()
        );

        final String correlationId = event.getCorrelationId(); // must exist

        /* ---------------- PARENT ---------------- */

        BalanceUpdatedPayload parentPayload =
                BalanceUpdatedPayload.builder()
                        .balance(event.getParentCurrentAmount())
                        .build();

        MqttEvent<BalanceUpdatedPayload> parentEvent =
                MqttEvent.<BalanceUpdatedPayload>builder()
                        .type("BalanceUpdated")
                        .payload(parentPayload)
                        .correlationId(correlationId)
                        .version(1)
                        .build();



        /* ---------------- CHILD ---------------- */

        BalanceUpdatedPayload childPayload =
                BalanceUpdatedPayload.builder()
                        .balance(event.getChildCurrentAmount())
                        .notify(true) // 👈 ONLY child has notify
                        .build();

        MqttEvent<BalanceUpdatedPayload> childEvent =
                MqttEvent.<BalanceUpdatedPayload>builder()
                        .type("BalanceUpdated")
                        .payload(childPayload)
                        .correlationId(correlationId)
                        .version(1)
                        .build();

        try {

            mqttService.publishAsync(
                    "user/" + event.getParentUserId() + "/events",
                    objectMapper.writeValueAsString(parentEvent),
                    1,
                    true
            );


            mqttService.publishAsync(
                    "user/" + event.getChildUserId() + "/events",
                    objectMapper.writeValueAsString(childEvent),
                    1,
                    true
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
