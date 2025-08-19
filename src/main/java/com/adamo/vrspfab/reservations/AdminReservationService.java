package com.adamo.vrspfab.reservations;

import com.adamo.vrspfab.notifications.NotificationService;
import com.adamo.vrspfab.notifications.NotificationType;
import com.adamo.vrspfab.slots.Slot;
import com.adamo.vrspfab.slots.SlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for handling reservation-related operations for administrators.
 * This service provides unrestricted access to reservation data for management purposes.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AdminReservationService {

    private final ReservationRepository reservationRepository;
    private final NotificationService notificationService;
    private final ReservationMapper reservationMapper;
    private final SlotRepository slotRepository;

    /**
     * Retrieves a paginated list of all reservations, with optional filters.
     *
     * @param filter The filter criteria (e.g., by userId, vehicleId, status).
     * @param pageable Pagination and sorting information.
     * @return A page of reservation summary DTOs.
     */
    @Transactional(readOnly = true)
    public Page<ReservationSummaryDto> getAllReservations(ReservationFilter filter, Pageable pageable) {
        Specification<Reservation> spec = ReservationSpecification.withFilter(filter, null); // Pass null user to bypass security constraint
        return reservationRepository.findAll(spec, pageable).map(reservationMapper::toSummaryDto);
    }

    /**
     * Retrieves any single reservation by its ID.
     *
     * @param id The ID of the reservation.
     * @return A detailed DTO of the reservation.
     * @throws ReservationNotFoundException if no reservation is found.
     */
    @Transactional(readOnly = true)
    public DetailedReservationDto getReservationById(Long id) {
        return reservationRepository.findById(id)
                .map(reservationMapper::toDetailedDto)
                .orElseThrow(() -> new ReservationNotFoundException(id));
    }

    /**
     * Updates the status of a reservation.
     *
     * @param id The ID of the reservation to update.
     * @param request The request containing the new status.
     * @return A detailed DTO of the updated reservation.
     */
    public DetailedReservationDto updateReservationStatus(Long id, AdminReservationUpdateRequest request) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));

        ReservationStatus oldStatus = reservation.getStatus();
        ReservationStatus newStatus = request.getStatus();

        // Validate state transition
        if (!isValidTransition(oldStatus, newStatus)) {
            throw new InvalidReservationStatusException(
                    String.format("Cannot transition from %s to %s.", oldStatus, newStatus)
            );
        }

        if (oldStatus == newStatus) {
            return reservationMapper.toDetailedDto(reservation);
        }

        reservation.setStatus(newStatus);
        Reservation savedReservation = reservationRepository.save(reservation);

        // Notify user on status change
        if (oldStatus == ReservationStatus.PENDING && newStatus == ReservationStatus.CONFIRMED) {
            notificationService.createAndDispatchNotification(
                    savedReservation.getUser(),
                    NotificationType.RESERVATION_CONFIRMED,
                    "Admin has confirmed your reservation for vehicle " + savedReservation.getVehicle().getBrand() + " " + savedReservation.getVehicle().getModel() + ".",
                    java.util.Map.of(
                            "reservationId", savedReservation.getId(),
                            "vehicle", savedReservation.getVehicle().getBrand() + " " + savedReservation.getVehicle().getModel(),
                            "start", savedReservation.getStartDate(),
                            "end", savedReservation.getEndDate()
                    )
            );
        } else if (newStatus == ReservationStatus.CANCELLED) {
            notificationService.createAndDispatchNotification(
                    savedReservation.getUser(),
                    NotificationType.RESERVATION_CANCELLED,
                    "Your reservation for vehicle " + savedReservation.getVehicle().getBrand() + " " + savedReservation.getVehicle().getModel() + " has been cancelled by an admin.",
                    java.util.Map.of(
                            "reservationId", savedReservation.getId(),
                            "vehicle", savedReservation.getVehicle().getBrand() + " " + savedReservation.getVehicle().getModel()
                    )
            );
        }

        // Update slot availability if cancelled
        if (newStatus == ReservationStatus.CANCELLED && savedReservation.getSlot() != null) {
            Slot slot = savedReservation.getSlot();
            slot.setAvailable(true);
            slotRepository.save(slot);
        }

        return reservationMapper.toDetailedDto(savedReservation);
    }


    /**
     * Validates if the transition from one reservation status to another is allowed.
     *
     * @param from The current status of the reservation.
     * @param to The new status to transition to.
     * @return true if the transition is valid, false otherwise.
     */
    private boolean isValidTransition(
            ReservationStatus from,
            ReservationStatus to
    ) {
        return switch (from) {
            case PENDING ->
                    to == ReservationStatus.CONFIRMED || to == ReservationStatus.CANCELLED;
            case CONFIRMED ->
                    to == ReservationStatus.COMPLETED || to == ReservationStatus.CANCELLED;
            case CANCELLED, COMPLETED -> false; // No transitions allowed from these states
        };
    }


    /**
     * Deletes a reservation from the system.
     *
     * @param id The ID of the reservation to delete.
     */
    public void deleteReservation(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new ReservationNotFoundException(id);
        }
        reservationRepository.deleteById(id);
    }
}