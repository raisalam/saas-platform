package com.saas.platform.catalog.repository;

import com.saas.platform.catalog.entity.ReportStatsProjection;
import com.saas.platform.catalog.entity.SubscriptionKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface SubscriptionKeyRepository extends JpaRepository<SubscriptionKey, Long> {
    List<SubscriptionKey> findTop20BySellerIdOrderByCreatedDateDesc(Long sellerId);

    @Query(value = """
            SELECT
        SUM(p.price) AS totalGenerated,
        SUM(CASE WHEN k.is_used = 1 THEN p.price ELSE 0 END) AS totalUsed,
        SUM(p.price) AS totalValue,
        SUM(p.price - p.discount_price) AS totalDiscount,
        DATE(k.created_date) AS statDate
    FROM subscription_keys k
    JOIN plans p ON k.plan_id = p.id
    WHERE (:sellerId IS NULL OR k.seller_id = :sellerId)
      AND k.created_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
    GROUP BY DATE(k.created_date)
    ORDER BY statDate DESC
    """, nativeQuery = true)
    List<ReportStatsProjection> getComprehensiveReport(@Param("sellerId") Long sellerId);

    @Query(value = """
    SELECT
        k.seller_id AS sellerId,
        SUM(p.price) AS `generated`,
        SUM(CASE WHEN k.is_used = 1 THEN p.price ELSE 0 END) AS `used`,
        DATE(k.created_date) AS `statDate`
    FROM subscription_keys k
    JOIN plans p ON k.plan_id = p.id
    WHERE k.created_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
    GROUP BY DATE(k.created_date), k.seller_id
    ORDER BY `statDate` DESC, `generated` DESC
    """, nativeQuery = true)
    List<Map<String, Object>> getAdminRawStats();

}
