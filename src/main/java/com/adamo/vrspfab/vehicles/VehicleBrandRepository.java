package com.adamo.vrspfab.vehicles;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleBrandRepository extends JpaRepository<VehicleBrand, Long> {
    
    Optional<VehicleBrand> findByName(String name);
    
    List<VehicleBrand> findByIsActiveTrue();
    
    boolean existsByName(String name);
}
