package com.saas.platform.user.domain.event.handler.firebase;

import com.saas.platform.common.events.DomainEventHandler;
import com.saas.platform.common.firebase.FirebaseService;
import com.saas.platform.user.domain.event.key.UserLoggedInEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "saas.common.firebase", name = "enabled", havingValue = "true")
public class FirebaseUserLoggedInHandler implements DomainEventHandler<UserLoggedInEvent> {

    private final FirebaseService firebase; // your SDK wrapper


    @Override
    public Class<UserLoggedInEvent> eventType() {
        return UserLoggedInEvent.class;
    }

    @Override
    public void handle(UserLoggedInEvent user) {
        // push notification stub
        log.debug("FirebaseUserLoggedInHandler :: handle :: {} - {} - {}", user.getUserId(), user.getBalance(), user.getAndroidId());

        firebase.pushToUser(user.getUserId(), "Login detected", "We saw a login from " + user.getIp());
    }
}
