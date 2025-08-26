package com.adamo.vrspfab.reservations;

import com.adamo.vrspfab.common.ResourceNotFoundException;
import com.adamo.vrspfab.common.SecurityUtilsService;
import com.adamo.vrspfab.slots.NoAvailableSlotsException;
import com.adamo.vrspfab.notifications.NotificationService;
import com.adamo.vrspfab.notifications.NotificationType;
import com.adamo.vrspfab.slots.SlotRepository;
import com.adamo.vrspfab.users.Role;
import com.adamo.vrspfab.users.User;
import com.adamo.vrspfab.vehicles.VehicleMapper;
import com.adamo.vrspfab.vehicles.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class for handling reservation-related operations for authenticated users.
 * This service enforces business rules and security constraints for user-facing actions.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final VehicleService vehicleService;
    private final VehicleMapper vehicleMapper;
    private final SecurityUtilsService securityUtils;
    private final NotificationService notificationService;
    private final ReservationMapper reservationMapper;
    private final SlotRepository slotRepository;
    private final SecurityUtilsService securityUtilsService;

    /**
     * Creates a new reservation for the currently authenticated user.
     *
     * @param request The request DTO containing reservation details.
     * @return A detailed DTO of the newly created reservation.
     * @throws NoAvailableSlotsException if no suitable slots are found for the requested dates.
     * @throws ReservationConflictException if the vehicle is already booked for the selected dates.
     */
    @Transactional
    public DetailedReservationDto createReservation(CreateReservationRequest request) {
        User currentUser = securityUtils.getCurrentAuthenticatedUser();
        var vehicleDto = vehicleService.getVehicleById(request.getVehicleId());

        // Check if vehicle is available for the requested date range
        List<Reservation> conflictingReservations = reservationRepository.findOverlappingReservations(
                request.getVehicleId(),
                request.getStartDate(),
                request.getEndDate()
        );

        if (!conflictingReservations.isEmpty()) {
            throw new NoAvailableSlotsException("Vehicle is not available for the requested date range due to existing reservations.");
        }

        // Create reservation
        Reservation reservation = reservationMapper.fromCreateRequest(request);
        reservation.setUser(currentUser);
        reservation.setVehicle(vehicleMapper.toEntity(vehicleDto));
        reservation.setStatus(ReservationStatus.PENDING);
        
        // Save reservation
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

    @Transactional(readOnly = true)
    public Page<ReservationSummaryDto> getUserReservations(Pageable pageable) {
        User currentUser = securityUtilsService.getCurrentAuthenticatedUser();
        Page<Reservation> reservations = reservationRepository.findByUserId(currentUser.getId(), pageable);
        return reservations.map(reservationMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public Page<ReservationSummaryDto> getUserReservationsWithFilter(ReservationFilter filter, Pageable pageable) {
        User currentUser = securityUtilsService.getCurrentAuthenticatedUser();
        
        // Create a specification that enforces user ownership and applies filters
        Specification<Reservation> spec = ReservationSpecification.withFilter(filter, currentUser);
        
        Page<Reservation> reservations = reservationRepository.findAll(spec, pageable);
        return reservations.map(reservationMapper::toSummaryDto);
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

        // Security Check: Ensure the user is the owner of the reservation OR an admin
        boolean isOwner = reservation.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        
        log.info("Security check for reservation {}: currentUser={}, reservationOwner={}, isOwner={}, isAdmin={}", 
                id, currentUser.getId(), reservation.getUser().getId(), isOwner, isAdmin);
        
        if (!isOwner && !isAdmin) {
            log.warn("Access denied for reservation {}: user {} is not owner and not admin", 
                    id, currentUser.getId());
            throw new AccessDeniedException("You do not have permission to view this reservation.");
        }
        
        log.info("Access granted for reservation {}: user {} (owner: {}, admin: {})", 
                id, currentUser.getId(), isOwner, isAdmin);

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

        // Restore slot availability for all associated slots
        if (!reservation.getSlots().isEmpty()) {
            reservation.getSlots().forEach(slot -> {
                slot.setAvailable(true);
                slot.setReservation(null); // Dissociate from reservation
                slotRepository.save(slot);
            });
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