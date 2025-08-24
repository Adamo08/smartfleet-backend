package com.adamo.vrspfab.vehicles;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class VehicleSpecification implements Specification<Vehicle> {

    private final VehicleFilter filters;

    public VehicleSpecification(VehicleFilter filters) {
        this.filters = filters;
    }

    @Override
    public Predicate toPredicate(jakarta.persistence.criteria.Root<Vehicle> root, jakarta.persistence.criteria.CriteriaQuery<?> query, jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (filters.getSearch() != null && !filters.getSearch().trim().isEmpty()) {
            String lowerCaseSearch = filters.getSearch().toLowerCase();
            Predicate brandLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("brand").get("name")), "%" + lowerCaseSearch + "%");
            Predicate modelLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("model").get("name")), "%" + lowerCaseSearch + "%");
            Predicate licensePlateLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("licensePlate")), "%" + lowerCaseSearch + "%");
            predicates.add(criteriaBuilder.or(brandLike, modelLike, licensePlateLike));
        }

        if (filters.getBrandId() != null) {
            jakarta.persistence.criteria.Join<Object, Object> brandJoin = root.join("brand", jakarta.persistence.criteria.JoinType.INNER);
            predicates.add(criteriaBuilder.equal(brandJoin.get("id"), filters.getBrandId()));
        }
        if (filters.getModelId() != null) {
            jakarta.persistence.criteria.Join<Object, Object> modelJoin = root.join("model", jakarta.persistence.criteria.JoinType.INNER);
            predicates.add(criteriaBuilder.equal(modelJoin.get("id"), filters.getModelId()));
        }
        if (filters.getCategoryId() != null) {
            // Use a left join to ensure vehicles without a category are still considered if no category filter is applied,
            // though the outer if ensures this block is only entered when a categoryId is provided.
            // This makes the join explicit and potentially more robust.
            jakarta.persistence.criteria.Join<Vehicle, VehicleCategory> categoryJoin = root.join("category", jakarta.persistence.criteria.JoinType.INNER);
            predicates.add(criteriaBuilder.equal(categoryJoin.get("id"), filters.getCategoryId()));
        }
        if (filters.getFuelType() != null && !filters.getFuelType().trim().isEmpty()) {
            try {
                predicates.add(criteriaBuilder.equal(root.get("fuelType"), FuelType.valueOf(filters.getFuelType().toUpperCase())));
            } catch (IllegalArgumentException e) {
                // Log the error or handle it as appropriate, for now, we'll just ignore the filter
                // log.warn("Invalid fuelType received: {}", filters.getFuelType());
            }
        }
        if (filters.getStatus() != null && !filters.getStatus().trim().isEmpty()) {
            try {
                predicates.add(criteriaBuilder.equal(root.get("status"), VehicleStatus.valueOf(filters.getStatus().toUpperCase())));
            } catch (IllegalArgumentException e) {
                // Log the error or handle it as appropriate, for now, we'll just ignore the filter
                // log.warn("Invalid status received: {}", filters.getStatus());
            }
        }
        if (filters.getMinPrice() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("pricePerDay"), filters.getMinPrice()));
        }
        if (filters.getMaxPrice() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("pricePerDay"), filters.getMaxPrice()));
        }
        if (filters.getMinYear() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("year"), filters.getMinYear()));
        }
        if (filters.getMaxYear() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("year"), filters.getMaxYear()));
        }
        if (filters.getMinMileage() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("mileage"), filters.getMinMileage()));
        }
        if (filters.getMaxMileage() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("mileage"), filters.getMaxMileage()));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}

