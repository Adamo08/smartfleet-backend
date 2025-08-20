package com.adamo.vrspfab.slots;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.lang.NonNull; // Import NonNull annotation

import java.time.LocalDateTime; // Import for LocalDateTime
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link Slot} entities.
 * Provides standard CRUD operations and custom queries for slots,
 * including pagination, eager loading of related entities, and specific lookups.
 */
@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {
    /**
     * Finds a Slot entity by its ID, eagerly fetching associated Vehicle details.
     *
     * @param id The ID of the slot to retrieve.
     * @return An {@link Optional} containing the Slot if found, or empty if not.
     */
    @EntityGraph(
            attributePaths = {"vehicle"},
            type = EntityGraph.EntityGraphType.LOAD
    )
    Optional<Slot> findWithVehicleById(Long id);

    /**
     * Finds available slots for a specific vehicle ID.
     *
     * @param vehicleId The ID of the vehicle.
     * @return A list of available Slot entities.
     */
    @EntityGraph(attributePaths = {"vehicle", "reservation"}, type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT s FROM Slot s WHERE s.vehicle.id = :vehicleId AND s.available = true")
    List<Slot> findAvailableSlotsByVehicleId(Long vehicleId);

    /**
     * Retrieves a paginated list of all Slot entities.
     * Vehicle details are eagerly fetched.
     *
     * @param pageable Pagination information (page number, size, sort). Must not be {@literal null}.
     * @return A {@link Page} of all Slot entities.
     */
    @Override
    @NonNull
    @EntityGraph(attributePaths = {"vehicle"}, type = EntityGraph.EntityGraphType.LOAD)
    Page<Slot> findAll(@NonNull Pageable pageable);

    /**
     * Retrieves a paginated list of Slot entities filtered by their availability status.
     * Vehicle details are eagerly fetched.
     *
     * @param isAvailable Boolean indicating if the slot should be available or not.
     * @param pageable Pagination information (page number, size, sort). Must not be {@literal null}.
     * @return A {@link Page} of Slot entities matching the availability status.
     */
    @EntityGraph(attributePaths = {"vehicle"}, type = EntityGraph.EntityGraphType.LOAD)
    Page<Slot> findByAvailable(Boolean isAvailable, @NonNull Pageable pageable);

    /**
     * Finds any overlapping slots for a given vehicle within a specified time range.
     * This query is crucial for preventing double-booking.
     *
     * @param vehicleId The ID of the vehicle.
     * @param startTime The start time of the new slot to check.
     * @param endTime The end time of the new slot to check.
     * @return A list of Slots that overlap with the given time range for the specified vehicle.
     */
    @Query("SELECT s FROM Slot s WHERE s.vehicle.id = :vehicleId AND " +
            "((s.startTime < :endTime AND s.endTime > :startTime))")
    List<Slot> findOverlappingSlots(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Finds all slots (available and unavailable) for a specific vehicle.
     */
    @EntityGraph(attributePaths = {"vehicle", "reservation"}, type = EntityGraph.EntityGraphType.LOAD)
    List<Slot> findByVehicle_Id(Long vehicleId);

    /**
     * Finds all slots for a specific vehicle within a date range.
     */
    @EntityGraph(attributePaths = {"vehicle", "reservation"}, type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT s FROM Slot s WHERE s.vehicle.id = :vehicleId AND s.startTime >= :start AND s.endTime <= :end")
    List<Slot> findByVehicleIdAndStartTimeBetween(Long vehicleId, LocalDateTime start, LocalDateTime end);
}
