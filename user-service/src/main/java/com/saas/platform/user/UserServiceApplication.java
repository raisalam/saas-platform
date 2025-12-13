package com.saas.platform.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;

import org.springframework.retry.annotation.EnableRetry; // <-- Import this


@SpringBootApplication(scanBasePackages = {"com.saas.platform.user", "com.saas.platform.db"})
@EnableJpaRepositories(
        basePackages = "com.saas.platform.user.repository",
        entityManagerFactoryRef = "tenantEntityManagerFactory",
        transactionManagerRef = "tenantTransactionManager"
)
@EntityScan(basePackages = "com.saas.platform.user.entity")
@EnableKafka
@EnableRetry // <--- ADD THIS ANNOTATION HERE
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
