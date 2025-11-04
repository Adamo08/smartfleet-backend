package com.adamo.vrspfab.vehicles;

import com.adamo.vrspfab.common.containers.MySqlTestBaseIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class VehicleRepositoryIT extends MySqlTestBaseIT {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Test
    void repositoryLoads_andBasicQueriesExecuteAgainstSchema() {
        assertNotNull(vehicleRepository);

        assertTrue(vehicleRepository.findByLicensePlate("NOPE").isEmpty());

        var byYear = vehicleRepository.findByYearBetween(2000, 2025, PageRequest.of(0, 5));
        assertNotNull(byYear);

        var byMileage = vehicleRepository.findByMileageBetween(0f, 100000f, PageRequest.of(0, 5));
        assertNotNull(byMileage);
    }
}


