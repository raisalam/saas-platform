package com.saas.platform.user.repository;

import com.saas.platform.user.entity.UserAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAttributeRepository extends JpaRepository<UserAttribute, Long> {}
