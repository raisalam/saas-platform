package com.saas.platform.catalog.schedular;

import com.saas.platform.catalog.entity.OutboxEvent;
import com.saas.platform.catalog.repository.OutboxEventRepository;
import com.saas.platform.common.kafka.KafkaPublisher;
import com.saas.platform.common.kafka.events.KafkaEvent;
import com.saas.platform.db.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxScheduler {

    private static final int MAX_RETRIES = 10;

    private final OutboxEventRepository repository;
    private final KafkaPublisher kafkaPublisher;

    @Scheduled(fixedDelayString = "${outbox.poll.interval:3000}")
    @Transactional
    public void publishOutboxEvents() {

    }

    private void processEvent(OutboxEvent event) {

    }

    private void applyBackoff(int retry) {
        long delayMs = (long) Math.pow(2, retry) * 100L; // exponential
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}

