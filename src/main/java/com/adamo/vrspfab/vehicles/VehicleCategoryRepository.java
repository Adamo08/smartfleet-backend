package com.adamo.vrspfab.vehicles;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleCategoryRepository extends JpaRepository<VehicleCategory, Long> {
    
    Optional<VehicleCategory> findByName(String name);
    
    boolean existsByName(String name);
    
    /**
     * Find only active categories for customer-facing interfaces.
     * Inactive categories should not appear in booking dropdowns.
     */
    List<VehicleCategory> findByIsActiveTrue();
}
