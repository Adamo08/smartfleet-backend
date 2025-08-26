package com.adamo.vrspfab.reservations;

import com.adamo.vrspfab.notifications.NotificationService;
import com.adamo.vrspfab.notifications.NotificationType;
import com.adamo.vrspfab.slots.SlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Contains scheduled tasks related to the reservation lifecycle.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationScheduledTasks {

    private final SlotRepository slotRepository;
    private final ReservationRepository reservationRepository;
    private final NotificationService notificationService;

    /**
     * A scheduled task that runs periodically to mark past, confirmed reservations as 'COMPLETED'.
     * This task runs every hour at the top of the hour.
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void completeOldReservations() {
        log.info("Running scheduled task to complete old reservations...");
        List<Reservation> reservationsToComplete = reservationRepository.findAllByStatusAndEndDateBefore(
                ReservationStatus.CONFIRMED,
                LocalDateTime.now()
        );

        if (reservationsToComplete.isEmpty()) {
            log.info("No reservations to complete.");
            return;
        }

        for (Reservation reservation : reservationsToComplete) {
            reservation.setStatus(ReservationStatus.COMPLETED);
            // Restore slot availability for all associated slots
            if (reservation.getSlots() != null && !reservation.getSlots().isEmpty()) {
                reservation.getSlots().forEach(slot -> {
                    slot.setAvailable(true);
                    slot.setReservation(null); // Dissociate from reservation
                    slotRepository.save(slot);
                });
            }

            // Send notification
            notificationService.createAndDispatchNotification(
                    reservation.getUser(),
                    NotificationType.RESERVATION_COMPLETED,
                    "Your reservation for vehicle " + reservation.getVehicle().getBrand() + " " + reservation.getVehicle().getModel() + " has been completed."
            );
        }

        reservationRepository.saveAll(reservationsToComplete);
        log.info("Successfully completed {} reservations.", reservationsToComplete.size());
    }
}