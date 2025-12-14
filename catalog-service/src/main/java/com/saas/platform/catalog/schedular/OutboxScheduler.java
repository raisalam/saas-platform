package com.saas.platform.catalog.schedular;

import com.saas.platform.catalog.entity.OutboxEvent;
import com.saas.platform.catalog.repository.OutboxEventRepository;
import com.saas.platform.catalog.repository.TenantRegistryRepository;
import com.saas.platform.catalog.service.OutboxEventService;
import com.saas.platform.catalog.service.OutboxProcessor;
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
import java.util.concurrent.Semaphore;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxScheduler {

    private static final int MAX_RETRIES = 10;

    private final TenantRegistryRepository tenantRegistry;
    private final OutboxProcessor processor;
    private final OutboxEventService outboxEventService;
    private final Semaphore semaphore = new Semaphore(1);


    @Scheduled(fixedDelayString = "${outbox.poll.interval:30000}")
    public void publishOutboxEvents() {

        if (!semaphore.tryAcquire()) {
            log.info("Previous Outbox processing still running. Skipping this cycle.");
            return;
        }

        log.info("Outbox Schedular started running at {}", LocalDateTime.now());

        try {
            // Fetch pending events (tenant-specific)
            TenantContext.clear();

            List<String> tenants = tenantRegistry.findActiveCatalogTenants();

            for (String tenantId : tenants) {
                processTenantOutbox(tenantId);
            }
        } catch (Exception ex) {
            log.error("Error processing Outbox events: {}", ex.getMessage(), ex);
        } finally {
            semaphore.release();
        }

        // 1️⃣ Always start from MASTER

    }

    private void processTenantOutbox(String tenantId) {

        try {
            // 2️⃣ Switch to tenant catalog DB
            TenantContext.setTenantId(tenantId);
            TenantContext.setMicroservice("catalog");
            List<OutboxEvent> events = outboxEventService.lockAndFetch(50);
            for (OutboxEvent event : events) {
                System.out.println("==========Schedular picked some events=========");
                processor.processEvent(event);
            }

        } catch (Exception e) {
            log.error("Outbox processing failed for tenant {}", tenantId, e);
        } finally {
            TenantContext.clear();
        }
    }
}
