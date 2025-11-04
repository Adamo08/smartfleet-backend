package com.adamo.vrspfab.database;

import com.adamo.vrspfab.common.containers.MySqlTestBaseIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class FlywayMySqlIT extends MySqlTestBaseIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void migrationsApplyAndTablesExist() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE()",
                Integer.class
        );
        assertTrue(count != null && count > 0);
    }
}


