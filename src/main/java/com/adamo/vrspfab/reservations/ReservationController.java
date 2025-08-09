package com.adamo.vrspfab.reservations;

import com.adamo.vrspfab.slots.SlotDto;
import com.adamo.vrspfab.slots.SlotService;
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
public class ReservationController {

    private final ReservationService reservationService;
    private final SlotService slotService;

    /**
     * POST /reservations : Creates a new reservation for the current user.
     *
     * @param request The reservation creation request.
     * @return A response entity with the created reservation details (201 CREATED).
     */
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
    @GetMapping
    public ResponseEntity<Page<ReservationSummaryDto>> getReservationsForCurrentUser(Pageable pageable) {
        return ResponseEntity.ok(reservationService.getReservationsForCurrentUser(pageable));
    }

    /**
     * GET /reservations/{id} : Gets a single reservation by ID for the current user.
     *
     * @param id The ID of the reservation.
     * @return A response entity with the detailed reservation DTO (200 OK).
     */
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
    @PostMapping("/{id}/cancel")
    public ResponseEntity<DetailedReservationDto> cancelReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.cancelReservation(id));
    }


    @GetMapping("/vehicles/{vehicleId}/available-slots")
    public ResponseEntity<List<SlotDto>> getAvailableSlots(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(slotService.getAvailableSlotsByVehicleId(vehicleId));
    }
}