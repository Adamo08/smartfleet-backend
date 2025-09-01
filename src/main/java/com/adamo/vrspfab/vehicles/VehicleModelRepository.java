package com.adamo.vrspfab.vehicles;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleModelRepository extends JpaRepository<VehicleModel, Long> {
    
    Optional<VehicleModel> findByName(String name);
    
    boolean existsByNameAndBrandId(String name, Long brandId);

    /**
     * Find models by brand ID
     */
    List<VehicleModel> findByBrandId(Long brandId);
    
    /**
     * Find only active models for customer-facing interfaces.
     * Inactive models should not appear in booking dropdowns.
     */
    List<VehicleModel> findByIsActiveTrue();
    
    /**
     * Find active models by brand ID for customer dropdowns.
     * Only shows models that are active AND belong to an active brand.
     */
    @Query("SELECT m FROM VehicleModel m WHERE m.brand.id = :brandId AND m.isActive = true AND m.brand.isActive = true")
    List<VehicleModel> findActiveModelsByBrandId(Long brandId);
}
