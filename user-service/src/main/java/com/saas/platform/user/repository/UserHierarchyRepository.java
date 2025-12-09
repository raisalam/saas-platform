package com.saas.platform.user.repository;

import com.saas.platform.user.entity.UserHierarchy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserHierarchyRepository extends JpaRepository<UserHierarchy, Long> {}
