package com.adamo.vrspfab.payments;

import com.adamo.vrspfab.notifications.NotificationService;
import com.adamo.vrspfab.notifications.NotificationType;
import com.adamo.vrspfab.reservations.Reservation;
import com.adamo.vrspfab.reservations.ReservationRepository;
import com.adamo.vrspfab.reservations.ReservationStatus;
import com.adamo.vrspfab.slots.Slot;
import com.adamo.vrspfab.slots.SlotRepository;
import com.adamo.vrspfab.slots.SlotType;
import com.adamo.vrspfab.vehicles.Vehicle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Event listener for payment-related events.
 * Handles business logic when payments are completed, such as updating reservation status
 * and creating associated slots.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final ReservationRepository reservationRepository;
    private final SlotRepository slotRepository;
    private final NotificationService notificationService;

    /**
     * Handles payment completion events.
     * Updates reservation status and creates associated slots.
     */
    @EventListener
    @Async
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("Processing payment completed event for payment ID: {}, reservation ID: {}", 
                event.getPaymentId(), event.getReservationId());

        try {
            // Find the reservation
            Reservation reservation = reservationRepository.findById(event.getReservationId())
                    .orElseThrow(() -> new RuntimeException("Reservation not found: " + event.getReservationId()));

            // Update reservation status to CONFIRMED
            if (reservation.getStatus() == ReservationStatus.PENDING) {
                reservation.setStatus(ReservationStatus.CONFIRMED);
                reservationRepository.save(reservation);
                log.info("Updated reservation {} status from PENDING to CONFIRMED", reservation.getId());

                // Create and associate slots with the reservation
                createAndAssociateSlots(reservation);

                // Send confirmation notification
                sendConfirmationNotification(reservation);
            } else {
                log.warn("Reservation {} is not in PENDING status, current status: {}", 
                        reservation.getId(), reservation.getStatus());
            }

        } catch (Exception e) {
            log.error("Error processing payment completed event for reservation {}: {}", 
                    event.getReservationId(), e.getMessage(), e);
        }
    }

    /**
     * Creates and associates slots with the confirmed reservation.
     */
    private void createAndAssociateSlots(Reservation reservation) {
        try {
            Vehicle vehicle = reservation.getVehicle();
            LocalDateTime startTime = reservation.getStartDate();
            LocalDateTime endTime = reservation.getEndDate();

            // Create a slot for the reservation period
            Slot slot = new Slot();
            slot.setVehicle(vehicle);
            slot.setStartTime(startTime);
            slot.setEndTime(endTime);
            slot.setAvailable(false); // Slot is not available as it's reserved
            slot.setSlotType(determineSlotType(startTime, endTime));
            slot.setPrice(calculateSlotPrice(vehicle, startTime, endTime));
            slot.setReservation(reservation);

            // Save the slot
            Slot savedSlot = slotRepository.save(slot);
            log.info("Created slot {} for reservation {}", savedSlot.getId(), reservation.getId());

            // Add the slot to the reservation
            reservation.getSlots().add(savedSlot);
            reservationRepository.save(reservation);

        } catch (Exception e) {
            log.error("Error creating slots for reservation {}: {}", reservation.getId(), e.getMessage(), e);
        }
    }

    /**
     * Determines the slot type based on duration.
     */
    private SlotType determineSlotType(LocalDateTime startTime, LocalDateTime endTime) {
        Duration duration = Duration.between(startTime, endTime);
        long hours = duration.toHours();
        
        if (hours <= 23) {
            return SlotType.HOURLY;
        } else if (hours <= 168) { // 7 days
            return SlotType.DAILY;
        } else {
            return SlotType.WEEKLY;
        }
    }

    /**
     * Calculates the price for the slot based on vehicle price and duration.
     */
    private BigDecimal calculateSlotPrice(Vehicle vehicle, LocalDateTime startTime, LocalDateTime endTime) {
        Duration duration = Duration.between(startTime, endTime);
        long hours = duration.toHours();
        
        // Default price if vehicle doesn't have a price set
        double dailyRate = vehicle.getPricePerDay() != null ? vehicle.getPricePerDay() : 100.0;
        
        if (hours <= 23) {
            // Hourly pricing (assuming hourly rate is 1/24 of daily rate)
            double hourlyRate = dailyRate / 24.0;
            return BigDecimal.valueOf(hourlyRate * hours);
        } else {
            // Daily pricing with hourly rounding
            long days = (hours + 23) / 24; // Round up to next day
            return BigDecimal.valueOf(dailyRate * days);
        }
    }

    /**
     * Sends confirmation notification to the user.
     */
    private void sendConfirmationNotification(Reservation reservation) {
        try {
            String vehicleInfo = "Unknown Vehicle";
            if (reservation.getVehicle() != null) {
                Vehicle vehicle = reservation.getVehicle();
                String brand = vehicle.getBrand() != null ? vehicle.getBrand().getName() : "Unknown";
                String model = vehicle.getModel() != null ? vehicle.getModel().getName() : "Unknown";
                vehicleInfo = brand + " " + model;
            }

            notificationService.createAndDispatchNotification(
                    reservation.getUser(),
                    NotificationType.RESERVATION_CONFIRMED,
                    "Your reservation for " + vehicleInfo + " has been confirmed! Payment completed successfully.",
                    java.util.Map.of(
                            "reservationId", reservation.getId(),
                            "vehicle", vehicleInfo,
                            "startDate", reservation.getStartDate(),
                            "endDate", reservation.getEndDate()
                    )
            );

            log.info("Sent confirmation notification for reservation {}", reservation.getId());

        } catch (Exception e) {
            log.error("Error sending confirmation notification for reservation {}: {}", 
                    reservation.getId(), e.getMessage(), e);
        }
    }
}
