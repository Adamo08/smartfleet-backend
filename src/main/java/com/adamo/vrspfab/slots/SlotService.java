package com.adamo.vrspfab.slots;

import com.adamo.vrspfab.common.ResourceNotFoundException;
import com.adamo.vrspfab.common.SecurityUtilsService;
import com.adamo.vrspfab.vehicles.VehicleMapper;
import com.adamo.vrspfab.vehicles.VehicleService;
import com.adamo.vrspfab.users.Role;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger; // Import for logging
import org.slf4j.LoggerFactory; // Import for logging
import org.springframework.security.access.AccessDeniedException; // Import for access control

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class SlotService {

    private static final Logger logger = LoggerFactory.getLogger(SlotService.class);

    private final SlotRepository slotRepository;
    private final SlotMapper slotMapper;
    private final VehicleService vehicleService;
    private final SecurityUtilsService securityUtilsService; // Inject SecurityUtilsService
    private final VehicleMapper vehicleMapper;
    
    @Transactional(readOnly = true)
    public List<SlotDto> getAllSlotsByVehicle(Long vehicleId, LocalDateTime start, LocalDateTime end) {
        // Public endpoint to power booking UI: returns all slots for a vehicle, optionally filtered by range
        List<Slot> slots;
        if (start != null && end != null) {
            slots = slotRepository.findByVehicleIdAndStartTimeBetween(vehicleId, start, end);
        } else {
            slots = slotRepository.findByVehicle_Id(vehicleId);
        }
        return slots.stream().map(slotMapper::toDto).collect(Collectors.toList());
    }

    @Transactional
    public SlotDto createSlot(SlotDto slotDto) {
        logger.info("Attempting to create slot: {}", slotDto);

        // Security check: Only ADMIN can create slots
        if (!securityUtilsService.getCurrentAuthenticatedUser().getRole().equals(Role.ADMIN)) {
            logger.warn("Non-ADMIN user attempted to create a slot.");
            throw new AccessDeniedException("Only administrators can create slots.");
        }

        // Basic time validation: startTime must be before endTime
        if (slotDto.getStartTime().isAfter(slotDto.getEndTime()) || slotDto.getStartTime().isEqual(slotDto.getEndTime())) {
            logger.warn("Invalid slot time: startTime {} is not before endTime {}", slotDto.getStartTime(), slotDto.getEndTime());
            throw new InvalidSlotTimeException("Slot start time must be strictly before end time.");
        }

        // Check for overlapping slots for the same vehicle
        List<Slot> overlappingSlots = slotRepository.findOverlappingSlots(
                slotDto.getVehicleId(),
                slotDto.getStartTime(),
                slotDto.getEndTime()
        );
        if (!overlappingSlots.isEmpty()) {
            logger.warn("New slot for vehicle {} from {} to {} overlaps with existing slots.",
                    slotDto.getVehicleId(), slotDto.getStartTime(), slotDto.getEndTime());
            throw new InvalidSlotTimeException("The proposed slot overlaps with an existing slot for this vehicle.");
        }

        Slot slot = slotMapper.toEntity(slotDto);
        slot.setVehicle(vehicleMapper.toEntity(vehicleService.getVehicleById(slotDto.getVehicleId())));
        slot.setAvailable(true); // Default to available on creation

        Slot savedSlot = slotRepository.save(slot);
        logger.info("Slot created successfully with ID: {}", savedSlot.getId());
        return slotMapper.toDto(savedSlot);
    }

    @Transactional(readOnly = true)
    public SlotDto getSlotById(Long id) {
        logger.debug("Fetching slot with ID: {}", id);

        // Security check: Only ADMIN can view any slot details.
        // For general users, they might only see available slots via specific endpoints.
        if (!securityUtilsService.getCurrentAuthenticatedUser().getRole().equals(Role.ADMIN)) {
            logger.warn("Non-ADMIN user attempted to retrieve slot ID {}.", id);
            throw new AccessDeniedException("Only administrators can view specific slot details.");
        }

        Slot slot = slotRepository.findWithVehicleById(id)
                .orElseThrow(() -> {
                    logger.warn("Slot not found with ID: {}", id);
                    return new ResourceNotFoundException("Slot not found with ID: " + id, "Slot");
                });
        return slotMapper.toDto(slot);
    }

    @Transactional
    public SlotDto bookSlot(Long id) {
        logger.info("Attempting to book slot with ID: {}", id);

        // Security check: Only ADMIN can book/block slots directly (often done via Reservation process)
        if (!securityUtilsService.getCurrentAuthenticatedUser().getRole().equals(Role.ADMIN)) {
            logger.warn("Non-ADMIN user attempted to book slot ID {}.", id);
            throw new AccessDeniedException("Only administrators can book slots directly.");
        }

        Slot slot = slotRepository.findWithVehicleById(id)
                .orElseThrow(() -> {
                    logger.warn("Slot not found with ID: {}", id);
                    return new ResourceNotFoundException("Slot not found with ID: " + id, "Slot");
                });

        if (!slot.isAvailable()) {
            logger.warn("Slot {} is not available for booking.", id);
            throw new InvalidSlotStateException("Slot must be available to book");
        }
        slot.setAvailable(false); // Mark as unavailable (booked)
        Slot updatedSlot = slotRepository.save(slot);
        logger.info("Slot with ID: {} booked successfully.", id);
        return slotMapper.toDto(updatedSlot);
    }

    @Transactional
    public SlotDto blockSlot(Long id) {
        logger.info("Attempting to block slot with ID: {}", id);

        // Security check: Only ADMIN can book/block slots directly
        if (!securityUtilsService.getCurrentAuthenticatedUser().getRole().equals(Role.ADMIN)) {
            logger.warn("Non-ADMIN user attempted to block slot ID {}.", id);
            throw new AccessDeniedException("Only administrators can block slots directly.");
        }

        Slot slot = slotRepository.findWithVehicleById(id)
                .orElseThrow(() -> {
                    logger.warn("Slot not found with ID: {}", id);
                    return new ResourceNotFoundException("Slot not found with ID: " + id, "Slot");
                });

        if (!slot.isAvailable()) {
            logger.warn("Slot {} is already unavailable (booked or blocked).", id);
            throw new InvalidSlotStateException("Slot must be available to block");
        }
        slot.setAvailable(false); // Treat as blocked (unavailable)
        Slot updatedSlot = slotRepository.save(slot);
        logger.info("Slot with ID: {} blocked successfully.", id);
        return slotMapper.toDto(updatedSlot);
    }

    @Transactional
    public void deleteSlot(Long id) {
        logger.info("Attempting to delete slot with ID: {}", id);

        // Security check: Only ADMIN can delete slots
        if (!securityUtilsService.getCurrentAuthenticatedUser().getRole().equals(Role.ADMIN)) {
            logger.warn("Non-ADMIN user attempted to delete slot ID {}.", id);
            throw new AccessDeniedException("Only administrators can delete slots.");
        }

        Slot slot = slotRepository.findWithVehicleById(id)
                .orElseThrow(() -> {
                    logger.warn("Slot not found with ID: {}", id);
                    return new ResourceNotFoundException("Slot not found with ID: " + id, "Slot");
                });
        slotRepository.delete(slot);
        logger.info("Slot with ID: {} deleted successfully.", id);
    }

    @Transactional(readOnly = true)
    public Page<SlotDto> getAllSlots(int page, int size, Boolean isAvailable, String sortBy, String sortDirection) {
        logger.debug("Fetching all slots with filters: page={}, size={}, isAvailable={}, sortBy={}, sortDirection={}",
                page, size, isAvailable, sortBy, sortDirection);

        // Security check: Only ADMIN can view all slots (with or without availability filter)
        if (!securityUtilsService.getCurrentAuthenticatedUser().getRole().equals(Role.ADMIN)) {
            logger.warn("Non-ADMIN user attempted to view all slots.");
            throw new AccessDeniedException("Only administrators can view all slots.");
        }

        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Slot> slotsPage;

        if (isAvailable != null) {
            slotsPage = slotRepository.findByAvailable(isAvailable, pageable);
        } else {
            slotsPage = slotRepository.findAll(pageable);
        }

        logger.debug("Fetched {} slots for page {} with size {}. Total elements: {}",
                slotsPage.getNumberOfElements(), page, size, slotsPage.getTotalElements());
        return slotsPage.map(slotMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<SlotDto> getAvailableSlotsByVehicleId(Long vehicleId) {
        logger.debug("Fetching available slots for vehicle ID: {}", vehicleId);
        // This endpoint is intended for customers/public to find available cars, so no role check here.
        List<Slot> slots = slotRepository.findAvailableSlotsByVehicleId(vehicleId);
        logger.debug("Found {} available slots for vehicle ID: {}", slots.size(), vehicleId);
        return slots.stream().map(slotMapper::toDto).toList();
    }
}
