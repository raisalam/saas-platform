package com.saas.platform.catalog.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tenant_databases")
public class TenantDatabaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private String tenantId;

    private String microservice;

    private boolean isActive;
}
