package com.adamo.vrspfab.vehicles;

import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing {@link Vehicle} entities.
 * Provides advanced searching and filtering capabilities.
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {

    /**
     * Finds a vehicle by its unique license plate.
     *
     * @param licensePlate The license plate of the vehicle.
     * @return An Optional containing the vehicle, or empty if not found.
     */
    Optional<Vehicle> findByLicensePlate(String licensePlate);


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


    boolean existsByLicensePlate(String licensePlate);

    /**
     * Counts the number of vehicles with a specific status.
     *
     * @param status the status to count vehicles by
     * @return the number of vehicles with the given status
     */
    Long countByStatus(VehicleStatus status);

    /**
     * Gets vehicle utilization data by category for dashboard analytics.
     *
     * @return List of Object arrays containing [categoryName, totalVehicles, reservedVehicles]
     */
    @Query("SELECT c.name, COUNT(v), COALESCE(COUNT(r), 0) " +
           "FROM Vehicle v " +
           "JOIN v.category c " +
           "LEFT JOIN Reservation r ON r.vehicle.id = v.id AND r.status = 'CONFIRMED' AND r.endDate > CURRENT_TIMESTAMP " +
           "GROUP BY c.name")
    List<Object[]> getVehicleUtilizationByCategory();

    /**
     * Find vehicles by brand ID
     */
    List<Vehicle> findByBrandId(Long brandId);

    /**
     * Find vehicles by category ID
     */
    List<Vehicle> findByCategoryId(Long categoryId);

    /**
     * Find vehicles by model ID
     */
    List<Vehicle> findByModelId(Long modelId);
}
