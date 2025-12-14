package com.saas.platform.catalog.repository;

import com.saas.platform.catalog.entity.TenantDatabaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenantRegistryRepository
        extends JpaRepository<TenantDatabaseEntity, Long> {

    @Query(value = """
        SELECT tenant_id
        FROM tenant_databases
        WHERE microservice = 'catalog'
          AND is_active = true
    """, nativeQuery = true)
    List<String> findActiveCatalogTenants();
}
