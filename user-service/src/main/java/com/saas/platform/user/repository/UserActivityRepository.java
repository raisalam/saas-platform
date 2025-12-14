package com.saas.platform.user.repository;

import com.saas.platform.user.entity.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {

    // Recent activity feed
    List<UserActivity> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);

    // Today's generated keys
    @Query("""
        SELECT COALESCE(SUM(CAST(JSON_EXTRACT(a.metadata, '$.keys') AS INTEGER)), 0)
        FROM UserActivity a
        WHERE a.activityType = 'KEY_GENERATED'
          AND DATE(a.createdAt) = CURRENT_DATE
    """)
    Long todayGeneratedKeys();

    // Today's used keys
    @Query("""
        SELECT COUNT(a.id)
        FROM UserActivity a
        WHERE a.activityType = 'KEY_USED'
          AND DATE(a.createdAt) = CURRENT_DATE
    """)
    Long todayUsedKeys();

    // Today's total spent
    @Query("""
        SELECT COALESCE(SUM(a.amount), 0)
        FROM UserActivity a
        WHERE a.activityType = 'KEY_GENERATED'
          AND DATE(a.createdAt) = CURRENT_DATE
    """)
    Double todayTotalSpent();
}
