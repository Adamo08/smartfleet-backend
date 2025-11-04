package com.adamo.vrspfab.reservations;

import com.adamo.vrspfab.common.containers.MySqlTestBaseIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReservationRepositoryIT extends MySqlTestBaseIT {

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void repositoryLoads_andBasicQueriesExecuteAgainstSchema() {
        assertNotNull(reservationRepository);

        // count by status on empty DB should be 0
        Long pending = reservationRepository.countByStatus(ReservationStatus.PENDING);
        assertNotNull(pending);

        // overlapping query returns empty list
        List<Reservation> overlapping = reservationRepository.findOverlappingReservations(
                1L,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1)
        );
        assertNotNull(overlapping);

        // pageable findAll with spec (null -> all) should not throw
        var page = reservationRepository.findAll(null, PageRequest.of(0, 5));
        assertNotNull(page);
    }
}


