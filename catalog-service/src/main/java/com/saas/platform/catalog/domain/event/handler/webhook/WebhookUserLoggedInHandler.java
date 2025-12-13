package com.saas.platform.catalog.domain.event.handler.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.platform.common.events.DomainEventHandler;
import com.saas.platform.catalog.domain.event.key.KeyGeneratedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Order(4)
@Slf4j
public class WebhookUserLoggedInHandler implements DomainEventHandler<KeyGeneratedEvent> {

    private final RestTemplate rest;
    private final ObjectMapper mapper;
    private final String webhookUrl = "https://example.com/webhook/user-login"; // your config

    public WebhookUserLoggedInHandler(RestTemplate rest, ObjectMapper mapper) {
        this.rest = rest;
        this.mapper = mapper;
    }

    @Override
    public Class<KeyGeneratedEvent> eventType() {
        return KeyGeneratedEvent.class;
    }

    @Override
    public void handle(KeyGeneratedEvent user) {
        try {

            log.info("WebhookUserLoggedInHandler :: handle :: {} - {}", user.getUserId(), user.getBalance());

            /**
            String json = mapper.writeValueAsString(Map.of(
                "userId", user.getUserId(),
                "email", user.getEmail(),
                "name", user.getName(),
                "ip", user.getIp(),
                "timestamp", user.getTimestamp().toString(),
                "meta", user.getMeta()
            ));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            rest.postForEntity(webhookUrl, new HttpEntity<>(json, headers), String.class);
       **/
        } catch (Exception e) {
            // log & handle
            throw new RuntimeException(e);
        }
    }
}
