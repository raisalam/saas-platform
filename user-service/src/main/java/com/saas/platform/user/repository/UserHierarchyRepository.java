package com.saas.platform.user.repository;

import com.saas.platform.user.entity.UserHierarchy;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserHierarchyRepository extends JpaRepository<UserHierarchy, Long> {
    List<UserHierarchy> findByParentId(Long parentId);

    List<UserHierarchy> findByChildId(Long childId);

    boolean existsByParentIdAndChildId(Long parentId, Long childId);

    void deleteByParentIdAndChildId(Long parentId, Long childId);

    @EntityGraph(attributePaths = {"child.role", "child.attributes"})
    @Query("SELECT h FROM UserHierarchy h JOIN FETCH h.child WHERE h.parent.id = :parentId")
    List<UserHierarchy> findByParentIdWithChild(@Param("parentId") Long parentId);
}
