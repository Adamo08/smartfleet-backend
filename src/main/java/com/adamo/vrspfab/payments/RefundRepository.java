package com.adamo.vrspfab.payments;

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
public interface RefundRepository extends JpaRepository<Refund, Long>, JpaSpecificationExecutor<Refund> {

    @EntityGraph(attributePaths = {"payment"})
    List<Refund> findByPaymentId(Long paymentId);

    long countByProcessedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    @EntityGraph(attributePaths = {"payment"})
    Optional<Refund> findByRefundTransactionId(String refundTransactionId);

    @EntityGraph(attributePaths = {"payment", "payment.reservation", "payment.reservation.user"})
    Page<Refund> findAll(Pageable pageable);

    // Admin queries for filtered refund history
    @Query("SELECT r FROM Refund r WHERE " +
           "(:paymentId IS NULL OR r.payment.id = :paymentId) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:minAmount IS NULL OR r.amount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR r.amount <= :maxAmount) AND " +
           "(:startDate IS NULL OR r.processedAt >= :startDate) AND " +
           "(:endDate IS NULL OR r.processedAt <= :endDate) AND " +
           "(:searchTerm IS NULL OR r.reason LIKE %:searchTerm%)")
    Page<Refund> findAllWithFilters(
            @Param("paymentId") Long paymentId,
            @Param("status") RefundStatus status,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

    // User-specific refund history
    @Query("SELECT r FROM Refund r WHERE r.payment.reservation.user.id = :userId")
    Page<Refund> findByUserId(@Param("userId") Long userId, Pageable pageable);

    // Find refunds by status
    @EntityGraph(attributePaths = {"payment", "payment.reservation", "payment.reservation.user"})
    Page<Refund> findByStatus(RefundStatus status, Pageable pageable);
}
