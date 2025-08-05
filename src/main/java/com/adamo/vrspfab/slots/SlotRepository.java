package com.adamo.vrspfab.slots;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {
    @EntityGraph(
            attributePaths = {"vehicle"},
            type = EntityGraph.EntityGraphType.LOAD
    )
    Optional<Slot> findWithVehicleById(Long id);

    @Query("SELECT s FROM Slot s WHERE s.vehicle.id = :vehicleId AND s.isAvailable = true")
    List<Slot> findAvailableSlotsByVehicleId(Long vehicleId);

    @Query("SELECT s FROM Slot s LEFT JOIN FETCH s.vehicle")
    List<Slot> findAllWithVehicle();


    @Query("SELECT s FROM Slot s WHERE s.isAvailable = :isAvailable")
    List<Slot> findByAvailable(Boolean isAvailable);

}