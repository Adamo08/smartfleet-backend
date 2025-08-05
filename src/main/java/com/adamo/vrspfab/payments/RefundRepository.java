package com.adamo.vrspfab.payments;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    @EntityGraph(attributePaths = {"payment"})
    List<Refund> findByPaymentId(Long paymentId);

    long countByProcessedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    @EntityGraph(attributePaths = {"payment"})
    Optional<Refund> findByPaymentTransactionId(String transactionId);
}
