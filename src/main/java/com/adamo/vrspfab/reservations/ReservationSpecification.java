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

            // Security constraint: Filter by user ID
            if (filter.getUserId() != null) {
                // Filter by specific user ID (admin functionality)
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), filter.getUserId()));
            } else if (currentUser != null) {
                // If no specific user ID is provided, always filter by current user
                // This ensures even admins see only their own reservations when using user endpoints
                predicates.add(criteriaBuilder.equal(root.get("user"), currentUser));
            }

            // Add other filter criteria
            if (filter.getVehicleId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("vehicle").get("id"), filter.getVehicleId()));
            }
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), filter.getStartDate()));
            }

            if (filter.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), filter.getEndDate()));
            }

            // Add search term functionality
            if (filter.getSearchTerm() != null && !filter.getSearchTerm().trim().isEmpty()) {
                String searchTerm = "%" + filter.getSearchTerm().toLowerCase().trim() + "%";
                
                // Search across user names
                Predicate userSearch = criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("user").get("firstName")), searchTerm),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("user").get("lastName")), searchTerm)
                );
                
                // Search across vehicle details
                Predicate vehicleSearch = criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("vehicle").get("brand")), searchTerm),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("vehicle").get("model")), searchTerm)
                );
                
                // Search across reservation ID
                Predicate idSearch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("id").as(String.class)), 
                    searchTerm
                );
                
                predicates.add(criteriaBuilder.or(userSearch, vehicleSearch, idSearch));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}