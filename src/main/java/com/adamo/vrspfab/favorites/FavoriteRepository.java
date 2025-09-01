package com.adamo.vrspfab.favorites;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link Favorite} entities.
 * Provides standard CRUD operations and custom queries for favorites,
 * including pagination, eager loading of related entities, and specific lookups.
 */
@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    /**
     * Finds a Favorite entity by its ID, eagerly fetching associated User and Vehicle details.
     * This is useful when you need the full context of the favorite in a single query.
     *
     * @param id The ID of the favorite to retrieve.
     * @return An {@link Optional} containing the Favorite if found, or empty if not.
     */
    @EntityGraph(
            attributePaths = {"user", "vehicle"},
            type = EntityGraph.EntityGraphType.LOAD
    )
    Optional<Favorite> findWithDetailsById(Long id);

    /**
     * Retrieves a paginated list of Favorite entities associated with a specific user ID.
     * User and Vehicle details are eagerly fetched.
     *
     * @param userId The ID of the user whose favorites are to be retrieved.
     * @param pageable Pagination information (page number, size, sort).
     * @return A {@link Page} of Favorite entities for the specified user.
     */
    @EntityGraph(attributePaths = {"user", "vehicle"}, type = EntityGraph.EntityGraphType.LOAD)
    Page<Favorite> findByUserId(Long userId, @NonNull Pageable pageable);



    /**
     * Retrieves a list of all Favorite entities associated with a specific user ID.
     * User and Vehicle details are eagerly fetched.
     *
     * @param userId The ID of the user whose favorites are to be retrieved.
     * @return A {@link Page} of Favorite entities for the specified user.
     */
    @EntityGraph(attributePaths = {"user", "vehicle"}, type = EntityGraph.EntityGraphType.LOAD)
    List<Favorite> findByUserId(Long userId);

    /**
     * Finds a Favorite entity by a specific user ID and vehicle ID.
     * This is used to check for the existence of a duplicate favorite.
     *
     * @param userId The ID of the user.
     * @param vehicleId The ID of the vehicle.
     * @return An {@link Optional} containing the Favorite if found, or empty if not.
     */
    Optional<Favorite> findByUserIdAndVehicleId(Long userId, Long vehicleId);

    /**
     * Retrieves a paginated list of all Favorite entities.
     * User and Vehicle details are eagerly fetched.
     * Overrides the default JpaRepository.findAll(Pageable) to include EntityGraph.
     *
     * @param pageable Pagination information (page number, size, sort). Must not be {@literal null}.
     * @return A {@link Page} of all Favorite entities.
     */
    @Override
    @NonNull
    @EntityGraph(attributePaths = {"user", "vehicle"}, type = EntityGraph.EntityGraphType.LOAD)
    Page<Favorite> findAll(@NonNull Pageable pageable);
}
