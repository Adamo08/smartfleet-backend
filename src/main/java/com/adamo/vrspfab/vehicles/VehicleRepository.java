package com.adamo.vrspfab.vehicles;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for managing {@link Vehicle} entities.
 * Provides advanced searching and filtering capabilities.
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    /**
     * Finds a vehicle by its unique license plate.
     *
     * @param licensePlate The license plate of the vehicle.
     * @return An Optional containing the vehicle, or empty if not found.
     */
    Optional<Vehicle> findByLicensePlate(String licensePlate);

    /**
     * Finds all vehicles with pagination.
     *
     * @param pageable Pagination and sorting information.
     * @return A Page of Vehicle entities.
     */
    @Override
    @NonNull
    Page<Vehicle> findAll(@NonNull Pageable pageable);

    /**
     * Finds all vehicles with a specific status, with pagination.
     *
     * @param status The status of the vehicles to find.
     * @param pageable Pagination and sorting information.
     * @return A Page of Vehicle entities.
     */
    @NonNull
    Page<Vehicle> findByStatus(VehicleStatus status, @NonNull Pageable pageable);

    /**
     * Finds all vehicles of a specific type, with pagination.
     *
     * @param type The type of the vehicles to find.
     * @param pageable Pagination and sorting information.
     * @return A Page of Vehicle entities.
     */
    @NonNull
    Page<Vehicle> findByVehicleType(VehicleType type, @NonNull Pageable pageable);

    /**
     * Finds all vehicles with a specific status and type, with pagination.
     *
     * @param status The status of the vehicles to find.
     * @param type The type of the vehicles to find.
     * @param pageable Pagination and sorting information.
     * @return A Page of Vehicle entities.
     */
    @NonNull
    Page<Vehicle> findByStatusAndVehicleType(VehicleStatus status, VehicleType type, @NonNull Pageable pageable);

    /**
     * Finds all vehicles with a brand containing the given string (case-insensitive), with pagination.
     *
     * @param brand The brand string to search for.
     * @param pageable Pagination and sorting information.
     * @return A Page of Vehicle entities.
     */
    @Query("SELECT v FROM Vehicle v WHERE LOWER(v.brand) LIKE LOWER(CONCAT('%', :brand, '%'))")
    @NonNull
    Page<Vehicle> findByBrandContainingIgnoreCase(String brand, @NonNull Pageable pageable);

    /**
     * Finds all vehicles with a model containing the given string (case-insensitive), with pagination.
     *
     * @param model The model string to search for.
     * @param pageable Pagination and sorting information.
     * @return A Page of Vehicle entities.
     */
    @Query("SELECT v FROM Vehicle v WHERE LOWER(v.model) LIKE LOWER(CONCAT('%', :model, '%'))")
    @NonNull
    Page<Vehicle> findByModelContainingIgnoreCase(String model, @NonNull Pageable pageable);

    /**
     * Finds all vehicles with a price per day within a specified range, with pagination.
     *
     * @param minPrice The minimum price.
     * @param maxPrice The maximum price.
     * @param pageable Pagination and sorting information.
     * @return A Page of Vehicle entities.
     */
    @NonNull
    Page<Vehicle> findByPricePerDayBetween(Double minPrice, Double maxPrice, @NonNull Pageable pageable);

    /**
     * Finds all vehicles with a year within a specified range, with pagination.
     *
     * @param startYear The start year of the range.
     * @param endYear The end year of the range.
     * @param pageable Pagination and sorting information.
     * @return A Page of Vehicle entities.
     */
    @Query("SELECT v FROM Vehicle v WHERE v.year BETWEEN :startYear AND :endYear")
    @NonNull
    Page<Vehicle> findByYearBetween(Integer startYear, Integer endYear, @NonNull Pageable pageable);

    /**
     * Finds all vehicles with a mileage within a specified range, with pagination.
     *
     * @param minMileage The minimum mileage.
     * @param maxMileage The maximum mileage.
     * @param pageable Pagination and sorting information.
     * @return A Page of Vehicle entities.
     */
    @Query("SELECT v FROM Vehicle v WHERE v.mileage BETWEEN :minMileage AND :maxMileage")
    @NonNull
    Page<Vehicle> findByMileageBetween(Float minMileage, Float maxMileage, @NonNull Pageable pageable);

    /**
     * Searches for vehicles based on a combination of filters, with pagination.
     *
     * @param status An optional vehicle status filter.
     * @param type An optional vehicle type filter.
     * @param brand An optional brand string to search for.
     * @param model An optional model string to search for.
     * @param minPrice An optional minimum price.
     * @param maxPrice An optional maximum price.
     * @param pageable Pagination and sorting information.
     * @return A Page of VehicleDto objects.
     */
    @Query("SELECT v FROM Vehicle v WHERE " +
            "(:status IS NULL OR v.status = :status) AND " +
            "(:type IS NULL OR v.vehicleType = :type) AND " +
            "(:brand IS NULL OR LOWER(v.brand) LIKE LOWER(CONCAT('%', :brand, '%'))) AND " +
            "(:model IS NULL OR LOWER(v.model) LIKE LOWER(CONCAT('%', :model, '%'))) AND " +
            "(:minPrice IS NULL OR v.pricePerDay >= :minPrice) AND " +
            "(:maxPrice IS NULL OR v.pricePerDay <= :maxPrice)")
    @NonNull
    Page<Vehicle> searchVehicles(VehicleStatus status, VehicleType type, String brand, String model, Double minPrice, Double maxPrice, @NonNull Pageable pageable);
}
