package com.saas.platform.user.repository;

import com.saas.platform.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = {"role", "attributes"})
    Optional<User> findByUsername(String username);


    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {"role", "attributes"})
  Optional<User> findByEmail(String usernameOrEmail);


    @EntityGraph(attributePaths = {"role", "attributes"})
    Optional<User> findByRefreshToken(String refreshToken);
}
