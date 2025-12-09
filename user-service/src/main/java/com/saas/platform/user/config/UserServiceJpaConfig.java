package com.saas.platform.user.config;

import com.saas.platform.db.TenantRoutingDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class UserServiceJpaConfig {

    private final TenantRoutingDataSource routingDataSource;
    private final JpaVendorAdapter jpaVendorAdapter;

    public UserServiceJpaConfig(TenantRoutingDataSource routingDataSource,
                                JpaVendorAdapter jpaVendorAdapter) {
        this.routingDataSource = routingDataSource;
        this.jpaVendorAdapter = jpaVendorAdapter;
    }

    @Bean(name = "tenantEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean tenantEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(routingDataSource);
        emf.setJpaVendorAdapter(jpaVendorAdapter);
        emf.setPackagesToScan("com.saas.platform.user.entity"); // microservice-specific
        return emf;
    }

    @Bean(name = "tenantTransactionManager")
    public PlatformTransactionManager tenantTransactionManager(
            LocalContainerEntityManagerFactoryBean userServiceEntityManagerFactory) {
        return new JpaTransactionManager(userServiceEntityManagerFactory.getObject());
    }
}
