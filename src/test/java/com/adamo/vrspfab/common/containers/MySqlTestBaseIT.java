package com.adamo.vrspfab.common.containers;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

public abstract class MySqlTestBaseIT {

    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>(
            DockerImageName.parse("mysql:8.0.36")
    )
            .withDatabaseName("vrspfab_test")
            .withUsername("test")
            .withPassword("test")
            .waitingFor(Wait.forLogMessage(".*ready for connections.*", 1))
            .withStartupTimeout(Duration.ofMinutes(2));

    @BeforeAll
    static void startContainer() {
        if (!MYSQL.isRunning()) {
            MYSQL.start();
        }
    }

    @AfterAll
    static void stopContainer() {
        // Container will be stopped automatically by Testcontainers
        // Only stop if explicitly needed
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.show-sql", () -> "false");
        // HikariCP connection pool settings for Testcontainers
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "5");
        registry.add("spring.datasource.hikari.minimum-idle", () -> "2");
        registry.add("spring.datasource.hikari.connection-timeout", () -> "60000"); // 60 seconds
        registry.add("spring.datasource.hikari.max-lifetime", () -> "300000"); // 5 minutes
        registry.add("spring.datasource.hikari.idle-timeout", () -> "300000"); // 5 minutes
        registry.add("spring.datasource.hikari.validation-timeout", () -> "5000"); // 5 seconds
        // Minimal mail configuration for Spring Boot to create JavaMailSender bean
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> "1025");
        registry.add("spring.mail.properties.mail.smtp.from", () -> "test@example.com");
    }
}


