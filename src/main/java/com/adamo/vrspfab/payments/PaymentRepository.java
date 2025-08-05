package com.adamo.vrspfab.payments;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;



@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @EntityGraph(
            attributePaths = {"reservation"},
            type = EntityGraph.EntityGraphType.LOAD
    )
    Optional<Payment> findWithDetailsById(Long id);
}
