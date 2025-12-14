package com.saas.platform.catalog.repository;

import com.saas.platform.catalog.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GameRepository extends JpaRepository<Game, Long> {

    Optional<Game> findByIdAndEnabled(Long id, boolean enabled);
    List<Game> findAllByIdInAndEnabled(Set<Long> ids, boolean enabled);
    List<Game> findAllByEnabled(boolean enabled);


}
