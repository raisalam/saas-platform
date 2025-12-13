package com.saas.platform.catalog.domain.event.handler.firebase;

import com.saas.platform.common.events.DomainEventHandler;
import com.saas.platform.common.firebase.FirebaseService;
import com.saas.platform.catalog.domain.event.key.KeyGeneratedEvent;
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
public class FirebaseKeyGeneratedHandler implements DomainEventHandler<KeyGeneratedEvent> {

    private final FirebaseService firebase; // your SDK wrapper


    @Override
    public Class<KeyGeneratedEvent> eventType() {
        return KeyGeneratedEvent.class;
    }

    @Override
    public void handle(KeyGeneratedEvent user) {
        // push notification stub
        log.debug("FirebaseUserLoggedInHandler :: handle :: {} - {} ", user.getUserId(), user.getBalance());

        firebase.pushToUser(user.getUserId(), "Login detected", "We saw a login from ");
    }
}
