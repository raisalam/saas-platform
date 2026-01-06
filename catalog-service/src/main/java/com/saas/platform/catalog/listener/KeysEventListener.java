package com.saas.platform.catalog.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.platform.catalog.dto.KeyUsedPayload;
import com.saas.platform.catalog.entity.SubscriptionKey;
import com.saas.platform.catalog.listener.payload.KeyUsedEvent;
import com.saas.platform.catalog.service.SubscriptionKeyService;
import com.saas.platform.common.mqtt.MqttEvent;
import com.saas.platform.common.mqtt.MqttService;
import com.saas.platform.db.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeysEventListener {

    private final ObjectMapper objectMapper;
    // 💡 Dependency needed for deduplication/idempotency check
    private final MqttService mqttService;
    private final SubscriptionKeyService subscriptionKeyService;

    @KafkaListener(
            topics = "${kafka.topic.key-used-events:KeyUsed}", // Use property for topic name
            groupId = "${saas.common.kafka.group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(ConsumerRecord<String, String> record, Acknowledgment ack) {
        System.out.println("====================================KeyUsed processing Start================");
        String tenantId = null;
        String eventCorrelationId = null; // Get this header too!

        try {
            // 1. Extract Headers and Context (No change here)
            tenantId = getHeader(record, "tenant-id");
            String eventType = getHeader(record, "event-type");
            eventCorrelationId = getHeader(record, "correlation-id"); // Get the unique ID from Outbox


            try {
                if (tenantId == null || eventCorrelationId == null) {
                    throw new IllegalStateException("Required Kafka header (Tenant ID or Correlation ID) missing.");
                }
                // Set context for database routing/MDC
                TenantContext.setTenantId(tenantId);
                MDC.put("tenantId", tenantId);
                TenantContext.setMicroservice("catalog");
                KeyUsedEvent payload = objectMapper.readValue(record.value(), KeyUsedEvent.class);
                SubscriptionKey subscriptionKey = subscriptionKeyService.markKeyUsed(payload.getKey());

                payload.setUserId(subscriptionKey.getSellerId());
                payload.setCorrelationId(eventCorrelationId);
                payload.setTenantId(tenantId);

                KeyUsedPayload payloadEvent =
                        new KeyUsedPayload(payload.getKey(), true, subscriptionKey.getUsedDate());
                MqttEvent<KeyUsedPayload> event =
                        MqttEvent.<KeyUsedPayload>builder()
                                .type("KeyUsed")
                                .payload(payloadEvent)
                                .correlationId(eventCorrelationId) // use same correlationId
                                .version(1)
                                .build();

                mqttService.publishAsync("user/" + subscriptionKey.getSellerId() + "/events", objectMapper.writeValueAsString(event), 1, false);
            } catch (Exception e) {
                System.out.println("KeyUsed Listener failed acknologing success");
                e.printStackTrace();
            }


            // 5. Acknowledge (Only on success and after all DB changes are committed)
            ack.acknowledge();
            log.info("ACK committed for event {}", eventCorrelationId);

        } catch (Exception ex) {

            log.error("❌ Error processing event {}: {}", eventCorrelationId, ex.getMessage(), ex);
            // DO NOT ACK on failure. The @Transactional will roll back DB/idempotency check.
            // Spring Kafka will retry based on configuration.
        } finally {
            // Clean up context after processing
            TenantContext.clear();
            MDC.clear();
        }
    }

    // Header utility remains the same

    private String getHeader(ConsumerRecord<String, String> record, String headerName) {
        Header header = record.headers().lastHeader(headerName);
        return header != null ? new String(header.value()) : null;
    }
}