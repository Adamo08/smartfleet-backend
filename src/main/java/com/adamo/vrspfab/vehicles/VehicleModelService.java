package com.adamo.vrspfab.vehicles;

import com.adamo.vrspfab.reservations.ReservationRepository;
import com.adamo.vrspfab.reservations.ReservationStatus;
import com.adamo.vrspfab.vehicles.dto.CreateVehicleModelDto;
import com.adamo.vrspfab.vehicles.dto.UpdateVehicleModelDto;
import com.adamo.vrspfab.vehicles.dto.VehicleModelResponseDto;
import com.adamo.vrspfab.vehicles.exceptions.DuplicateVehicleModelException;
import com.adamo.vrspfab.vehicles.exceptions.VehicleBrandNotFoundException;
import com.adamo.vrspfab.vehicles.exceptions.VehicleModelNotFoundException;
import com.adamo.vrspfab.vehicles.mappers.EnhancedVehicleModelMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VehicleModelService {

    private final VehicleModelRepository modelRepository;
    private final VehicleBrandRepository brandRepository;
    private final EnhancedVehicleModelMapper modelMapper;
    private final VehicleRepository vehicleRepository;
    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "vehicleModels", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<VehicleModelResponseDto> getAllModels(Pageable pageable) {
        log.info("Fetching all vehicle models with pagination");
        Page<VehicleModel> modelsPage = modelRepository.findAll(pageable);
        return modelsPage.map(modelMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "vehicleModels", key = "#id")
    public VehicleModelResponseDto getModelById(Long id) {
        log.info("Fetching vehicle model with ID: {}", id);
        VehicleModel model = modelRepository.findById(id)
                .orElseThrow(() -> new VehicleModelNotFoundException(id));
        return modelMapper.toResponseDto(model);
    }

    @CacheEvict(value = "vehicleModels", allEntries = true)
    public VehicleModelResponseDto createModel(@Valid CreateVehicleModelDto createDto) {
        log.info("Creating new vehicle model: {}", createDto.getName());

        // Check if brand exists
        VehicleBrand brand = brandRepository.findById(createDto.getBrandId())
                .orElseThrow(() -> new VehicleBrandNotFoundException(createDto.getBrandId()));

        if (modelRepository.existsByNameAndBrandId(createDto.getName(), createDto.getBrandId())) {
            log.warn("Model creation failed: Model with name '{}' already exists for brand '{}'", 
                     createDto.getName(), brand.getName());
            throw new DuplicateVehicleModelException(createDto.getName(), brand.getName());
        }

        VehicleModel model = modelMapper.toEntity(createDto);
        model.setBrand(brand);
        VehicleModel savedModel = modelRepository.save(model);
        log.info("Vehicle model created successfully with ID: {}", savedModel.getId());
        return modelMapper.toResponseDto(savedModel);
    }

    @CacheEvict(value = "vehicleModels", allEntries = true)
    public VehicleModelResponseDto updateModel(Long id, @Valid UpdateVehicleModelDto updateDto) {
        log.info("Updating vehicle model with ID: {}", id);
        VehicleModel existingModel = modelRepository.findById(id)
                .orElseThrow(() -> new VehicleModelNotFoundException(id));

        // Check if brand exists if it's being changed
        if (updateDto.getBrandId() != null && !existingModel.getBrand().getId().equals(updateDto.getBrandId())) {
            VehicleBrand brand = brandRepository.findById(updateDto.getBrandId())
                    .orElseThrow(() -> new VehicleBrandNotFoundException(updateDto.getBrandId()));
            existingModel.setBrand(brand);
        }

        // Check for name conflicts if name is being changed
        if (updateDto.getName() != null && 
            !existingModel.getName().equals(updateDto.getName()) && 
            modelRepository.existsByNameAndBrandId(updateDto.getName(), 
                updateDto.getBrandId() != null ? updateDto.getBrandId() : existingModel.getBrand().getId())) {
            log.warn("Model update failed: Model with name '{}' already exists for this brand", updateDto.getName());
            throw new DuplicateVehicleModelException(updateDto.getName(), existingModel.getBrand().getName());
        }

        modelMapper.updateEntity(existingModel, updateDto);
        VehicleModel updatedModel = modelRepository.save(existingModel);
        log.info("Vehicle model with ID {} updated successfully", id);
        return modelMapper.toResponseDto(updatedModel);
    }

    @CacheEvict(value = "vehicleModels", allEntries = true)
    public void deleteModel(Long id) {
        log.info("Deleting vehicle model with ID: {}", id);
        if (!modelRepository.existsById(id)) {
            throw new VehicleModelNotFoundException(id);
        }
        modelRepository.deleteById(id);
        log.info("Vehicle model with ID {} deleted successfully", id);
    }

    @CacheEvict(value = "vehicleModels", allEntries = true)
    public VehicleModelResponseDto toggleModelStatus(Long id) {
        log.info("Toggling status for vehicle model with ID: {}", id);
        VehicleModel model = modelRepository.findById(id)
                .orElseThrow(() -> new VehicleModelNotFoundException(id));

        boolean wasActive = model.getIsActive();
        model.setIsActive(!model.getIsActive());
        VehicleModel updatedModel = modelRepository.save(model);
        
        // Log business impact
        if (wasActive && !updatedModel.getIsActive()) {
            log.warn("Model '{}' (ID: {}) deactivated - vehicles of this model will no longer be available for booking", 
                    updatedModel.getName(), id);
        } else if (!wasActive && updatedModel.getIsActive()) {
            log.info("Model '{}' (ID: {}) activated - vehicles of this model are now available for booking", 
                    updatedModel.getName(), id);
        }
        
        return modelMapper.toResponseDto(updatedModel);
    }

    /**
     * Get only active models for customer-facing dropdowns and filters.
     * This ensures inactive models don't appear in booking interfaces.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "activeModels")
    public java.util.List<VehicleModelResponseDto> getActiveModels() {
        log.info("Fetching only active vehicle models for customer interface");
        java.util.List<VehicleModel> activeModels = modelRepository.findByIsActiveTrue();
        return activeModels.stream()
                .map(modelMapper::toResponseDto)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get active models for a specific brand for customer dropdowns.
     * Only returns models that are active AND belong to an active brand.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "activeModelsByBrand", key = "#brandId")
    public java.util.List<VehicleModelResponseDto> getActiveModelsByBrandId(Long brandId) {
        log.info("Fetching active vehicle models for brand ID: {}", brandId);
        java.util.List<VehicleModel> activeModels = modelRepository.findActiveModelsByBrandId(brandId);
        return activeModels.stream()
                .map(modelMapper::toResponseDto)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get status information for vehicles that would be affected by deactivating this model
     */
    @Transactional(readOnly = true)
    public VehicleStatusInfo getModelStatusInfo(Long id) {
        log.info("Getting status info for vehicle model with ID: {}", id);
        
        // Verify model exists
        VehicleModel model = modelRepository.findById(id)
                .orElseThrow(() -> new VehicleModelNotFoundException(id));
        
        // Get all vehicles for this model
        List<Vehicle> vehicles = vehicleRepository.findByModelId(id);
        
        // Count active and inactive vehicles
        long activeVehicles = vehicles.stream()
                .mapToLong(vehicle -> vehicle.getStatus() == VehicleStatus.AVAILABLE ? 1 : 0)
                .sum();
        
        long inactiveVehicles = vehicles.size() - activeVehicles;
        
        // Check for active reservations (pending or confirmed)
        boolean hasActiveReservations = false;
        int futureReservations = 0;
        
        for (Vehicle vehicle : vehicles) {
            Long activeCount = reservationRepository.countByVehicleIdAndStatusIn(
                    vehicle.getId(),
                    List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED)
            );
            
            if (activeCount > 0) {
                hasActiveReservations = true;
                futureReservations += activeCount.intValue();
            }
        }
        
        return VehicleStatusInfo.builder()
                .totalVehicles(vehicles.size())
                .affectedVehicles(vehicles.size()) // All vehicles will be affected
                .activeVehicles((int) activeVehicles)
                .inactiveVehicles((int) inactiveVehicles)
                .hasActiveReservations(hasActiveReservations)
                .futureReservations(futureReservations)
                .build();
    }

    // Legacy method for backward compatibility - delegates to new implementation
    @Deprecated
    public VehicleModelDto getModelByIdLegacy(Long id) {
        VehicleModelResponseDto responseDto = getModelById(id);
        VehicleModelDto legacyDto = new VehicleModelDto();
        legacyDto.setId(responseDto.getId());
        legacyDto.setName(responseDto.getName());
        legacyDto.setBrandId(responseDto.getBrandId());
        legacyDto.setBrandName(responseDto.getBrandName());
        legacyDto.setDescription(responseDto.getDescription());
        legacyDto.setIsActive(responseDto.getIsActive());
        legacyDto.setCreatedAt(responseDto.getCreatedAt().toString());
        legacyDto.setUpdatedAt(responseDto.getUpdatedAt().toString());
        return legacyDto;
    }
}
