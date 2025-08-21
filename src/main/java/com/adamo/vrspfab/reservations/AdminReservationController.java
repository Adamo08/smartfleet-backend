package com.adamo.vrspfab.reservations;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for administrators to manage all reservations in the system.
 * Path: /admin/reservations
 */
@RestController
@RequestMapping("/admin/reservations")
@RequiredArgsConstructor
@Tag(name = "Admin Reservations", description = "APIs for administrators to manage all vehicle reservations")
public class AdminReservationController {

    private final AdminReservationService adminReservationService;

    /**
     * GET /admin/reservations : Gets a paginated list of all reservations, with filtering.
     *
     * @param filter   The filter criteria (userId, vehicleId, status).
     * @param pageable Pagination and sorting parameters.
     * @return A page of reservation summaries.
     */
    @Operation(summary = "Get all reservations (Admin only)",
               description = "Retrieves a paginated list of all reservations in the system, with optional filtering by user ID, vehicle ID, and status. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved reservations"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping
    public ResponseEntity<Page<ReservationSummaryDto>> getAllReservations(ReservationFilter filter, Pageable pageable) {
        return ResponseEntity.ok(adminReservationService.getAllReservations(filter, pageable));
    }

    /**
     * GET /admin/reservations/{id} : Gets any reservation by its ID.
     *
     * @param id The ID of the reservation.
     * @return A detailed DTO of the reservation.
     */
    @Operation(summary = "Get reservation by ID (Admin only)",
               description = "Retrieves a single reservation by its ID. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved reservation"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "404", description = "Reservation not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/{id}")
    public ResponseEntity<DetailedReservationDto> getReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(adminReservationService.getReservationById(id));
    }

    /**
     * PATCH /admin/reservations/{id} : Updates a reservation (e.g., to change its status).
     *
     * @param id The ID of the reservation to update.
     * @param request The update request.
     * @return The updated reservation DTO.
     */
    @Operation(summary = "Update reservation details (Admin only)",
               description = "Updates an existing reservation, typically to change its status or other administrative details. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Reservation updated successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid update request"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "404", description = "Reservation not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PatchMapping("/{id}")
    public ResponseEntity<DetailedReservationDto> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody AdminReservationUpdateRequest request
    ) {
        return ResponseEntity.ok(adminReservationService.updateReservationStatus(id, request));
    }

    /**
     * DELETE /admin/reservations/{id} : Deletes a reservation.
     *
     * @param id The ID of the reservation to delete.
     * @return A response with no content (204 NO_CONTENT).
     */
    @Operation(summary = "Delete a reservation (Admin only)",
               description = "Deletes a reservation by its ID. This action is irreversible. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "204", description = "Reservation deleted successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "404", description = "Reservation not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        adminReservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}