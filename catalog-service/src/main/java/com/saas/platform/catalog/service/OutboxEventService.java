package com.saas.platform.catalog.service;

import com.saas.platform.catalog.entity.OutboxEvent;
import com.saas.platform.catalog.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventService {

    private final OutboxEventRepository repository;
    private static final int MAX_RETRIES = 10;

    @Transactional(value = "transactionManager") // 👈 Specify the bean name
    public List<OutboxEvent> lockAndFetch(int batchSize) {
        List<OutboxEvent> events =
                repository.fetchForProcessing(LocalDateTime.now(), batchSize);

        // Mark as PROCESSING while still holding DB lock
        events.forEach(e -> e.setStatus("PROCESSING"));

        return events;
    }

    @Transactional
    public void markSuccess(OutboxEvent event) {
        event.markPublished();
    }

    @Transactional
    public void markFailure(OutboxEvent event, Exception ex) {

        if (event.getRetryCount() >= MAX_RETRIES) {
            event.markFailed(ex.getMessage());
        } else {
            int backoff = (int) Math.pow(2, event.getRetryCount()) * 5;
            event.markForRetry(ex.getMessage(), backoff);
        }
    }
}
