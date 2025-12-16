package com.saas.platform.user.listener;

import com.saas.platform.common.mqtt.MqttEvent;
import com.saas.platform.common.mqtt.MqttService;
import com.saas.platform.db.TenantContext;
import com.saas.platform.user.entity.UserActivity;
import com.saas.platform.user.factory.ActivityFactory;
import com.saas.platform.user.listener.payload.KeyGeneratedEvent;
import com.saas.platform.user.service.IdempotencyService;
import com.saas.platform.user.service.UserActivityService;
import com.saas.platform.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional; // <-- CRITICAL: For deduplication
import com.fasterxml.jackson.databind.ObjectMapper; // Use standard Jackson

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeysEventListener {

    private final UserService userService;
    private final ObjectMapper objectMapper;
    // 💡 Dependency needed for deduplication/idempotency check
    private final IdempotencyService idempotencyService;
    private final UserActivityService userActivityService;
    private final MqttService mqttService;




    @KafkaListener(
            topics = "${kafka.topic.keys-events:keys.events}", // Use property for topic name
            groupId = "${saas.common.kafka.group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(ConsumerRecord<String, String> record, Acknowledgment ack) {
        System.out.println("====================================Lisner processing Start================");
        String tenantId = null;
        String eventCorrelationId = null; // Get this header too!

        try {
            // 1. Extract Headers and Context (No change here)
            tenantId = getHeader(record, "tenant-id");
            String eventType = getHeader(record, "event-type");
            eventCorrelationId = getHeader(record, "correlation-id"); // Get the unique ID from Outbox

            if (tenantId == null || eventCorrelationId == null) {
                throw new IllegalStateException("Required Kafka header (Tenant ID or Correlation ID) missing.");
            }

            // Set context for database routing/MDC
            TenantContext.setTenantId(tenantId);
            MDC.put("tenantId", tenantId);
            TenantContext.setMicroservice("user");

            System.out.println("=====================context set========"+TenantContext.getTenantId()+" = "+TenantContext.getMicroservice());
            // 2. Prevent Double Processing (Idempotency Check)
            // The correlationId from the OutboxEvent is the unique key for this operation.
            if (idempotencyService.isProcessed(eventCorrelationId, tenantId, "KeysEventListener")) {
                log.warn("Event {} already processed for tenant {}. Skipping.", eventCorrelationId, tenantId);
                ack.acknowledge(); // Acknowledge to move the offset
                return;
            }

            // 3. Deserialization and Logic
            if ("KeyGenerated".equals(eventType)) {
                // ❌ FIX 1: Deserialization - Use a specific payload DTO
                // The JSON is NOT a KafkaEvent object. It's the PAYLOAD of a KafkaEvent.
                System.out.println("Raw Kafka Value: " + record.value());
                KeyGeneratedEvent payload = objectMapper.readValue(record.value(), KeyGeneratedEvent.class);

                // ❌ FIX 2: Correctly map fields for balance update
                // The balance passed is the balance BEFORE the deduction.
                // We need the AGGREGATE ID (seller ID) and the TOTAL COST.

                // The balance update must be done transactionally.
                userService.updateBalance(
                        payload.getUserId(),
                        payload.getTotalCost()// The correlation ID for logging/idempotency
                );

                UserActivity userActivity =  ActivityFactory.keyGenerated(
                        payload.getUserId(),
                        payload.getKeys().getKeys(),
                        payload.getTotalCost(),
                        payload.getBalance()-payload.getTotalCost(),
                        eventCorrelationId
                );
                userActivityService.log(
                        userActivity
                );

                MqttEvent<UserActivity> mqttEvent =
                        MqttEvent.<UserActivity>builder()
                                .type("UserActivity")
                                .payload(userActivity)
                                .correlationId(eventCorrelationId)
                                .version(1)
                                .build();

                mqttService.publishAsync( "user/" + payload.getUserId()+"/activity", objectMapper.writeValueAsString(mqttEvent));

                // 4. Mark as Processed (Atomically with balance update)
                idempotencyService.markAsProcessed(eventCorrelationId, tenantId, "KeysEventListener", eventType);
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