package com.adamo.vrspfab.notifications;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class NotificationSpecification implements Specification<Notification> {

    private final NotificationFilter filter;

    public NotificationSpecification(NotificationFilter filter) {
        this.filter = filter;
    }

    @Override
    public Predicate toPredicate(jakarta.persistence.criteria.Root<Notification> root, jakarta.persistence.criteria.CriteriaQuery<?> query, jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (filter.getUserId() != null) {
            predicates.add(criteriaBuilder.equal(root.get("user").get("id"), filter.getUserId()));
        }
        if (filter.getRead() != null) {
            predicates.add(criteriaBuilder.equal(root.get("read"), filter.getRead()));
        }
        if (filter.getType() != null) {
            predicates.add(criteriaBuilder.equal(root.get("type"), filter.getType()));
        }
        if (filter.getStartDate() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), filter.getStartDate()));
        }
        if (filter.getEndDate() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), filter.getEndDate()));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
