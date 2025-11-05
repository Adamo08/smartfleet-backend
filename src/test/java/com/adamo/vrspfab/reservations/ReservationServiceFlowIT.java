package com.adamo.vrspfab.reservations;

import com.adamo.vrspfab.common.containers.MySqlTestBaseIT;
import com.adamo.vrspfab.common.config.TestMailConfig;
import com.adamo.vrspfab.users.User;
import com.adamo.vrspfab.users.UserRepository;
import com.adamo.vrspfab.vehicles.Vehicle;
import com.adamo.vrspfab.vehicles.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestMailConfig.class)
class ReservationServiceFlowIT extends MySqlTestBaseIT {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    void createAndCancelReservation_persistsStatusChanges() {
        // Arrange: pick any existing user and vehicle from seeded data
        User user = userRepository.findAll().stream().findFirst().orElseThrow();
        Vehicle vehicle = vehicleRepository.findAll().stream().findFirst().orElseThrow();

        Reservation reservation = Reservation.builder()
                .user(user)
                .vehicle(vehicle)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .status(ReservationStatus.PENDING)
                .build();

        // Act: save and then cancel
        Reservation saved = reservationRepository.save(reservation);
        assertNotNull(saved.getId());

        saved.setStatus(ReservationStatus.CANCELLED);
        Reservation cancelled = reservationRepository.save(saved);

        // Assert
        assertEquals(ReservationStatus.CANCELLED, cancelled.getStatus());
        Reservation reloaded = reservationRepository.findById(saved.getId()).orElseThrow();
        assertEquals(ReservationStatus.CANCELLED, reloaded.getStatus());
    }
}


