package com.adamo.vrspfab.vehicles;

import com.adamo.vrspfab.reservations.ReservationRepository;
import com.adamo.vrspfab.reservations.ReservationStatus;
import com.adamo.vrspfab.vehicles.exceptions.VehicleBrandNotFoundException;
import com.adamo.vrspfab.vehicles.exceptions.VehicleCategoryNotFoundException;
import com.adamo.vrspfab.vehicles.exceptions.VehicleModelNotFoundException;
import com.adamo.vrspfab.vehicles.exceptions.VehicleNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleDeletionService {

    private final VehicleRepository vehicleRepository;
    private final VehicleBrandRepository brandRepository;
    private final VehicleCategoryRepository categoryRepository;
    private final VehicleModelRepository modelRepository;
    private final ReservationRepository reservationRepository;

    /**
     * Safely delete a vehicle brand with cascade validation
     */
    @Transactional
    public VehicleDeletionResult deleteBrand(Long brandId) {
        log.info("Attempting to delete vehicle brand with ID: {}", brandId);
        
        VehicleBrand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new VehicleBrandNotFoundException(brandId));

        // Get all vehicles for this brand
        List<Vehicle> brandVehicles = vehicleRepository.findByBrandId(brandId);
        
        // Check for active reservations
        VehicleDeletionResult validationResult = validateVehicleDeletion(brandVehicles);
        if (!validationResult.isCanDelete()) {
            log.warn("Cannot delete brand '{}': {}", brand.getName(), validationResult.getReason());
            return validationResult;
        }

        // Safe to delete - cascade will handle vehicles and models
        brandRepository.deleteById(brandId);
        log.info("Successfully deleted vehicle brand '{}' and {} related vehicles", 
                brand.getName(), brandVehicles.size());

        return VehicleDeletionResult.success(
                String.format("Brand '%s' and %d related vehicles deleted successfully", 
                        brand.getName(), brandVehicles.size())
        );
    }

    /**
     * Safely delete a vehicle category with cascade validation
     */
    @Transactional
    public VehicleDeletionResult deleteCategory(Long categoryId) {
        log.info("Attempting to delete vehicle category with ID: {}", categoryId);
        
        VehicleCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new VehicleCategoryNotFoundException(categoryId));

        // Get all vehicles for this category
        List<Vehicle> categoryVehicles = vehicleRepository.findByCategoryId(categoryId);
        
        // Check for active reservations
        VehicleDeletionResult validationResult = validateVehicleDeletion(categoryVehicles);
        if (!validationResult.isCanDelete()) {
            log.warn("Cannot delete category '{}': {}", category.getName(), validationResult.getReason());
            return validationResult;
        }

        // Safe to delete - cascade will handle vehicles
        categoryRepository.deleteById(categoryId);
        log.info("Successfully deleted vehicle category '{}' and {} related vehicles", 
                category.getName(), categoryVehicles.size());

        return VehicleDeletionResult.success(
                String.format("Category '%s' and %d related vehicles deleted successfully", 
                        category.getName(), categoryVehicles.size())
        );
    }

    /**
     * Safely delete a vehicle model with cascade validation
     */
    @Transactional
    public VehicleDeletionResult deleteModel(Long modelId) {
        log.info("Attempting to delete vehicle model with ID: {}", modelId);
        
        VehicleModel model = modelRepository.findById(modelId)
                .orElseThrow(() -> new VehicleModelNotFoundException(modelId));

        // Get all vehicles for this model
        List<Vehicle> modelVehicles = vehicleRepository.findByModelId(modelId);
        
        // Check for active reservations
        VehicleDeletionResult validationResult = validateVehicleDeletion(modelVehicles);
        if (!validationResult.isCanDelete()) {
            log.warn("Cannot delete model '{}': {}", model.getName(), validationResult.getReason());
            return validationResult;
        }

        // Safe to delete - cascade will handle vehicles
        modelRepository.deleteById(modelId);
        log.info("Successfully deleted vehicle model '{}' and {} related vehicles", 
                model.getName(), modelVehicles.size());

        return VehicleDeletionResult.success(
                String.format("Model '%s' and %d related vehicles deleted successfully", 
                        model.getName(), modelVehicles.size())
        );
    }

    /**
     * Safely delete a vehicle with reservation validation
     */
    @Transactional
    public VehicleDeletionResult deleteVehicle(Long vehicleId) {
        log.info("Attempting to delete vehicle with ID: {}", vehicleId);
        
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId));

        // Check for active reservations on this specific vehicle
        VehicleDeletionResult validationResult = validateVehicleDeletion(List.of(vehicle));
        if (!validationResult.isCanDelete()) {
            log.warn("Cannot delete vehicle '{}': {}", vehicle.getLicensePlate(), validationResult.getReason());
            return validationResult;
        }

        vehicleRepository.deleteById(vehicleId);
        log.info("Successfully deleted vehicle '{}'", vehicle.getLicensePlate());

        return VehicleDeletionResult.success(
                String.format("Vehicle '%s' deleted successfully", vehicle.getLicensePlate())
        );
    }

    /**
     * Validate if vehicles can be safely deleted
     */
    private VehicleDeletionResult validateVehicleDeletion(List<Vehicle> vehicles) {
        if (vehicles.isEmpty()) {
            return VehicleDeletionResult.success("No vehicles to delete");
        }

        // Check for active reservations (PENDING, CONFIRMED)
        for (Vehicle vehicle : vehicles) {
            long activeReservations = reservationRepository.countByVehicleIdAndStatusIn(
                    vehicle.getId(), 
                    List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED)
            );

            if (activeReservations > 0) {
                return VehicleDeletionResult.failure(
                        String.format("Vehicle '%s' has %d active reservation(s). Cannot delete.", 
                                vehicle.getLicensePlate(), activeReservations)
                );
            }
        }

        return VehicleDeletionResult.success("Safe to delete");
    }

    /**
     * Get deletion impact analysis
     */
    @Transactional(readOnly = true)
    public VehicleDeletionImpact analyzeBrandDeletionImpact(Long brandId) {
        VehicleBrand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new VehicleBrandNotFoundException(brandId));

        List<Vehicle> affectedVehicles = vehicleRepository.findByBrandId(brandId);
        List<VehicleModel> affectedModels = modelRepository.findByBrandId(brandId);
        
        long activeReservations = affectedVehicles.stream()
                .mapToLong(vehicle -> reservationRepository.countByVehicleIdAndStatusIn(
                        vehicle.getId(), 
                        List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED)
                ))
                .sum();

        return VehicleDeletionImpact.builder()
                .entityName(brand.getName())
                .entityType("Brand")
                .vehiclesAffected(affectedVehicles.size())
                .modelsAffected(affectedModels.size())
                .activeReservations((int) activeReservations)
                .canDelete(activeReservations == 0)
                .build();
    }

    @Transactional(readOnly = true)
    public VehicleDeletionImpact analyzeCategoryDeletionImpact(Long categoryId) {
        VehicleCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new VehicleCategoryNotFoundException(categoryId));

        List<Vehicle> affectedVehicles = vehicleRepository.findByCategoryId(categoryId);
        
        long activeReservations = affectedVehicles.stream()
                .mapToLong(vehicle -> reservationRepository.countByVehicleIdAndStatusIn(
                        vehicle.getId(), 
                        List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED)
                ))
                .sum();

        return VehicleDeletionImpact.builder()
                .entityName(category.getName())
                .entityType("Category")
                .vehiclesAffected(affectedVehicles.size())
                .modelsAffected(0)
                .activeReservations((int) activeReservations)
                .canDelete(activeReservations == 0)
                .build();
    }

    @Transactional(readOnly = true)
    public VehicleDeletionImpact analyzeModelDeletionImpact(Long modelId) {
        VehicleModel model = modelRepository.findById(modelId)
                .orElseThrow(() -> new VehicleModelNotFoundException(modelId));

        List<Vehicle> affectedVehicles = vehicleRepository.findByModelId(modelId);
        
        long activeReservations = affectedVehicles.stream()
                .mapToLong(vehicle -> reservationRepository.countByVehicleIdAndStatusIn(
                        vehicle.getId(), 
                        List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED)
                ))
                .sum();

        return VehicleDeletionImpact.builder()
                .entityName(model.getName())
                .entityType("Model")
                .vehiclesAffected(affectedVehicles.size())
                .modelsAffected(0)
                .activeReservations((int) activeReservations)
                .canDelete(activeReservations == 0)
                .build();
    }
}
