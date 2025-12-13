package com.saas.platform.catalog.repository;

import com.saas.platform.catalog.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * Used by the Outbox Poller/Relayer to find events ready for publishing.
     * We often order by 'createdAt' to maintain publishing order.
     */
    List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(String status);
}