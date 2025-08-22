package com.adamo.vrspfab.vehicles;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class VehicleSpecification implements Specification<Vehicle> {

    private final String brand;
    private final String model;
    private final String vehicleType;
    private final String fuelType;
    private final String status;
    private final Double minPrice;
    private final Double maxPrice;

    public VehicleSpecification(String brand, String model, String vehicleType, String fuelType, String status, Double minPrice, Double maxPrice) {
        this.brand = brand;
        this.model = model;
        this.vehicleType = vehicleType;
        this.fuelType = fuelType;
        this.status = status;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    @Override
    public Predicate toPredicate(jakarta.persistence.criteria.Root<Vehicle> root, jakarta.persistence.criteria.CriteriaQuery<?> query, jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (brand != null && !brand.trim().isEmpty()) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("brand")), "%" + brand.toLowerCase() + "%"));
        }
        if (model != null && !model.trim().isEmpty()) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("model")), "%" + model.toLowerCase() + "%"));
        }
        if (vehicleType != null && !vehicleType.trim().isEmpty()) {
            try {
                predicates.add(criteriaBuilder.equal(root.get("vehicleType"), VehicleType.valueOf(vehicleType.toUpperCase())));
            } catch (IllegalArgumentException e) {
                // Log the error or handle it as appropriate, for now, we'll just ignore the filter
                // log.warn("Invalid vehicleType received: {}", vehicleType);
            }
        }
        if (fuelType != null && !fuelType.trim().isEmpty()) {
            try {
                predicates.add(criteriaBuilder.equal(root.get("fuelType"), FuelType.valueOf(fuelType.toUpperCase())));
            } catch (IllegalArgumentException e) {
                // Log the error or handle it as appropriate, for now, we'll just ignore the filter
                // log.warn("Invalid fuelType received: {}", fuelType);
            }
        }
        if (status != null && !status.trim().isEmpty()) {
            try {
                predicates.add(criteriaBuilder.equal(root.get("status"), VehicleStatus.valueOf(status.toUpperCase())));
            } catch (IllegalArgumentException e) {
                // Log the error or handle it as appropriate, for now, we'll just ignore the filter
                // log.warn("Invalid status received: {}", status);
            }
        }
        if (minPrice != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("pricePerDay"), minPrice));
        }
        if (maxPrice != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("pricePerDay"), maxPrice));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
