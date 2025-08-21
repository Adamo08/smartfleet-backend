package com.adamo.vrspfab.users;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification implements Specification<User> {

    private final String searchTerm;
    private final String role;

    public UserSpecification(String searchTerm, String role) {
        this.searchTerm = searchTerm;
        this.role = role;
    }

    @Override
    public Predicate toPredicate(jakarta.persistence.criteria.Root<User> root, jakarta.persistence.criteria.CriteriaQuery<?> query, jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String likePattern = "%" + searchTerm.toLowerCase() + "%";
            Predicate firstNameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), likePattern);
            Predicate lastNameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), likePattern);
            Predicate emailLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likePattern);
            predicates.add(criteriaBuilder.or(firstNameLike, lastNameLike, emailLike));
        }

        if (role != null && !role.trim().isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("role"), Role.valueOf(role.toUpperCase())));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
