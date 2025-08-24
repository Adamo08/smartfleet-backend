package com.adamo.vrspfab.reservations;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    /**
     * Finds a reservation by its ID, eagerly fetching the associated user and vehicle.
     *
     * @param id The reservation ID.
     * @return An Optional containing the reservation if found.
     */
    @NonNull
    @EntityGraph(attributePaths = {"user", "vehicle"})
    Optional<Reservation> findById(@NonNull Long id);


    /**
     * Finds all reservations matching the given specification, applying pagination and sorting.
     * Eagerly fetches user and vehicle data to prevent N+1 query issues.
     *
     * @param spec The specification to filter by.
     * @param pageable The pagination and sorting information.
     * @return A page of reservations.
     */
    @NonNull
    @EntityGraph(attributePaths = {"user", "vehicle", "slot"}, type = EntityGraph.EntityGraphType.LOAD)
    Page<Reservation> findAll(Specification<Reservation> spec,@NonNull Pageable pageable);

    /**
     * Checks for existing reservations that overlap with the given time frame for a specific vehicle.
     * This is crucial for preventing double bookings.
     *
     * @param vehicleId The ID of the vehicle to check.
     * @param startDate The start time of the proposed reservation.
     * @param endDate The end time of the proposed reservation.
     * @return A list of overlapping reservations.
     */
    @EntityGraph(attributePaths = {"user", "vehicle", "slot"}, type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT r FROM Reservation r WHERE r.vehicle.id = :vehicleId AND r.status <> 'CANCELLED' AND r.startDate < :endDate AND r.endDate > :startDate")
    List<Reservation> findOverlappingReservations(Long vehicleId, LocalDateTime startDate, LocalDateTime endDate);

    @EntityGraph(attributePaths = {"user", "vehicle", "slot"}, type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT r FROM Reservation r WHERE r.vehicle.id = :vehicleId")
    Page<Reservation> findByVehicleId(Long vehicleId, Pageable pageable);


    /**
     * Finds all reservations with a given status and an end date before a specified time.
     * Used by the scheduled task to automatically complete past reservations.
     *
     * @param status The status of the reservations to find.
     * @param now The timestamp to compare the end date against.
     * @return A list of completable reservations.
     */
    @EntityGraph(attributePaths = {"user", "vehicle", "slot"}, type = EntityGraph.EntityGraphType.LOAD)
    List<Reservation> findAllByStatusAndEndDateBefore(ReservationStatus status, LocalDateTime now);

    /**
     * Counts the number of reservations with a specific status.
     *
     * @param status the status to count reservations by
     * @return the number of reservations with the given status
     */
    Long countByStatus(ReservationStatus status);
    @EntityGraph(attributePaths = {"user", "vehicle", "slot"}, type = EntityGraph.EntityGraphType.LOAD)
    Page<Reservation> findByUserId(Long id, Pageable pageable);
}