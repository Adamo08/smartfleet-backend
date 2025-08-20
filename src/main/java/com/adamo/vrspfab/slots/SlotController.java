package com.adamo.vrspfab.slots;

import jakarta.validation.Valid; // Import Valid annotation
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page; // Import for pagination
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger; // Import for logging
import org.slf4j.LoggerFactory; // Import for logging

import java.util.List;


@AllArgsConstructor
@RestController
@RequestMapping("/slots")
public class SlotController {

    private static final Logger logger = LoggerFactory.getLogger(SlotController.class);

    private final SlotService slotService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SlotDto createSlot(@Valid @RequestBody SlotDto slotDto) {
        logger.info("Received request to create slot: {}", slotDto);
        return slotService.createSlot(slotDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SlotDto> getSlot(@PathVariable Long id) {
        logger.info("Received request to get slot with ID: {}", id);
        return ResponseEntity.ok(slotService.getSlotById(id));
    }

    @GetMapping("/vehicle/{vehicleId}/available")
    public ResponseEntity<List<SlotDto>> getAvailableSlots(@PathVariable Long vehicleId) {
        logger.info("Received request to get available slots for vehicle ID: {}", vehicleId);
        List<SlotDto> slots = slotService.getAvailableSlotsByVehicleId(vehicleId);
        return ResponseEntity.ok(slots);
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<SlotDto>> getAllSlotsForVehicle(
            @PathVariable Long vehicleId,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end
    ) {
        logger.info("Received request to get all slots (any availability) for vehicle ID: {}", vehicleId);
        java.time.LocalDateTime startTime = null;
        java.time.LocalDateTime endTime = null;
        if (start != null && end != null) {
            startTime = java.time.LocalDateTime.parse(start);
            endTime = java.time.LocalDateTime.parse(end);
        }
        return ResponseEntity.ok(slotService.getAllSlotsByVehicle(vehicleId, startTime, endTime));
    }

    @PutMapping("/{id}/book")
    public ResponseEntity<SlotDto> bookSlot(@PathVariable Long id) {
        logger.info("Received request to book slot with ID: {}", id);
        return ResponseEntity.ok(slotService.bookSlot(id));
    }

    @PutMapping("/{id}/block")
    public ResponseEntity<SlotDto> blockSlot(@PathVariable Long id) {
        logger.info("Received request to block slot with ID: {}", id);
        return ResponseEntity.ok(slotService.blockSlot(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSlot(@PathVariable Long id) {
        logger.info("Received request to delete slot with ID: {}", id);
        slotService.deleteSlot(id);
    }

    @GetMapping
    public ResponseEntity<Page<SlotDto>> getAllSlots(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean isAvailable,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        logger.info("Received request to get all slots with filters: page={}, size={}, isAvailable={}, sortBy={}, sortDirection={}",
                page, size, isAvailable, sortBy, sortDirection);
        Page<SlotDto> slotsPage = slotService.getAllSlots(page, size, isAvailable, sortBy, sortDirection);
        return ResponseEntity.ok(slotsPage);
    }
}
