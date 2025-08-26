package com.adamo.vrspfab.slots;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid; // Import Valid annotation
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page; // Import for pagination
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger; // Import for logging
import org.slf4j.LoggerFactory; // Import for logging

import java.util.List;
import java.time.OffsetDateTime; // Import OffsetDateTime
import java.time.format.DateTimeParseException;


@Tag(name = "Slot Management", description = "APIs for managing vehicle slots")
@AllArgsConstructor
@RestController
@RequestMapping("/slots")
public class SlotController {

    private static final Logger logger = LoggerFactory.getLogger(SlotController.class);

    private final SlotService slotService;

    @Operation(summary = "Create a new slot",
               description = "Creates a new slot for a vehicle.",
               responses = {
                       @ApiResponse(responseCode = "201", description = "Slot created successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid slot data provided"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SlotDto createSlot(@Valid @RequestBody SlotDto slotDto) {
        logger.info("Received request to create slot: {}", slotDto);
        return slotService.createSlot(slotDto);
    }

    @Operation(summary = "Get slot by ID",
               description = "Retrieves a single slot by its ID.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved slot"),
                       @ApiResponse(responseCode = "404", description = "Slot not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/{id}")
    public ResponseEntity<SlotDto> getSlot(@PathVariable Long id) {
        logger.info("Received request to get slot with ID: {}", id);
        return ResponseEntity.ok(slotService.getSlotById(id));
    }

    @Operation(summary = "Get available slots for a vehicle",
               description = "Retrieves a list of available slots for a given vehicle ID.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved available slots"),
                       @ApiResponse(responseCode = "404", description = "Vehicle not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/vehicle/{vehicleId}/available")
    public ResponseEntity<List<SlotDto>> getAvailableSlots(@PathVariable Long vehicleId) {
        logger.info("Received request to get available slots for vehicle ID: {}", vehicleId);
        List<SlotDto> slots = slotService.getAvailableSlotsByVehicleId(vehicleId);
        return ResponseEntity.ok(slots);
    }

    @Operation(summary = "Get all slots for a vehicle",
               description = "Retrieves all slots (available or not) for a given vehicle ID within a date range.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved all slots for vehicle"),
                       @ApiResponse(responseCode = "404", description = "Vehicle not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
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
            try {
                startTime = OffsetDateTime.parse(start).toLocalDateTime();
                endTime = OffsetDateTime.parse(end).toLocalDateTime();
            } catch (DateTimeParseException e) {
                logger.error("Failed to parse date parameters: start={}, end={}", start, end, e);
                // Optionally, throw a custom exception or return a bad request status
                throw new IllegalArgumentException("Invalid date format provided. Please use ISO 8601 format (e.g., 2023-01-01T10:00:00Z).");
            }
        }
        return ResponseEntity.ok(slotService.getAllSlotsByVehicle(vehicleId, startTime, endTime));
    }

    @Operation(summary = "Book a slot",
               description = "Books a specific slot by its ID.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Slot booked successfully"),
                       @ApiResponse(responseCode = "404", description = "Slot not found"),
                       @ApiResponse(responseCode = "409", description = "Slot already booked or unavailable"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PutMapping("/{id}/book")
    public ResponseEntity<SlotDto> bookSlot(@PathVariable Long id) {
        logger.info("Received request to book slot with ID: {}", id);
        return ResponseEntity.ok(slotService.bookSlot(id));
    }

    @Operation(summary = "Block a slot",
               description = "Blocks a specific slot by its ID, making it unavailable.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Slot blocked successfully"),
                       @ApiResponse(responseCode = "404", description = "Slot not found"),
                       @ApiResponse(responseCode = "409", description = "Slot already blocked"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PutMapping("/{id}/block")
    public ResponseEntity<SlotDto> blockSlot(@PathVariable Long id) {
        logger.info("Received request to block slot with ID: {}", id);
        return ResponseEntity.ok(slotService.blockSlot(id));
    }

    @Operation(summary = "Delete a slot",
               description = "Deletes a slot by its ID.",
               responses = {
                       @ApiResponse(responseCode = "204", description = "Slot deleted successfully"),
                       @ApiResponse(responseCode = "404", description = "Slot not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSlot(@PathVariable Long id) {
        logger.info("Received request to delete slot with ID: {}", id);
        slotService.deleteSlot(id);
    }

    @Operation(summary = "Get all slots with pagination and filters",
               description = "Retrieves a paginated list of all slots, with optional filtering by availability and sorting.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated list of slots"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping
    public ResponseEntity<Page<SlotDto>> getAllSlots(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean isAvailable,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) Long vehicleId, // New filter parameter
            @RequestParam(required = false) String startDate, // New filter parameter
            @RequestParam(required = false) String endDate // New filter parameter
            ) {
        logger.info("Received request to get all slots with filters: page={}, size={}, isAvailable={}, sortBy={}, sortDirection={}, vehicleId={}, startDate={}, endDate={}",
                page, size, isAvailable, sortBy, sortDirection, vehicleId, startDate, endDate);
        java.time.LocalDateTime startDateTime = null;
        java.time.LocalDateTime endDateTime = null;
        if (startDate != null && endDate != null) {
            try {
                startDateTime = OffsetDateTime.parse(startDate).toLocalDateTime();
                endDateTime = OffsetDateTime.parse(endDate).toLocalDateTime();
            } catch (DateTimeParseException e) {
                logger.error("Failed to parse date parameters: startDate={}, endDate={}", startDate, endDate, e);
                throw new IllegalArgumentException("Invalid date format provided. Please use ISO 8601 format (e.g., 2023-01-01T10:00:00Z).");
            }
        }
        Page<SlotDto> slotsPage = slotService.getAllSlots(page, size, isAvailable, sortBy, sortDirection, vehicleId, startDateTime, endDateTime);
        return ResponseEntity.ok(slotsPage);
    }
}
