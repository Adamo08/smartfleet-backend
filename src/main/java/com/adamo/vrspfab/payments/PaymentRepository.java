package com.adamo.vrspfab.payments;

import com.adamo.vrspfab.reservations.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {

    Optional<Payment> findByReservation(Reservation reservation);

    Optional<Payment> findByReservationId(Long reservationId);

    @EntityGraph(attributePaths = {"reservation"})
    Optional<Payment> findWithDetailsById(Long paymentId);

    Optional<Payment> findByTransactionId(String transactionId);

    @Query("SELECT p FROM Payment p WHERE p.reservation.user.id = :userId")
    Page<Payment> findByReservationUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.reservation.user.id = :userId")
    List<Payment> findByReservationUserId(@Param("userId") Long userId);

    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    long countByStatusAndCreatedAtBetween(PaymentStatus status, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status AND p.createdAt BETWEEN :startDate AND :endDate")
    Optional<BigDecimal> sumAmountByStatusAndCreatedAtBetween(
            @Param("status") PaymentStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status")
    Optional<BigDecimal> sumAmountByStatus(@Param("status") PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.reservation.user.id = :userId AND p.status = :status AND p.createdAt BETWEEN :startDate AND :endDate")
    Optional<BigDecimal> sumAmountByReservationUserIdAndStatusAndCreatedAtBetween(
            @Param("userId") Long userId,
            @Param("status") PaymentStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.reservation.user.id = :userId AND p.status = :status")
    Optional<BigDecimal> sumAmountByReservationUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") PaymentStatus status
    );

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.reservation.user.id = :userId AND p.status = :status")
    Long countByReservationUserIdAndStatus(@Param("userId") Long userId, @Param("status") PaymentStatus status);

    List<Payment> findByStatus(PaymentStatus status);

    /**
     * Get total revenue by category
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.status = 'COMPLETED' AND p.reservation.vehicle.category.id = :categoryId")
    BigDecimal getTotalRevenueByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Get total revenue by brand
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.status = 'COMPLETED' AND p.reservation.vehicle.brand.id = :brandId")
    BigDecimal getTotalRevenueByBrandId(@Param("brandId") Long brandId);

    /**
     * Get total fleet revenue
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'COMPLETED'")
    BigDecimal getTotalFleetRevenue();

    /**
     * Get monthly growth rate (placeholder - you can implement proper calculation)
     */
    @Query("SELECT 12.5 FROM Payment p WHERE p.status = 'COMPLETED' GROUP BY MONTH(p.createdAt) HAVING COUNT(p) > 0")
    BigDecimal getMonthlyGrowthRate();
}
