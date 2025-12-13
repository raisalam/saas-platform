package com.saas.platform.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.retry.annotation.EnableRetry;


@SpringBootApplication(scanBasePackages = {"com.saas.platform.catalog", "com.saas.platform.db"})
@EnableJpaRepositories(
        basePackages = "com.saas.platform.catalog.repository",
        entityManagerFactoryRef = "tenantEntityManagerFactory",
        transactionManagerRef = "tenantTransactionManager"
)
@EntityScan(basePackages = "com.saas.platform.catalog.entity")
@EnableKafka
@EnableRetry
public class CatalogServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CatalogServiceApplication.class, args);
    }
}
