package com.saas.platform.catalog.repository;

import com.saas.platform.catalog.entity.SubscriptionKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionKeyRepository extends JpaRepository<SubscriptionKey, Long> {
    List<SubscriptionKey> findTop20BySellerIdOrderByCreatedDateDesc(Long sellerId);

}
