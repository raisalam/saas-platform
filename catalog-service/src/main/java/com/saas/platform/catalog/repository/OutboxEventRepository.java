package com.saas.platform.catalog.repository;

import com.saas.platform.catalog.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * Used by the Outbox Poller/Relayer to find events ready for publishing.
     * We often order by 'createdAt' to maintain publishing order.
     */
    List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(String status);

    @Query("""
               SELECT e FROM OutboxEvent e
               WHERE e.status = 'PENDING'
                 AND (e.nextRetryAt IS NULL OR e.nextRetryAt <= :now)
               ORDER BY e.createdAt
            """)
    List<OutboxEvent> findReadyForProcessing(@Param("now") LocalDateTime now);

    @Query(
            value = """
    SELECT *
    FROM outbox
    WHERE status = 'PENDING'
      AND (next_retry_at IS NULL OR next_retry_at <= :now)
    ORDER BY created_at
    LIMIT :limit
    FOR UPDATE SKIP LOCKED
  """,
            nativeQuery = true
    )
    List<OutboxEvent> fetchForProcessing(
            @Param("now") LocalDateTime now,
            @Param("limit") int limit
    );


}