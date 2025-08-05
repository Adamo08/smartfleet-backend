package com.adamo.vrspfab.vehicles;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    @EntityGraph(
            attributePaths = {"slots"},
            type = EntityGraph.EntityGraphType.LOAD
    )
    Optional<Vehicle> findWithSlotsById(Long id);

    @Query("SELECT v FROM Vehicle v WHERE v.status = :status")
    List<Vehicle> findByStatus(VehicleStatus status);

    @Query("SELECT v FROM Vehicle v LEFT JOIN FETCH v.slots")
    List<Vehicle> findAllWithSlots();

    Optional<Object> findByLicensePlate(String licensePlate);
}
