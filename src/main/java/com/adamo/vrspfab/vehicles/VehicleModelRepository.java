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
}
