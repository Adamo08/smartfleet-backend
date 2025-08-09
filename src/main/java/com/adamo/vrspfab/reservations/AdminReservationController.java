package com.adamo.vrspfab.reservations;


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
public class AdminReservationController {

    private final AdminReservationService adminReservationService;

    /**
     * GET /admin/reservations : Gets a paginated list of all reservations, with filtering.
     *
     * @param filter   The filter criteria (userId, vehicleId, status).
     * @param pageable Pagination and sorting parameters.
     * @return A page of reservation summaries.
     */
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
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        adminReservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}