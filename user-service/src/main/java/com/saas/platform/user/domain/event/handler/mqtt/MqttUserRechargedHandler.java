package com.saas.platform.user.domain.event.handler.mqtt;

import com.saas.platform.common.events.DomainEventHandler;
import com.saas.platform.common.mqtt.MqttService;
import com.saas.platform.user.domain.event.key.UserLoggedInEvent;
import com.saas.platform.user.domain.event.key.UserRechargedEvent;
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

    private final MqttService mqttService;

    @Override
    public Class<UserRechargedEvent> eventType() {
        return UserRechargedEvent.class;
    }

    @Override
    public void handle(UserRechargedEvent event) {
        log.debug("MqttUserRechargedHandler :: handle :: {} - {} - {}", event.getChildUserId(), event.getChildRechargeAmount(), event.getParentRechargeAmount());

        String parentMessage = String.format("""
        {
          "type": "BalanceUpdated",
          "balance": %s
        }
        """,
                event.getParentCurrentAmount().toString()
        );

        mqttService.publishAsync( "user/" + event.getParentUserId()+"/events", parentMessage, 1, true);

        String childMessage = String.format("""
        {
          "type": "BalanceUpdated",
          "balance": %s,
          "notify":true
        }
        """,
                event.getChildCurrentAmount().toString()
        );

        mqttService.publishAsync( "user/" + event.getChildUserId()+"/events", childMessage, 1, true);
    }
}
