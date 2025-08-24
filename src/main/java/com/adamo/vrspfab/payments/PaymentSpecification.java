package com.adamo.vrspfab.payments;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentSpecification implements Specification<Payment> {

    private final Long userId;
    private final Long reservationId;
    private final PaymentStatus status;
    private final BigDecimal minAmount;
    private final BigDecimal maxAmount;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final String searchTerm;

    public PaymentSpecification(Long userId, Long reservationId, PaymentStatus status, BigDecimal minAmount, BigDecimal maxAmount, LocalDateTime startDate, LocalDateTime endDate, String searchTerm) {
        this.userId = userId;
        this.reservationId = reservationId;
        this.status = status;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.searchTerm = searchTerm;
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

        // Add search term functionality
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String searchPattern = "%" + searchTerm.toLowerCase().trim() + "%";
            
            // Search across payment ID
            Predicate idSearch = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("id").as(String.class)), 
                searchPattern
            );
            
            // Search across transaction ID
            Predicate transactionSearch = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("transactionId")), 
                searchPattern
            );
            
            // Search across user names
            Predicate userSearch = criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("reservation").get("user").get("firstName")), searchPattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("reservation").get("user").get("lastName")), searchPattern)
            );
            
            // Search across reservation ID
            Predicate reservationSearch = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("reservation").get("id").as(String.class)), 
                searchPattern
            );
            
            predicates.add(criteriaBuilder.or(idSearch, transactionSearch, userSearch, reservationSearch));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
