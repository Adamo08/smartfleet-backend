package com.adamo.vrspfab.common.containers;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class MySqlTestBaseIT {

    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>(
            DockerImageName.parse("mysql:8.0.36")
    )
            .withDatabaseName("vrspfab_test")
            .withUsername("test")
            .withPassword("test");

    @BeforeAll
    static void startContainer() {
        MYSQL.start();
    }

    @AfterAll
    static void stopContainer() {
        MYSQL.stop();
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.show-sql", () -> "false");
        // Avoid external services in tests
        registry.add("spring.mail.host", () -> "localhost");
    }
}


