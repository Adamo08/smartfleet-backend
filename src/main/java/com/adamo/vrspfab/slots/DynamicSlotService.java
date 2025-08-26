package com.adamo.vrspfab.slots;

import com.adamo.vrspfab.reservations.Reservation;
import com.adamo.vrspfab.reservations.ReservationRepository;
import com.adamo.vrspfab.vehicles.Vehicle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for dynamically generating slots based on vehicle availability.
 * This eliminates the need for manual slot creation and provides flexible booking options.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DynamicSlotService {

    private final ReservationRepository reservationRepository;

    /**
     * Generates available slots for a vehicle within a date range.
     * Slots are generated dynamically based on existing reservations and vehicle availability.
     */
    @Transactional(readOnly = true)
    public List<SlotDto> generateAvailableSlots(Vehicle vehicle, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Generating available slots for vehicle {} from {} to {}", vehicle.getId(), startDate, endDate);

        // Check for conflicting reservations
        List<Reservation> conflictingReservations = findConflictingReservations(vehicle.getId(), startDate, endDate);
        
        if (!conflictingReservations.isEmpty()) {
            log.debug("Found {} conflicting reservations for vehicle {}", conflictingReservations.size(), vehicle.getId());
            return generateSlotsWithConflicts(vehicle, startDate, endDate, conflictingReservations);
        }

        // No conflicts, generate a single available slot
        return List.of(createAvailableSlot(vehicle, startDate, endDate));
    }

    /**
     * Checks if a vehicle is available for the specified date range.
     */
    @Transactional(readOnly = true)
    public boolean isVehicleAvailable(Vehicle vehicle, LocalDateTime startDate, LocalDateTime endDate) {
        List<Reservation> conflicts = findConflictingReservations(vehicle.getId(), startDate, endDate);
        return conflicts.isEmpty();
    }

    /**
     * Calculates the price for a rental period.
     */
    public BigDecimal calculatePrice(Vehicle vehicle, LocalDateTime startDate, LocalDateTime endDate) {
        Duration duration = Duration.between(startDate, endDate);
        long hours = duration.toHours();
        
        // Default price if vehicle doesn't have a price set
        BigDecimal dailyRate = vehicle.getPricePerDay() != null ? BigDecimal.valueOf(vehicle.getPricePerDay()) : BigDecimal.valueOf(100.0);
        
        if (hours <= 23) {
            // Hourly pricing (assuming hourly rate is 1/24 of daily rate)
            BigDecimal hourlyRate = dailyRate.divide(BigDecimal.valueOf(24), 2, BigDecimal.ROUND_HALF_UP);
            return hourlyRate.multiply(BigDecimal.valueOf(hours));
        } else {
            // Daily pricing with hourly rounding
            long days = (hours + 23) / 24; // Round up to next day
            return dailyRate.multiply(BigDecimal.valueOf(days));
        }
    }

    /**
     * Finds reservations that conflict with the requested date range.
     */
    private List<Reservation> findConflictingReservations(Long vehicleId, LocalDateTime startDate, LocalDateTime endDate) {
        return reservationRepository.findOverlappingReservations(vehicleId, startDate, endDate);
    }

    /**
     * Generates slots when there are conflicting reservations.
     */
    private List<SlotDto> generateSlotsWithConflicts(Vehicle vehicle, LocalDateTime startDate, LocalDateTime endDate, 
                                                     List<Reservation> conflicts) {
        List<SlotDto> availableSlots = new ArrayList<>();
        
        // Sort conflicts by start date
        conflicts.sort((a, b) -> a.getStartDate().compareTo(b.getStartDate()));
        
        LocalDateTime currentTime = startDate;
        
        for (Reservation conflict : conflicts) {
            // If there's a gap before this conflict, create an available slot
            if (currentTime.isBefore(conflict.getStartDate())) {
                availableSlots.add(createAvailableSlot(vehicle, currentTime, conflict.getStartDate()));
            }
            
            // Create unavailable slot for the conflict period
            availableSlots.add(createUnavailableSlot(vehicle, conflict.getStartDate(), conflict.getEndDate()));
            
            currentTime = conflict.getEndDate();
        }
        
        // If there's time remaining after the last conflict, create an available slot
        if (currentTime.isBefore(endDate)) {
            availableSlots.add(createAvailableSlot(vehicle, currentTime, endDate));
        }
        
        return availableSlots;
    }

    /**
     * Creates an available slot.
     */
    private SlotDto createAvailableSlot(Vehicle vehicle, LocalDateTime startTime, LocalDateTime endTime) {
        SlotDto slot = new SlotDto();
        slot.setVehicleId(vehicle.getId());
        slot.setStartTime(startTime);
        slot.setEndTime(endTime);
        slot.setAvailable(true);
        slot.setSlotType(determineSlotType(startTime, endTime));
        slot.setPrice(calculatePrice(vehicle, startTime, endTime));
        slot.setVehicleBrand(vehicle.getBrand() != null ? vehicle.getBrand().getName() : "Unknown");
        slot.setVehicleModel(vehicle.getModel() != null ? vehicle.getModel().getName() : "Unknown");
        slot.setCreatedAt(LocalDateTime.now());
        slot.setUpdatedAt(LocalDateTime.now());
        return slot;
    }

    /**
     * Creates an unavailable slot.
     */
    private SlotDto createUnavailableSlot(Vehicle vehicle, LocalDateTime startTime, LocalDateTime endTime) {
        SlotDto slot = new SlotDto();
        slot.setVehicleId(vehicle.getId());
        slot.setStartTime(startTime);
        slot.setEndTime(endTime);
        slot.setAvailable(false);
        slot.setSlotType(determineSlotType(startTime, endTime));
        slot.setPrice(calculatePrice(vehicle, startTime, endTime));
        slot.setVehicleBrand(vehicle.getBrand() != null ? vehicle.getBrand().getName() : "Unknown");
        slot.setVehicleModel(vehicle.getModel() != null ? vehicle.getModel().getName() : "Unknown");
        slot.setCreatedAt(LocalDateTime.now());
        slot.setUpdatedAt(LocalDateTime.now());
        return slot;
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
}
