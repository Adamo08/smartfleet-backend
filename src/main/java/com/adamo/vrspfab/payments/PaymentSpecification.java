package com.adamo.vrspfab.payments;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentSpecification implements Specification<Payment> {

    private final Long userId;
    private final Long reservationId;
    private final PaymentStatus status;
    private final Double minAmount;
    private final Double maxAmount;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;

    public PaymentSpecification(Long userId, Long reservationId, PaymentStatus status, Double minAmount, Double maxAmount, LocalDateTime startDate, LocalDateTime endDate) {
        this.userId = userId;
        this.reservationId = reservationId;
        this.status = status;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public Predicate toPredicate(jakarta.persistence.criteria.Root<Payment> root, jakarta.persistence.criteria.CriteriaQuery<?> query, jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (userId != null) {
            predicates.add(criteriaBuilder.equal(root.get("reservation").get("user").get("id"), userId));
        }
        if (reservationId != null) {
            predicates.add(criteriaBuilder.equal(root.get("reservation").get("id"), reservationId));
        }
        if (status != null) {
            predicates.add(criteriaBuilder.equal(root.get("status"), status));
        }
        if (minAmount != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), minAmount));
        }
        if (maxAmount != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("amount"), maxAmount));
        }
        if (startDate != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate));
        }
        if (endDate != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
