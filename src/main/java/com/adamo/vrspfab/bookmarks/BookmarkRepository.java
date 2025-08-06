package com.adamo.vrspfab.bookmarks;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.lang.NonNull;

import java.util.Optional;

/**
 * Repository interface for managing {@link Bookmark} entities.
 * Provides standard CRUD operations and custom queries for bookmarks,
 * including pagination, eager loading of related entities, and specific lookups.
 */
@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    /**
     * Finds a Bookmark entity by its ID, eagerly fetching associated User and Reservation details.
     * This is useful when you need the full context of the bookmark in a single query.
     * The Reservation's Vehicle is also eagerly fetched for DTO enrichment.
     *
     * @param id The ID of the bookmark to retrieve.
     * @return An {@link Optional} containing the Bookmark if found, or empty if not.
     */
    @EntityGraph(
            attributePaths = {"user", "reservation", "reservation.vehicle"}, // Eagerly fetch user, reservation, and reservation's vehicle
            type = EntityGraph.EntityGraphType.LOAD
    )
    Optional<Bookmark> findWithDetailsById(Long id);

    /**
     * Retrieves a paginated list of Bookmark entities associated with a specific user ID.
     * User and Reservation details (including Reservation's Vehicle) are eagerly fetched.
     *
     * @param userId The ID of the user whose bookmarks are to be retrieved.
     * @param pageable Pagination information (page number, size, sort).
     * @return A {@link Page} of Bookmark entities for the specified user.
     */
    @EntityGraph(attributePaths = {"user", "reservation", "reservation.vehicle"}, type = EntityGraph.EntityGraphType.LOAD)
    Page<Bookmark> findByUserId(Long userId, @NonNull Pageable pageable);

    /**
     * Finds a Bookmark entity by a specific user ID and reservation ID.
     * This is used to check for the existence of a duplicate bookmark.
     *
     * @param userId The ID of the user.
     * @param reservationId The ID of the reservation.
     * @return An {@link Optional} containing the Bookmark if found, or empty if not.
     */
    Optional<Bookmark> findByUserIdAndReservationId(Long userId, Long reservationId);

    /**
     * Retrieves a paginated list of all Bookmark entities.
     * User and Reservation details (including Reservation's Vehicle) are eagerly fetched.
     * Overrides the default JpaRepository.findAll(Pageable) to include EntityGraph.
     *
     * @param pageable Pagination information (page number, size, sort). Must not be {@literal null}.
     * @return A {@link Page} of all Bookmark entities.
     */
    @Override
    @NonNull
    @EntityGraph(attributePaths = {"user", "reservation", "reservation.vehicle"}, type = EntityGraph.EntityGraphType.LOAD)
    Page<Bookmark> findAll(@NonNull Pageable pageable);
}
