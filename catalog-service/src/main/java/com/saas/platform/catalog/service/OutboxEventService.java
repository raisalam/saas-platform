package com.saas.platform.catalog.service;

import com.saas.platform.catalog.entity.OutboxEvent;
import com.saas.platform.catalog.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventService {

    private final OutboxEventRepository repository;

    @Transactional(value = "transactionManager") // 👈 Specify the bean name
    public List<OutboxEvent> getEvents(String  status) {
        List<OutboxEvent> events =
                repository.findTop100ByStatusOrderByCreatedAtAsc(status);
        events.forEach(e -> e.setStatus("PROCESSING"));
        return events;
    }

    @Transactional(value = "transactionManager") // 👈 Specify the bean name
    public void markPublished(Long id) {
        repository.findById(id).ifPresent(e -> {
            e.setStatus("PUBLISHED");});
    }

    @Transactional(value = "transactionManager") // 👈 Specify the bean name
    public void markFailed(Long id) {
        repository.findById(id).ifPresent(e -> {
            e.setStatus("FAILED");
            e.setRetryCount(e.getRetryCount() + 1);
        });
    }
}
