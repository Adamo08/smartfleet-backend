package com.adamo.vrspfab.reservations;

import com.adamo.vrspfab.slots.SlotDto;
import com.adamo.vrspfab.slots.SlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for authenticated users to manage their own reservations.
 * Path: /api/reservations
 */
@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@Tag(name = "User Reservations", description = "APIs for users to manage their own vehicle reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final SlotService slotService;

    /**
     * POST /reservations : Creates a new reservation for the current user.
     *
     * @param request The reservation creation request.
     * @return A response entity with the created reservation details (201 CREATED).
     */
    @Operation(summary = "Create a new reservation",
               description = "Allows the authenticated user to create a new vehicle reservation.",
               responses = {
                       @ApiResponse(responseCode = "201", description = "Reservation created successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid reservation details or vehicle unavailable"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PostMapping
    public ResponseEntity<DetailedReservationDto> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        DetailedReservationDto createdReservation = reservationService.createReservation(request);
        return new ResponseEntity<>(createdReservation, HttpStatus.CREATED);
    }

    /**
     * GET /reservations : Gets a paginated list of reservations for the current user.
     *
     * @param pageable Pagination and sorting parameters.
     * @return A response entity with the page of reservation summaries (200 OK).
     */
    @Operation(summary = "Get user's reservations",
               description = "Retrieves a paginated list of reservations for the currently authenticated user.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved user's reservations"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping
    public ResponseEntity<Page<ReservationSummaryDto>> getReservationsForCurrentUser(Pageable pageable) {
        return ResponseEntity.ok(reservationService.getReservationsForCurrentUser(pageable));
    }


    /**
     * GET /reservations/filtered : Gets a filtered and paginated list of reservations for the current user.
     *
     * @param page          The page number (0-based).
     * @param size          The page size.
     * @param sortBy        The field to sort by.
     * @param sortDirection The sort direction (ASC or DESC).
     * @param status        (Optional) Filter by reservation status.
     * @param startDate    (Optional) Filter by start date (ISO 8601 format).
     * @param endDate      (Optional) Filter by end date (ISO 8601 format).
     * @param searchTerm   (Optional) Search term to filter reservations.
     * @return A response entity with the page of filtered reservation summaries (200 OK).
     */
    @Operation(summary = "Get filtered user's reservations",
               description = "Retrieves a filtered and paginated list of reservations for the currently authenticated user.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved filtered user's reservations"),
                       @ApiResponse(responseCode = "400", description = "Invalid filter parameters"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/filtered")
    public ResponseEntity<Page<ReservationSummaryDto>> getFilteredReservationsForCurrentUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime endDate,
            @RequestParam(required = false) String searchTerm
    ) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.fromString(sortDirection), sortBy));
        
        ReservationFilter filter = ReservationFilter.builder()
                .status(status != null ? ReservationStatus.valueOf(status) : null)
                .startDate(startDate)
                .endDate(endDate)
                .searchTerm(searchTerm)
                .build();
        
        return ResponseEntity.ok(reservationService.getUserReservationsWithFilter(filter, pageable));
    }

    /**
     * GET /reservations/{id} : Gets a single reservation by ID for the current user.
     *
     * @param id The ID of the reservation.
     * @return A response entity with the detailed reservation DTO (200 OK).
     */
    @Operation(summary = "Get reservation by ID",
               description = "Retrieves a single reservation by its ID for the currently authenticated user.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved reservation"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, user does not own this reservation"),
                       @ApiResponse(responseCode = "404", description = "Reservation not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/{id}")
    public ResponseEntity<DetailedReservationDto> getReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationByIdForCurrentUser(id));
    }

    /**
     * POST /reservations/{id}/cancel : Cancels a reservation.
     *
     * @param id The ID of the reservation to cancel.
     * @return A response entity with the updated reservation DTO (200 OK).
     */
    @Operation(summary = "Cancel a reservation",
               description = "Cancels an existing reservation for the currently authenticated user.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Reservation cancelled successfully"),
                       @ApiResponse(responseCode = "400", description = "Cannot cancel reservation (e.g., already completed)"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, user does not own this reservation"),
                       @ApiResponse(responseCode = "404", description = "Reservation not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PostMapping("/{id}/cancel")
    public ResponseEntity<DetailedReservationDto> cancelReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.cancelReservation(id));
    }


    @Operation(summary = "Get available slots for a vehicle",
               description = "Retrieves a list of available slots for a given vehicle ID. This is a helper endpoint for creating reservations.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved available slots"),
                       @ApiResponse(responseCode = "404", description = "Vehicle not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/vehicles/{vehicleId}/available-slots")
    public ResponseEntity<List<SlotDto>> getAvailableSlots(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(slotService.getAvailableSlotsByVehicleId(vehicleId));
    }
}