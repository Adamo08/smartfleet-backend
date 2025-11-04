package com.adamo.vrspfab.favorites;

import com.adamo.vrspfab.common.containers.MySqlTestBaseIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FavoriteRepositoryIT extends MySqlTestBaseIT {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Test
    void repositoryLoads_andBasicQueriesExecuteAgainstSchema() {
        assertNotNull(favoriteRepository);
        var page = favoriteRepository.findAll(PageRequest.of(0, 5));
        assertNotNull(page);
        assertTrue(favoriteRepository.findByUserIdAndVehicleId(1L, 1L).isEmpty());
    }
}


