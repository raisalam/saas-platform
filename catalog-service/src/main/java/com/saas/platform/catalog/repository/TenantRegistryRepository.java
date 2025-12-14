package com.saas.platform.catalog.repository;

import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TenantRegistryRepository {

    @Query(value = """
        SELECT tenant_id
        FROM tenant_databases
        WHERE microservice = 'catalog'
          AND is_active = true
    """, nativeQuery = true)
    List<String> findActiveCatalogTenants();
}
