package com.saas.platform.catalog.repository;

import com.saas.platform.catalog.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    Optional<Plan> findByIdAndEnabled(Long id, boolean enabled);
    List<Plan> findAllByIdInAndEnabled(Set<Long> ids, boolean enabled);

}
