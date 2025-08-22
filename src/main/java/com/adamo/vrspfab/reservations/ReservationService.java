package com.adamo.vrspfab.reservations;

import com.adamo.vrspfab.common.ResourceNotFoundException;
import com.adamo.vrspfab.common.SecurityUtilsService;
import com.adamo.vrspfab.notifications.NotificationService;
import com.adamo.vrspfab.notifications.NotificationType;
import com.adamo.vrspfab.slots.*;
import com.adamo.vrspfab.users.Role;
import com.adamo.vrspfab.users.User;
import com.adamo.vrspfab.vehicles.VehicleMapper;
import com.adamo.vrspfab.vehicles.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for handling reservation-related operations for authenticated users.
 * This service enforces business rules and security constraints for user-facing actions.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final VehicleService vehicleService;
    private final VehicleMapper vehicleMapper;
    private final SecurityUtilsService securityUtils;
    private final NotificationService notificationService;
    private final ReservationMapper reservationMapper;
    private final SlotRepository slotRepository;

    /**
     * Creates a new reservation for the currently authenticated user.
     *
     * @param request The request DTO containing reservation details.
     * @return A detailed DTO of the newly created reservation.
     * @throws InvalidReservationDateException if the start/end dates are invalid.
     * @throws ReservationConflictException if the vehicle is already booked for the selected dates.
     */
    @Transactional
    public DetailedReservationDto createReservation(CreateReservationRequest request) {
        User currentUser = securityUtils.getCurrentAuthenticatedUser();
        var vehicleDto = vehicleService.getVehicleById(request.getVehicleId());

        // Validate slot
        Slot slot = slotRepository.findWithVehicleById(request.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found with ID: " + request.getSlotId(), "Slot"));
        if (!slot.isAvailable()) {
            throw new InvalidSlotStateException("Selected slot is not available.");
        }
        if (!slot.getVehicle().getId().equals(request.getVehicleId())) {
            throw new InvalidSlotStateException("Selected slot does not belong to the requested vehicle.");
        }

        if (!slot.getStartTime().equals(request.getStartDate()) || !slot.getEndTime().equals(request.getEndDate())) {
            throw new InvalidSlotTimeException("Reservation dates must match the selected slot's start and end times.");
        }

        // Check for overlapping reservations (optional, as slot availability should suffice)
        if (!reservationRepository.findOverlappingReservations(request.getVehicleId(), request.getStartDate(), request.getEndDate()).isEmpty()) {
            throw new ReservationConflictException("Vehicle is unavailable for the selected dates.");
        }

        // Book the slot
        slot.setAvailable(false);
        slotRepository.save(slot);

        // Create reservation
        Reservation reservation = reservationMapper.fromCreateRequest(request);
        reservation.setUser(currentUser);
        reservation.setVehicle(vehicleMapper.toEntity(vehicleDto));
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setSlot(slot); // Link the slot to the reservation

        Reservation savedReservation = reservationRepository.save(reservation);

        // Send notification
        notificationService.createAndDispatchNotification(
                currentUser,
                NotificationType.RESERVATION_PENDING,
                "Your reservation for vehicle " + vehicleDto.getBrand() + " " + vehicleDto.getModel() + " is now pending.",
                java.util.Map.of(
                        "reservationId", savedReservation.getId(),
                        "vehicle", vehicleDto.getBrand() + " " + vehicleDto.getModel(),
                        "start", savedReservation.getStartDate(),
                        "end", savedReservation.getEndDate()
                )
        );

        return reservationMapper.toDetailedDto(savedReservation);
    }

    public Long countAllReservations() {
        return reservationRepository.count();
    }

    public Long countReservationsByStatus(String status) {
        return reservationRepository.countByStatus(ReservationStatus.valueOf(status.toUpperCase()));
    }

    /**
     * Retrieves a reservation by its ID, including user and vehicle details.
     *
     * @param id The ID of the reservation.
     * @return A detailed DTO of the reservation.
     * @throws ResourceNotFoundException if no reservation is found with the given ID.
     */
    @Transactional(readOnly = true)
    public ReservationDto getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
        return reservationMapper.toDto(reservation);
    }


    /**
     * Retrieves a paginated list of reservations for the currently authenticated user.
     *
     * @param pageable Pagination and sorting information.
     * @return A page of reservation summary DTOs.
     */
    @Transactional(readOnly = true)
    public Page<ReservationSummaryDto> getReservationsForCurrentUser(Pageable pageable) {
        User currentUser = securityUtils.getCurrentAuthenticatedUser();
        var filter = ReservationFilter.builder().build(); // Empty filter, spec will apply user constraint
        Specification<Reservation> spec = ReservationSpecification.withFilter(filter, currentUser);
        return reservationRepository.findAll(spec, pageable).map(reservationMapper::toSummaryDto);
    }

    /**
     * Retrieves a single reservation by its ID, ensuring it belongs to the current user.
     *
     * @param id The ID of the reservation.
     * @return A detailed DTO of the reservation.
     * @throws ReservationNotFoundException if no reservation is found.
     * @throws AccessDeniedException if the reservation does not belong to the current user.
     */
    @Transactional(readOnly = true)
    public DetailedReservationDto getReservationByIdForCurrentUser(Long id) {
        User currentUser = securityUtils.getCurrentAuthenticatedUser();
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));

        // Security Check: Ensure the user is the owner of the reservation. (Or an admin)
        if (!reservation.getUser().getId().equals(currentUser.getId()) || currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You do not have permission to view this reservation.");
        }

        return reservationMapper.toDetailedDto(reservation);
    }

    /**
     * Cancels a reservation owned by the currently authenticated user.
     *
     * @param id The ID of the reservation to cancel.
     * @return A detailed DTO of the cancelled reservation.
     * @throws InvalidReservationStatusException if the reservation is not in a cancellable state.
     */
    public DetailedReservationDto cancelReservation(Long id) {
        User currentUser = securityUtils.getCurrentAuthenticatedUser();
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));

        if (!reservation.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to cancel this reservation.");
        }
        if (reservation.getStatus() != ReservationStatus.PENDING && reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new InvalidReservationStatusException("Reservation cannot be cancelled in its current state: " + reservation.getStatus());
        }

        // Restore slot availability
        if (reservation.getSlot() != null) {
            Slot slot = reservation.getSlot();
            slot.setAvailable(true);
            slotRepository.save(slot);
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        Reservation savedReservation = reservationRepository.save(reservation);

        notificationService.createAndDispatchNotification(
                currentUser,
                NotificationType.RESERVATION_CANCELLED,
                "Your reservation for vehicle " + savedReservation.getVehicle().getBrand() + " " + savedReservation.getVehicle().getModel() + " has been cancelled.",
                java.util.Map.of(
                        "reservationId", savedReservation.getId(),
                        "vehicle", savedReservation.getVehicle().getBrand() + " " + savedReservation.getVehicle().getModel()
                )
        );

        return reservationMapper.toDetailedDto(savedReservation);
    }
}