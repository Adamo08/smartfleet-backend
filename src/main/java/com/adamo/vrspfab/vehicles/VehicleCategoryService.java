package com.adamo.vrspfab.vehicles;

import com.adamo.vrspfab.reservations.ReservationRepository;
import com.adamo.vrspfab.reservations.ReservationStatus;
import com.adamo.vrspfab.vehicles.dto.CreateVehicleCategoryDto;
import com.adamo.vrspfab.vehicles.dto.UpdateVehicleCategoryDto;
import com.adamo.vrspfab.vehicles.dto.VehicleCategoryResponseDto;
import com.adamo.vrspfab.vehicles.exceptions.DuplicateVehicleCategoryException;
import com.adamo.vrspfab.vehicles.exceptions.VehicleCategoryNotFoundException;
import com.adamo.vrspfab.vehicles.mappers.EnhancedVehicleCategoryMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VehicleCategoryService {

    private final VehicleCategoryRepository categoryRepository;
    private final EnhancedVehicleCategoryMapper categoryMapper;
    private final VehicleRepository vehicleRepository;
    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "vehicleCategories", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<VehicleCategoryResponseDto> getAllCategories(Pageable pageable) {
        log.info("Fetching all vehicle categories with pagination");
        Page<VehicleCategory> categoriesPage = categoryRepository.findAll(pageable);
        return categoriesPage.map(categoryMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "vehicleCategories", key = "#id")
    public VehicleCategoryResponseDto getCategoryById(Long id) {
        log.info("Fetching vehicle category with ID: {}", id);
        VehicleCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new VehicleCategoryNotFoundException(id));
        return categoryMapper.toResponseDto(category);
    }

    @CacheEvict(value = "vehicleCategories", allEntries = true)
    public VehicleCategoryResponseDto createCategory(@Valid CreateVehicleCategoryDto createDto) {
        log.info("Creating new vehicle category: {}", createDto.getName());
        
        if (categoryRepository.existsByName(createDto.getName())) {
            log.warn("Category creation failed: Category with name '{}' already exists", createDto.getName());
            throw new DuplicateVehicleCategoryException(createDto.getName());
        }

        VehicleCategory category = categoryMapper.toEntity(createDto);
        VehicleCategory savedCategory = categoryRepository.save(category);
        log.info("Vehicle category created successfully with ID: {}", savedCategory.getId());
        return categoryMapper.toResponseDto(savedCategory);
    }

    @CacheEvict(value = "vehicleCategories", allEntries = true)
    public VehicleCategoryResponseDto updateCategory(Long id, @Valid UpdateVehicleCategoryDto updateDto) {
        log.info("Updating vehicle category with ID: {}", id);
        VehicleCategory existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new VehicleCategoryNotFoundException(id));

        // Check for name conflicts if name is being changed
        if (updateDto.getName() != null && 
            !existingCategory.getName().equals(updateDto.getName()) && 
            categoryRepository.existsByName(updateDto.getName())) {
            log.warn("Category update failed: Category with name '{}' already exists", updateDto.getName());
            throw new DuplicateVehicleCategoryException(updateDto.getName());
        }

        categoryMapper.updateEntity(existingCategory, updateDto);
        VehicleCategory updatedCategory = categoryRepository.save(existingCategory);
        log.info("Vehicle category with ID {} updated successfully", id);
        return categoryMapper.toResponseDto(updatedCategory);
    }

    @CacheEvict(value = "vehicleCategories", allEntries = true)
    public void deleteCategory(Long id) {
        log.info("Deleting vehicle category with ID: {}", id);
        if (!categoryRepository.existsById(id)) {
            throw new VehicleCategoryNotFoundException(id);
        }
        categoryRepository.deleteById(id);
        log.info("Vehicle category with ID {} deleted successfully", id);
    }

    @CacheEvict(value = "vehicleCategories", allEntries = true)
    public VehicleCategoryResponseDto toggleCategoryStatus(Long id) {
        log.info("Toggling status for vehicle category with ID: {}", id);
        VehicleCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new VehicleCategoryNotFoundException(id));

        boolean wasActive = category.getIsActive();
        category.setIsActive(!category.getIsActive());
        VehicleCategory updatedCategory = categoryRepository.save(category);
        
        // Log business impact
        if (wasActive && !updatedCategory.getIsActive()) {
            log.warn("Category '{}' (ID: {}) deactivated - vehicles of this category will no longer be available for booking", 
                    updatedCategory.getName(), id);
        } else if (!wasActive && updatedCategory.getIsActive()) {
            log.info("Category '{}' (ID: {}) activated - vehicles of this category are now available for booking", 
                    updatedCategory.getName(), id);
        }
        
        return categoryMapper.toResponseDto(updatedCategory);
    }

    /**
     * Get only active categories for customer-facing dropdowns and filters.
     * This ensures inactive categories don't appear in booking interfaces.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "activeCategories")
    public java.util.List<VehicleCategoryResponseDto> getActiveCategories() {
        log.info("Fetching only active vehicle categories for customer interface");
        java.util.List<VehicleCategory> activeCategories = categoryRepository.findByIsActiveTrue();
        return activeCategories.stream()
                .map(categoryMapper::toResponseDto)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get status information for vehicles that would be affected by deactivating this category
     */
    @Transactional(readOnly = true)
    public VehicleStatusInfo getCategoryStatusInfo(Long id) {
        log.info("Getting status info for vehicle category with ID: {}", id);
        
        // Verify category exists
        VehicleCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new VehicleCategoryNotFoundException(id));
        
        // Get all vehicles for this category
        List<Vehicle> vehicles = vehicleRepository.findByCategoryId(id);
        
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
    public VehicleCategoryDto getCategoryByIdLegacy(Long id) {
        VehicleCategoryResponseDto responseDto = getCategoryById(id);
        VehicleCategoryDto legacyDto = new VehicleCategoryDto();
        legacyDto.setId(responseDto.getId());
        legacyDto.setName(responseDto.getName());
        legacyDto.setDescription(responseDto.getDescription());
        legacyDto.setIconUrl(responseDto.getIconUrl());
        legacyDto.setIsActive(responseDto.getIsActive());
        legacyDto.setCreatedAt(responseDto.getCreatedAt().toString());
        legacyDto.setUpdatedAt(responseDto.getUpdatedAt().toString());
        return legacyDto;
    }
}
