package com.adamo.vrspfab.reservations;


import com.adamo.vrspfab.users.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a mechanism to dynamically build JPA Criteria Queries for Reservations.
 * This class is used to filter reservations based on various criteria and enforces security
 * by ensuring non-admin users can only see their own reservations.
 */
public class ReservationSpecification {

    /**
     * Creates a JPA Specification based on the provided filter criteria and the current user.
     *
     * @param filter The DTO containing the filter parameters (e.g., userId, vehicleId, status).
     * @param currentUser The currently authenticated user, used for security enforcement. Can be null for anonymous access if needed.
     * @return A Specification object that can be used in repository queries.
     */
    public static Specification<Reservation> withFilter(ReservationFilter filter, User currentUser) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Security constraint: Non-admins can only see their own reservations.
            if (currentUser != null && !currentUser.getRole().equals(com.adamo.vrspfab.users.Role.ADMIN)) {
                predicates.add(criteriaBuilder.equal(root.get("user"), currentUser));
            } else if (filter.getUserId() != null) {
                // Admins can filter by any user ID.
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), filter.getUserId()));
            }

            // Add other filter criteria
            if (filter.getVehicleId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("vehicle").get("id"), filter.getVehicleId()));
            }
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}