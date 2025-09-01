package com.adamo.vrspfab.vehicles;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin-specific vehicle specification that doesn't filter by active status.
 * This allows admins to see and manage vehicles with inactive brands/categories/models.
 */
@RequiredArgsConstructor
public class AdminVehicleSpecification implements Specification<Vehicle> {

    private final VehicleFilter filter;

    @Override
    public Predicate toPredicate(Root<Vehicle> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        // NOTE: Admin specification does NOT filter by active status
        // This allows admins to manage all vehicles regardless of brand/category/model status

        if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
            String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
            Predicate searchPredicate = criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("licensePlate")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.join("brand").get("name")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.join("model").get("name")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.join("category").get("name")), searchPattern)
            );
            predicates.add(searchPredicate);
        }

        if (filter.getBrandId() != null) {
            predicates.add(criteriaBuilder.equal(root.join("brand").get("id"), filter.getBrandId()));
        }

        if (filter.getModelId() != null) {
            predicates.add(criteriaBuilder.equal(root.join("model").get("id"), filter.getModelId()));
        }

        if (filter.getCategoryId() != null) {
            predicates.add(criteriaBuilder.equal(root.join("category").get("id"), filter.getCategoryId()));
        }

        if (filter.getFuelType() != null && !filter.getFuelType().trim().isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("fuelType"), FuelType.valueOf(filter.getFuelType().toUpperCase())));
        }

        if (filter.getStatus() != null && !filter.getStatus().trim().isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("status"), VehicleStatus.valueOf(filter.getStatus().toUpperCase())));
        }

        if (filter.getMinPrice() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("pricePerDay"), filter.getMinPrice()));
        }

        if (filter.getMaxPrice() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("pricePerDay"), filter.getMaxPrice()));
        }

        if (filter.getMinYear() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("year"), filter.getMinYear()));
        }

        if (filter.getMaxYear() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("year"), filter.getMaxYear()));
        }

        if (filter.getMinMileage() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("mileage"), filter.getMinMileage()));
        }

        if (filter.getMaxMileage() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("mileage"), filter.getMaxMileage()));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
