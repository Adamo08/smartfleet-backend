package com.adamo.vrspfab.vehicles;

import com.adamo.vrspfab.reservations.ReservationRepository;
import com.adamo.vrspfab.reservations.ReservationStatus;
import com.adamo.vrspfab.vehicles.dto.CreateVehicleBrandDto;
import com.adamo.vrspfab.vehicles.dto.UpdateVehicleBrandDto;
import com.adamo.vrspfab.vehicles.dto.VehicleBrandResponseDto;
import com.adamo.vrspfab.vehicles.exceptions.DuplicateVehicleBrandException;
import com.adamo.vrspfab.vehicles.exceptions.VehicleBrandNotFoundException;
import com.adamo.vrspfab.vehicles.mappers.EnhancedVehicleBrandMapper;
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
public class VehicleBrandService {

    private final VehicleBrandRepository brandRepository;
    private final EnhancedVehicleBrandMapper brandMapper;
    private final VehicleDeletionService deletionService;
    private final VehicleRepository vehicleRepository;
    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "vehicleBrands", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<VehicleBrandResponseDto> getAllBrands(Pageable pageable) {
        log.info("Fetching all vehicle brands with pagination");
        Page<VehicleBrand> brandsPage = brandRepository.findAll(pageable);
        return brandsPage.map(brandMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "vehicleBrands", key = "#id")
    public VehicleBrandResponseDto getBrandById(Long id) {
        log.info("Fetching vehicle brand with ID: {}", id);
        VehicleBrand brand = brandRepository.findById(id)
                .orElseThrow(() -> new VehicleBrandNotFoundException(id));
        return brandMapper.toResponseDto(brand);
    }

    @CacheEvict(value = "vehicleBrands", allEntries = true)
    public VehicleBrandResponseDto createBrand(@Valid CreateVehicleBrandDto createDto) {
        log.info("Creating new vehicle brand: {}", createDto.getName());
        
        if (brandRepository.existsByName(createDto.getName())) {
            log.warn("Brand creation failed: Brand with name '{}' already exists", createDto.getName());
            throw new DuplicateVehicleBrandException(createDto.getName());
        }

        VehicleBrand brand = brandMapper.toEntity(createDto);
        VehicleBrand savedBrand = brandRepository.save(brand);
        log.info("Vehicle brand created successfully with ID: {}", savedBrand.getId());
        return brandMapper.toResponseDto(savedBrand);
    }

    @CacheEvict(value = "vehicleBrands", allEntries = true)
    public VehicleBrandResponseDto updateBrand(Long id, @Valid UpdateVehicleBrandDto updateDto) {
        log.info("Updating vehicle brand with ID: {}", id);
        VehicleBrand existingBrand = brandRepository.findById(id)
                .orElseThrow(() -> new VehicleBrandNotFoundException(id));

        // Check for name conflicts if name is being changed
        if (updateDto.getName() != null && 
            !existingBrand.getName().equals(updateDto.getName()) && 
            brandRepository.existsByName(updateDto.getName())) {
            log.warn("Brand update failed: Brand with name '{}' already exists", updateDto.getName());
            throw new DuplicateVehicleBrandException(updateDto.getName());
        }

        brandMapper.updateEntity(existingBrand, updateDto);
        VehicleBrand updatedBrand = brandRepository.save(existingBrand);
        log.info("Vehicle brand with ID {} updated successfully", id);
        return brandMapper.toResponseDto(updatedBrand);
    }

    @CacheEvict(value = "vehicleBrands", allEntries = true)
    public VehicleDeletionResult deleteBrand(Long id) {
        log.info("Attempting to delete vehicle brand with ID: {}", id);
        if (!brandRepository.existsById(id)) {
            throw new VehicleBrandNotFoundException(id);
        }
        
        // Use deletion service for safe cascade delete
        return deletionService.deleteBrand(id);
    }

    @CacheEvict(value = "vehicleBrands", allEntries = true)
    public VehicleBrandResponseDto toggleBrandStatus(Long id) {
        log.info("Toggling status for vehicle brand with ID: {}", id);
        VehicleBrand brand = brandRepository.findById(id)
                .orElseThrow(() -> new VehicleBrandNotFoundException(id));

        boolean wasActive = brand.getIsActive();
        brand.setIsActive(!brand.getIsActive());
        VehicleBrand updatedBrand = brandRepository.save(brand);
        
        // Log business impact
        if (wasActive && !updatedBrand.getIsActive()) {
            log.warn("Brand '{}' (ID: {}) deactivated - vehicles of this brand will no longer be available for booking", 
                    updatedBrand.getName(), id);
        } else if (!wasActive && updatedBrand.getIsActive()) {
            log.info("Brand '{}' (ID: {}) activated - vehicles of this brand are now available for booking", 
                    updatedBrand.getName(), id);
        }
        
        return brandMapper.toResponseDto(updatedBrand);
    }

    /**
     * Get only active brands for customer-facing dropdowns and filters.
     * This ensures inactive brands don't appear in booking interfaces.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "activeBrands")
    public List<VehicleBrandResponseDto> getActiveBrands() {
        log.info("Fetching only active vehicle brands for customer interface");
        List<VehicleBrand> activeBrands = brandRepository.findByIsActiveTrue();
        return activeBrands.stream()
                .map(brandMapper::toResponseDto)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get status information for vehicles that would be affected by deactivating this brand
     */
    @Transactional(readOnly = true)
    public VehicleStatusInfo getBrandStatusInfo(Long id) {
        log.info("Getting status info for vehicle brand with ID: {}", id);
        
        // Verify brand exists
        VehicleBrand brand = brandRepository.findById(id)
                .orElseThrow(() -> new VehicleBrandNotFoundException(id));
        
        // Get all vehicles for this brand
        List<Vehicle> vehicles = vehicleRepository.findByBrandId(id);
        
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
    public VehicleBrandDto getBrandByIdLegacy(Long id) {
        VehicleBrandResponseDto responseDto = getBrandById(id);
        VehicleBrandDto legacyDto = new VehicleBrandDto();
        legacyDto.setId(responseDto.getId());
        legacyDto.setName(responseDto.getName());
        legacyDto.setDescription(responseDto.getDescription());
        legacyDto.setLogoUrl(responseDto.getLogoUrl());
        legacyDto.setCountryOfOrigin(responseDto.getCountryOfOrigin());
        legacyDto.setIsActive(responseDto.getIsActive());
        return legacyDto;
    }
}
