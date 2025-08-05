package com.adamo.vrspfab.payments;


import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;



@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {
    @EntityGraph(
            attributePaths = {"payment"},
            type = EntityGraph.EntityGraphType.LOAD
    )
    List<Dispute> findByPaymentId(Long paymentId);
}