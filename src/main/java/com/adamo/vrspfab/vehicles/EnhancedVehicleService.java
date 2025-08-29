package com.adamo.vrspfab.vehicles;

import com.adamo.vrspfab.common.ResourceNotFoundException;
import com.adamo.vrspfab.common.SecurityUtilsService;
import com.adamo.vrspfab.dashboard.ActivityEventListener;
import com.adamo.vrspfab.vehicles.dto.CreateVehicleDto;
import com.adamo.vrspfab.vehicles.dto.UpdateVehicleDto;
import com.adamo.vrspfab.vehicles.dto.VehicleResponseDto;
import com.adamo.vrspfab.vehicles.exceptions.*;
import com.adamo.vrspfab.vehicles.mappers.EnhancedVehicleMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class EnhancedVehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleCategoryRepository vehicleCategoryRepository;
    private final VehicleBrandRepository vehicleBrandRepository;
    private final VehicleModelRepository vehicleModelRepository;
    private final EnhancedVehicleMapper vehicleMapper;
    private final ActivityEventListener activityEventListener;
    private final SecurityUtilsService securityUtilsService;

    /**
     * Creates a new vehicle with comprehensive validation.
     */
    @CacheEvict(value = {"vehicles", "vehicleReservations"}, allEntries = true)
    public VehicleResponseDto createVehicle(@Valid CreateVehicleDto createDto) {
        log.info("Creating new vehicle with license plate: {}", createDto.getLicensePlate());
        
        // Validate license plate uniqueness
        if (vehicleRepository.findByLicensePlate(createDto.getLicensePlate()).isPresent()) {
            log.warn("Vehicle creation failed: License plate {} already exists", createDto.getLicensePlate());
            throw new DuplicateLicensePlateException("A vehicle with license plate '" + createDto.getLicensePlate() + "' already exists.");
        }
        
        // Validate year
        if (createDto.getYear() > LocalDate.now().getYear()) {
            log.warn("Vehicle creation failed: Year {} is in the future", createDto.getYear());
            throw new InvalidVehicleDataException("Vehicle year cannot be in the future.");
        }
        
        Vehicle vehicle = vehicleMapper.toEntity(createDto);
        
        // Set related entities with proper validation
        setVehicleRelations(vehicle, createDto.getCategoryId(), createDto.getBrandId(), createDto.getModelId());
        
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle created successfully with ID: {}", savedVehicle.getId());
        
        // Record vehicle creation activity
        try {
            var currentUser = securityUtilsService.getCurrentAuthenticatedUser();
            String vehicleName = savedVehicle.getBrand().getName() + " " + savedVehicle.getModel().getName();
            activityEventListener.recordVehicleCreated(savedVehicle.getId(), vehicleName, currentUser);
        } catch (Exception e) {
            log.warn("Could not record vehicle creation activity: {}", e.getMessage());
        }
        
        return vehicleMapper.toResponseDto(savedVehicle);
    }

    /**
     * Creates multiple vehicles in bulk with validation.
     */
    @CacheEvict(value = {"vehicles", "vehicleReservations"}, allEntries = true)
    public List<VehicleResponseDto> createVehiclesBulk(@Valid List<CreateVehicleDto> createDtos) {
        log.info("Creating {} vehicles in bulk", createDtos.size());
        
        // Validate all license plates are unique
        List<String> licensePlates = createDtos.stream()
                .map(CreateVehicleDto::getLicensePlate)
                .collect(Collectors.toList());
        
        for (String licensePlate : licensePlates) {
            if (vehicleRepository.findByLicensePlate(licensePlate).isPresent()) {
                log.warn("Bulk creation failed: License plate {} already exists", licensePlate);
                throw new DuplicateLicensePlateException("A vehicle with license plate '" + licensePlate + "' already exists.");
            }
        }
        
        // Validate years
        for (CreateVehicleDto dto : createDtos) {
            if (dto.getYear() > LocalDate.now().getYear()) {
                log.warn("Bulk creation failed: Year {} is in the future for license plate {}", dto.getYear(), dto.getLicensePlate());
                throw new InvalidVehicleDataException("Vehicle year cannot be in the future for license plate: " + dto.getLicensePlate());
            }
        }
        
        List<Vehicle> vehicles = createDtos.stream()
                .map(createDto -> {
                    Vehicle vehicle = vehicleMapper.toEntity(createDto);
                    setVehicleRelations(vehicle, createDto.getCategoryId(), createDto.getBrandId(), createDto.getModelId());
                    return vehicle;
                })
                .collect(Collectors.toList());
        
        List<Vehicle> savedVehicles = vehicleRepository.saveAll(vehicles);
        log.info("Successfully created {} vehicles in bulk", savedVehicles.size());
        
        return savedVehicles.stream()
                .map(vehicleMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a vehicle by its ID.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "vehicles", key = "#id")
    public VehicleResponseDto getVehicleById(Long id) {
        log.debug("Fetching vehicle with ID: {}", id);
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Vehicle not found with ID: {}", id);
                    return new ResourceNotFoundException("Vehicle not found with ID: " + id, "Vehicle");
                });
        return vehicleMapper.toResponseDto(vehicle);
    }

    /**
     * Retrieves all vehicles with pagination and filtering.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "vehicles", key = "#page + '-' + #size + '-' + #sortBy + '-' + #sortDirection + '-' + #filters.hashCode()")
    public Page<VehicleResponseDto> getAllVehicles(
            int page, int size, String sortBy, String sortDirection, VehicleFilter filters
    ) {
        log.debug("Fetching all vehicles: page={}, size={}, sortBy={}, sortDirection={}",
                page, size, sortBy, sortDirection);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        VehicleSpecification spec = new VehicleSpecification(filters);
        Page<Vehicle> vehicles = vehicleRepository.findAll(spec, pageable);
        return vehicles.map(vehicleMapper::toResponseDto);
    }

    /**
     * Updates an existing vehicle.
     */
    @CacheEvict(value = {"vehicles", "vehicleReservations"}, allEntries = true)
    public VehicleResponseDto updateVehicle(Long id, @Valid UpdateVehicleDto updateDto) {
        log.info("Updating vehicle with ID: {}", id);
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Vehicle update failed: Vehicle not found with ID: {}", id);
                    return new ResourceNotFoundException("Vehicle not found with ID: " + id, "Vehicle");
                });

        // Validate license plate uniqueness if being changed
        if (updateDto.getLicensePlate() != null && 
            !vehicle.getLicensePlate().equals(updateDto.getLicensePlate()) &&
            vehicleRepository.findByLicensePlate(updateDto.getLicensePlate()).isPresent()) {
            log.warn("Vehicle update failed: License plate {} already exists", updateDto.getLicensePlate());
            throw new DuplicateLicensePlateException("A vehicle with license plate '" + updateDto.getLicensePlate() + "' already exists.");
        }
        
        // Validate year if being changed
        if (updateDto.getYear() != null && updateDto.getYear() > LocalDate.now().getYear()) {
            log.warn("Vehicle update failed: Year {} is in the future", updateDto.getYear());
            throw new InvalidVehicleDataException("Vehicle year cannot be in the future.");
        }
        
        // Update related entities if IDs are provided
        if (updateDto.getCategoryId() != null || updateDto.getBrandId() != null || updateDto.getModelId() != null) {
            setVehicleRelations(vehicle, 
                updateDto.getCategoryId(), 
                updateDto.getBrandId(), 
                updateDto.getModelId());
        }
        
        vehicleMapper.updateVehicleFromDto(updateDto, vehicle);
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle with ID {} updated successfully", id);
        return vehicleMapper.toResponseDto(updatedVehicle);
    }

    /**
     * Updates the status of a vehicle.
     */
    @CacheEvict(value = {"vehicles", "vehicleReservations"}, allEntries = true)
    public VehicleResponseDto updateVehicleStatus(Long id, VehicleStatus newStatus) {
        log.info("Updating status of vehicle with ID {} to {}", id, newStatus);
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Status update failed: Vehicle not found with ID: {}", id);
                    return new ResourceNotFoundException("Vehicle not found with ID: " + id, "Vehicle");
                });

        if (vehicle.getStatus().equals(VehicleStatus.DECOMMISSIONED)) {
            log.warn("Status update failed: Cannot update status of a decommissioned vehicle with ID: {}", id);
            throw new VehicleDecommissionedException("Cannot update a decommissioned vehicle.");
        }

        if (vehicle.getStatus().equals(newStatus)) {
            log.warn("Status update failed: Vehicle with ID {} is already in status {}", id, newStatus);
            throw new InvalidVehicleStatusUpdateException("Vehicle is already in status: " + newStatus.name());
        }

        if (!vehicle.getStatus().equals(VehicleStatus.AVAILABLE) && newStatus.equals(VehicleStatus.RENTED)) {
            log.warn("Status update failed: Cannot rent a vehicle that is not in AVAILABLE status (current status: {})", vehicle.getStatus());
            throw new InvalidVehicleStatusUpdateException("Cannot rent a vehicle that is not available.");
        }

        vehicle.setStatus(newStatus);
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        log.info("Status of vehicle with ID {} updated to {} successfully", id, newStatus);
        return vehicleMapper.toResponseDto(updatedVehicle);
    }

    /**
     * Updates the mileage of a vehicle.
     */
    @CacheEvict(value = {"vehicles", "vehicleReservations"}, allEntries = true)
    public VehicleResponseDto updateVehicleMileage(Long id, Float newMileage) {
        log.info("Updating mileage of vehicle with ID {} to {}", id, newMileage);
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Mileage update failed: Vehicle not found with ID: {}", id);
                    return new ResourceNotFoundException("Vehicle not found with ID: " + id, "Vehicle");
                });

        if (vehicle.getStatus().equals(VehicleStatus.DECOMMISSIONED)) {
            log.warn("Mileage update failed: Cannot update mileage of a decommissioned vehicle with ID: {}", id);
            throw new VehicleDecommissionedException("Cannot update mileage for a decommissioned vehicle.");
        }

        if (newMileage < vehicle.getMileage()) {
            log.warn("Mileage update failed: New mileage {} is less than current mileage {}", newMileage, vehicle.getMileage());
            throw new IllegalArgumentException("New mileage cannot be less than the current mileage.");
        }

        if (newMileage - vehicle.getMileage() > 10000) {
            log.warn("Mileage update failed: New mileage {} is implausibly high compared to current mileage {}", newMileage, vehicle.getMileage());
            throw new InvalidVehicleDataException("New mileage increase is implausible.");
        }

        vehicle.setMileage(newMileage);
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        log.info("Mileage of vehicle with ID {} updated to {} successfully", id, newMileage);
        return vehicleMapper.toResponseDto(updatedVehicle);
    }

    /**
     * Deletes a vehicle by its ID.
     */
    @CacheEvict(value = {"vehicles", "vehicleReservations"}, allEntries = true)
    public void deleteVehicle(Long id) {
        log.info("Deleting vehicle with ID: {}", id);
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Vehicle deletion failed: Vehicle not found with ID: {}", id);
                    return new ResourceNotFoundException("Vehicle not found with ID: " + id, "Vehicle");
                });

        if (vehicle.getStatus().equals(VehicleStatus.DECOMMISSIONED)) {
            log.warn("Vehicle deletion failed: Cannot delete a decommissioned vehicle with ID: {}", id);
            throw new VehicleDecommissionedException("Cannot delete a decommissioned vehicle.");
        }

        vehicleRepository.deleteById(id);
        log.info("Vehicle with ID {} deleted successfully", id);
    }

    /**
     * Checks if a vehicle is available for a given date range.
     */
    @Transactional(readOnly = true)
    public boolean isVehicleAvailable(Long id, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Checking availability for vehicle with ID {} from {} to {}", id, startDate, endDate);
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Availability check failed: Vehicle not found with ID: {}", id);
                    return new ResourceNotFoundException("Vehicle not found with ID: " + id, "Vehicle");
                });

        if (startDate.isBefore(LocalDateTime.now())) {
            log.warn("Availability check failed: Start date {} is in the past", startDate);
            throw new IllegalArgumentException("Start date cannot be in the past.");
        }

        if (startDate.isAfter(endDate)) {
            log.warn("Availability check failed: Start date {} is after end date {}", startDate, endDate);
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }

        if (!vehicle.getStatus().equals(VehicleStatus.AVAILABLE)) {
            log.warn("Vehicle with ID {} is not available due to status: {}", id, vehicle.getStatus());
            throw new VehicleNotAvailableException("Vehicle is not available due to status: " + vehicle.getStatus().name());
        }

        boolean hasConflictingReservations = vehicle.getReservations().stream()
                .anyMatch(reservation ->
                        !(endDate.isBefore(reservation.getStartDate()) || startDate.isAfter(reservation.getEndDate()))
                );

        if (hasConflictingReservations) {
            log.warn("Vehicle with ID {} is not available due to conflicting reservations", id);
            throw new VehicleNotAvailableException("Vehicle is not available due to existing reservations.");
        }

        log.info("Vehicle with ID {} is available from {} to {}", id, startDate, endDate);
        return true;
    }

    /**
     * Helper method to set vehicle relationships with proper validation.
     */
    private void setVehicleRelations(Vehicle vehicle, Long categoryId, Long brandId, Long modelId) {
        if (categoryId != null) {
            VehicleCategory category = vehicleCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new VehicleCategoryNotFoundException(categoryId));
            vehicle.setCategory(category);
        }
        
        if (brandId != null) {
            VehicleBrand brand = vehicleBrandRepository.findById(brandId)
                    .orElseThrow(() -> new VehicleBrandNotFoundException(brandId));
            vehicle.setBrand(brand);
        }
        
        if (modelId != null) {
            VehicleModel model = vehicleModelRepository.findById(modelId)
                    .orElseThrow(() -> new VehicleModelNotFoundException(modelId));
            vehicle.setModel(model);
        }
    }

    public Long countActiveVehicles() {
        return vehicleRepository.countByStatus(VehicleStatus.AVAILABLE);
    }
}

