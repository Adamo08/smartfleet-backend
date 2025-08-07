package com.adamo.vrspfab.reservations;

import com.adamo.vrspfab.reservations.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @EntityGraph(attributePaths = {"user", "vehicle"}, type = EntityGraph.EntityGraphType.LOAD)
    Optional<Reservation> findWithDetailsById(Long id);

    @Query("SELECT r FROM Reservation r WHERE r.user.id = :userId")
    List<Reservation> findByUserId(Long userId);

    @Query("SELECT r FROM Reservation r WHERE r.status = :status")
    List<Reservation> findByStatus(String status);

    @Query("SELECT r FROM Reservation r LEFT JOIN FETCH r.user LEFT JOIN FETCH r.vehicle")
    List<Reservation> findAllWithUserAndVehicle();

    @Query("SELECT r FROM Reservation r WHERE r.vehicle.id = :vehicleId AND " +
            "((r.startDate <= :endDate AND r.endDate >= :startDate) OR " +
            "(r.startDate >= :startDate AND r.endDate <= :endDate))")
    Optional<Reservation> findOverlappingReservations(Long vehicleId, LocalDateTime startDate, LocalDateTime endDate);


    @EntityGraph(attributePaths = {"user", "vehicle"}, type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT r FROM Reservation r WHERE r.vehicle.id = :vehicleId")
    Page<Reservation> findByVehicleId(Long vehicleId, Pageable pageable);
}