package com.adamo.vrspfab.reservations;

import com.adamo.vrspfab.common.ResourceNotFoundException;
import com.adamo.vrspfab.common.SecurityUtilsService;
import com.adamo.vrspfab.slots.NoAvailableSlotsException;
import com.adamo.vrspfab.notifications.NotificationService;
import com.adamo.vrspfab.notifications.NotificationType;
import com.adamo.vrspfab.dashboard.ActivityEventListener;
import com.adamo.vrspfab.slots.SlotRepository;
import com.adamo.vrspfab.slots.SlotDto;
import com.adamo.vrspfab.slots.DynamicSlotService;
import com.adamo.vrspfab.users.Role;
import com.adamo.vrspfab.users.User;
import com.adamo.vrspfab.vehicles.mappers.VehicleMapper;
import com.adamo.vrspfab.vehicles.VehicleService;
import com.adamo.vrspfab.vehicles.Vehicle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private final DynamicSlotService dynamicSlotService;
    private final SecurityUtilsService securityUtilsService;
    private final com.adamo.vrspfab.vehicles.VehicleCategoryRepository vehicleCategoryRepository;
    private final com.adamo.vrspfab.vehicles.VehicleBrandRepository vehicleBrandRepository;
    private final com.adamo.vrspfab.vehicles.VehicleModelRepository vehicleModelRepository;
    private final ActivityEventListener activityEventListener;

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
        
        // Create vehicle entity and set relationships manually
        Vehicle vehicleEntity = vehicleMapper.toEntity(vehicleDto);
        // Set the relationships manually since the mapper ignores them
        if (vehicleDto.getCategoryId() != null) {
            vehicleEntity.setCategory(vehicleCategoryRepository.findById(vehicleDto.getCategoryId()).orElse(null));
        }
        if (vehicleDto.getBrandId() != null) {
            vehicleEntity.setBrand(vehicleBrandRepository.findById(vehicleDto.getBrandId()).orElse(null));
        }
        if (vehicleDto.getModelId() != null) {
            vehicleEntity.setModel(vehicleModelRepository.findById(vehicleDto.getModelId()).orElse(null));
        }
        
        reservation.setVehicle(vehicleEntity);
        reservation.setStatus(ReservationStatus.PENDING);
        
        // Save reservation
        Reservation savedReservation = reservationRepository.save(reservation);

        // Record activity
        activityEventListener.recordReservationCreated(savedReservation);

        // Send notification
        notificationService.createAndDispatchNotification(
                currentUser,
                NotificationType.RESERVATION_PENDING,
                "Your reservation for vehicle " + vehicleDto.getBrandName() + " " + vehicleDto.getModelName() + " is now pending.",
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
                "Your reservation for vehicle " + savedReservation.getVehicle().getBrand().getName() + " " + savedReservation.getVehicle().getModel().getName() + " has been cancelled.",
                java.util.Map.of(
                        "reservationId", savedReservation.getId(),
                        "vehicle", savedReservation.getVehicle().getBrand() + " " + savedReservation.getVehicle().getModel()
                )
        );

        return reservationMapper.toDetailedDto(savedReservation);
    }

    /**
     * Get blocked dates for a vehicle based on existing reservations
     */
    public List<String> getBlockedDatesForVehicle(Long vehicleId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Reservation> overlappingReservations = reservationRepository.findOverlappingReservations(
            vehicleId, startDate, endDate);
        
        Set<String> blockedDates = new HashSet<>();
        
        for (Reservation reservation : overlappingReservations) {
            LocalDateTime current = reservation.getStartDate().toLocalDate().atStartOfDay();
            LocalDateTime reservationEnd = reservation.getEndDate().toLocalDate().atStartOfDay();
            
            while (current.isBefore(reservationEnd) || current.isEqual(reservationEnd)) {
                blockedDates.add(current.toLocalDate().toString());
                current = current.plusDays(1);
            }
        }
        
        return new ArrayList<>(blockedDates);
    }

    /**
     * Gets available slots for a vehicle within a date range, supporting different booking types.
     *
     * @param vehicleId The ID of the vehicle
     * @param startDate The start date for slot search
     * @param endDate The end date for slot search
     * @param bookingType The type of booking (HOURLY, DAILY, WEEKLY, CUSTOM)
     * @return List of available slots
     */
    @Transactional(readOnly = true)
    public List<SlotDto> getAvailableSlots(Long vehicleId, LocalDateTime startDate, LocalDateTime endDate, String bookingType) {
        log.debug("Getting available slots for vehicle {} from {} to {} with booking type {}", vehicleId, startDate, endDate, bookingType);
        
        // Get the vehicle
        var vehicledto = vehicleService.getVehicleById(vehicleId);
        Vehicle vehicle = vehicleMapper.toEntity(vehicledto);
        
        // Generate slots based on booking type
        return generateSlotsByBookingType(vehicle, startDate, endDate, bookingType);
    }

    /**
     * Generates slots based on the booking type.
     */
    private List<SlotDto> generateSlotsByBookingType(Vehicle vehicle, LocalDateTime startDate, LocalDateTime endDate, String bookingType) {
        List<SlotDto> allSlots;
        
        switch (bookingType.toUpperCase()) {
            case "HOURLY":
                allSlots = generateHourlySlots(vehicle, startDate, endDate);
                break;
            case "DAILY":
                allSlots = generateDailySlots(vehicle, startDate, endDate);
                break;
            case "WEEKLY":
                allSlots = generateWeeklySlots(vehicle, startDate, endDate);
                break;
            case "CUSTOM":
            default:
                // For custom or default, use the dynamic slot service
                allSlots = dynamicSlotService.generateAvailableSlots(vehicle, startDate, endDate);
                break;
        }
        
        return allSlots;
    }

    /**
     * Generates hourly slots for the vehicle.
     */
    private List<SlotDto> generateHourlySlots(Vehicle vehicle, LocalDateTime startDate, LocalDateTime endDate) {
        List<SlotDto> slots = new ArrayList<>();
        LocalDateTime current = startDate;
        
        while (current.isBefore(endDate)) {
            LocalDateTime slotEnd = current.plusHours(1);
            if (slotEnd.isAfter(endDate)) {
                slotEnd = endDate;
            }
            
            // Check if this hour slot is available
            List<SlotDto> hourSlots = dynamicSlotService.generateAvailableSlots(vehicle, current, slotEnd);
            slots.addAll(hourSlots);
            
            current = current.plusHours(1);
        }
        
        return slots;
    }

    /**
     * Generates daily slots for the vehicle.
     */
    private List<SlotDto> generateDailySlots(Vehicle vehicle, LocalDateTime startDate, LocalDateTime endDate) {
        List<SlotDto> slots = new ArrayList<>();
        LocalDateTime current = startDate.toLocalDate().atStartOfDay();
        
        while (current.isBefore(endDate)) {
            LocalDateTime slotEnd = current.plusDays(1);
            if (slotEnd.isAfter(endDate)) {
                slotEnd = endDate;
            }
            
            // Check if this day is available
            List<SlotDto> daySlots = dynamicSlotService.generateAvailableSlots(vehicle, current, slotEnd);
            slots.addAll(daySlots);
            
            current = current.plusDays(1);
        }
        
        return slots;
    }

    /**
     * Generates weekly slots for the vehicle.
     */
    private List<SlotDto> generateWeeklySlots(Vehicle vehicle, LocalDateTime startDate, LocalDateTime endDate) {
        List<SlotDto> slots = new ArrayList<>();
        LocalDateTime current = startDate.toLocalDate().atStartOfDay();
        
        while (current.isBefore(endDate)) {
            LocalDateTime slotEnd = current.plusWeeks(1);
            if (slotEnd.isAfter(endDate)) {
                slotEnd = endDate;
            }
            
            // Check if this week is available
            List<SlotDto> weekSlots = dynamicSlotService.generateAvailableSlots(vehicle, current, slotEnd);
            slots.addAll(weekSlots);
            
            current = current.plusWeeks(1);
        }
        
        return slots;
    }
}