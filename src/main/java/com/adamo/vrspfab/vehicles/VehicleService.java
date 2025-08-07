package com.adamo.vrspfab.vehicles;

import com.adamo.vrspfab.common.ResourceNotFoundException;
import com.adamo.vrspfab.reservations.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing {@link Vehicle} entities.
 * It contains business logic for creating, reading, updating, and deleting vehicles,
 * as well as searching and updating their status and mileage.
 */
@Slf4j
@AllArgsConstructor
@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;

    /**
     * Creates a new vehicle.
     *
     * @param vehicleDto The DTO containing the vehicle data.
     * @return The DTO of the newly created vehicle.
     * @throws DuplicateLicensePlateException if a vehicle with the same license plate already exists.
     * @throws InvalidVehicleDataException if the vehicle data is invalid (e.g., future year).
     */
    @Transactional
    public VehicleDto createVehicle(@Valid VehicleDto vehicleDto) {
        log.info("Attempting to create a new vehicle with license plate: {}", vehicleDto.getLicensePlate());
        if (vehicleRepository.findByLicensePlate(vehicleDto.getLicensePlate()).isPresent()) {
            log.warn("Vehicle creation failed: A vehicle with license plate {} already exists.", vehicleDto.getLicensePlate());
            throw new DuplicateLicensePlateException("A vehicle with license plate '" + vehicleDto.getLicensePlate() + "' already exists.");
        }
        if (vehicleDto.getYear() > LocalDate.now().getYear()) {
            log.warn("Vehicle creation failed: Year {} is in the future.", vehicleDto.getYear());
            throw new InvalidVehicleDataException("Vehicle year cannot be in the future.");
        }
        Vehicle vehicle = vehicleMapper.toEntity(vehicleDto);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle created successfully with ID: {}", savedVehicle.getId());
        return vehicleMapper.toDto(savedVehicle);
    }

    /**
     * Creates multiple vehicles in bulk.
     *
     * @param vehicleDtos The list of DTOs containing vehicle data.
     * @return The list of DTOs of the created vehicles.
     * @throws DuplicateLicensePlateException if any vehicle has a duplicate license plate.
     * @throws InvalidVehicleDataException if any vehicle data is invalid.
     */
    @Transactional
    public List<VehicleDto> createVehiclesBulk(@Valid List<VehicleDto> vehicleDtos) {
        log.info("Attempting to create {} vehicles in bulk", vehicleDtos.size());
        vehicleDtos.forEach(dto -> {
            if (vehicleRepository.findByLicensePlate(dto.getLicensePlate()).isPresent()) {
                log.warn("Bulk vehicle creation failed: A vehicle with license plate {} already exists.", dto.getLicensePlate());
                throw new DuplicateLicensePlateException("A vehicle with license plate '" + dto.getLicensePlate() + "' already exists.");
            }
            if (dto.getYear() > LocalDate.now().getYear()) {
                log.warn("Bulk vehicle creation failed: Year {} is in the future for license plate {}.", dto.getYear(), dto.getLicensePlate());
                throw new InvalidVehicleDataException("Vehicle year cannot be in the future for license plate: " + dto.getLicensePlate());
            }
        });
        List<Vehicle> vehicles = vehicleDtos.stream()
                .map(vehicleMapper::toEntity)
                .collect(Collectors.toList());
        List<Vehicle> savedVehicles = vehicleRepository.saveAll(vehicles);
        log.info("Successfully created {} vehicles in bulk", savedVehicles.size());
        return savedVehicles.stream()
                .map(vehicleMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a vehicle by its ID.
     *
     * @param id The ID of the vehicle.
     * @return The DTO of the found vehicle.
     * @throws ResourceNotFoundException if the vehicle is not found.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "vehicles", key = "#id")
    public VehicleDto getVehicleById(Long id) {
        log.debug("Fetching vehicle with ID: {}", id);
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Vehicle not found with ID: {}", id);
                    return new ResourceNotFoundException("Vehicle not found with ID: " + id, "Vehicle");
                });
        return vehicleMapper.toDto(vehicle);
    }

    /**
     * Retrieves all vehicles with pagination.
     *
     * @param page The page number (0-indexed).
     * @param size The number of items per page.
     * @param sortBy The field to sort by.
     * @param sortDirection The sort direction (ASC or DESC).
     * @return A page of Vehicle DTOs.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "vehicles", key = "#page + '-' + #size + '-' + #sortBy + '-' + #sortDirection")
    public Page<VehicleDto> getAllVehicles(
            int page, int size, String sortBy, String sortDirection
    ) {
        log.debug("Fetching all vehicles: page={}, size={}, sortBy={}, sortDirection={}",
                page, size, sortBy, sortDirection);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Vehicle> vehicles = vehicleRepository.findAll(pageable);
        return vehicles.map(vehicleMapper::toDto);
    }

    /**
     * Searches for vehicles based on multiple optional criteria.
     *
     * @param page The page number (0-indexed).
     * @param size The number of items per page.
     * @param sortBy The field to sort by.
     * @param sortDirection The sort direction (ASC or DESC).
     * @param status Optional filter for vehicle status.
     * @param type Optional filter for vehicle type.
     * @param brand Optional filter for brand name.
     * @param model Optional filter for model name.
     * @param minPrice Optional minimum price filter.
     * @param maxPrice Optional maximum price filter.
     * @return A page of matching Vehicle DTOs.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "vehicles", key = "#page + '-' + #size + '-' + #sortBy + '-' + #sortDirection + '-' + #status.orElse(null) + '-' + #type.orElse(null) + '-' + #brand.orElse(null) + '-' + #model.orElse(null) + '-' + #minPrice.orElse(null) + '-' + #maxPrice.orElse(null)")
    public Page<VehicleDto> searchVehicles(
            int page, int size, String sortBy, String sortDirection,
            Optional<VehicleStatus> status, Optional<VehicleType> type,
            Optional<String> brand, Optional<String> model,
            Optional<Double> minPrice, Optional<Double> maxPrice
    ) {
        log.debug("Searching vehicles: page={}, size={}, sortBy={}, sortDirection={}, status={}, type={}, brand={}, model={}, minPrice={}, maxPrice={}",
                page, size, sortBy, sortDirection, status, type, brand, model, minPrice, maxPrice);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Vehicle> vehicles = vehicleRepository.searchVehicles(
                status.orElse(null), type.orElse(null),
                brand.orElse(null), model.orElse(null),
                minPrice.orElse(null), maxPrice.orElse(null), pageable
        );
        return vehicles.map(vehicleMapper::toDto);
    }

    /**
     * Retrieves vehicles within a specified year range.
     *
     * @param page The page number (0-indexed).
     * @param size The number of items per page.
     * @param sortBy The field to sort by.
     * @param sortDirection The sort direction (ASC or DESC).
     * @param startYear The start year of the range.
     * @param endYear The end year of the range.
     * @return A page of matching Vehicle DTOs.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "vehicles", key = "#page + '-' + #size + '-' + #sortBy + '-' + #sortDirection + '-' + #startYear + '-' + #endYear")
    public Page<VehicleDto> getVehiclesByYearRange(
            int page, int size, String sortBy, String sortDirection,
            int startYear, int endYear
    ) {
        log.debug("Fetching vehicles by year range: page={}, size={}, sortBy={}, sortDirection={}, startYear={}, endYear={}",
                page, size, sortBy, sortDirection, startYear, endYear);
        if (startYear > endYear) {
            log.warn("Invalid year range: startYear={} is greater than endYear={}", startYear, endYear);
            throw new IllegalArgumentException("Start year cannot be greater than end year.");
        }
        if (endYear > LocalDate.now().getYear()) {
            log.warn("Invalid year range: endYear={} is in the future.", endYear);
            throw new InvalidVehicleDataException("End year cannot be in the future.");
        }
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Vehicle> vehicles = vehicleRepository.findByYearBetween(startYear, endYear, pageable);
        return vehicles.map(vehicleMapper::toDto);
    }

    /**
     * Retrieves vehicles within a specified mileage range.
     *
     * @param page The page number (0-indexed).
     * @param size The number of items per page.
     * @param sortBy The field to sort by.
     * @param sortDirection The sort direction (ASC or DESC).
     * @param minMileage The minimum mileage.
     * @param maxMileage The maximum mileage.
     * @return A page of matching Vehicle DTOs.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "vehicles", key = "#page + '-' + #size + '-' + #sortBy + '-' + #sortDirection + '-' + #minMileage + '-' + #maxMileage")
    public Page<VehicleDto> getVehiclesByMileageRange(
            int page, int size, String sortBy, String sortDirection,
            float minMileage, float maxMileage
    ) {
        log.debug("Fetching vehicles by mileage range: page={}, size={}, sortBy={}, sortDirection={}, minMileage={}, maxMileage={}",
                page, size, sortBy, sortDirection, minMileage, maxMileage);
        if (minMileage > maxMileage) {
            log.warn("Invalid mileage range: minMileage={} is greater than maxMileage={}", minMileage, maxMileage);
            throw new IllegalArgumentException("Minimum mileage cannot be greater than maximum mileage.");
        }
        if (minMileage < 0) {
            log.warn("Invalid mileage: minMileage={} cannot be negative.", minMileage);
            throw new IllegalArgumentException("Mileage cannot be negative.");
        }
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Vehicle> vehicles = vehicleRepository.findByMileageBetween(minMileage, maxMileage, pageable);
        return vehicles.map(vehicleMapper::toDto);
    }

    /**
     * Updates an existing vehicle.
     *
     * @param id The ID of the vehicle to update.
     * @param vehicleDto The DTO containing the updated vehicle data.
     * @return The DTO of the updated vehicle.
     * @throws ResourceNotFoundException if the vehicle is not found.
     * @throws DuplicateLicensePlateException if the new license plate already exists for another vehicle.
     * @throws InvalidVehicleDataException if the vehicle data is invalid.
     */
    @Transactional
    public VehicleDto updateVehicle(Long id, @Valid VehicleDto vehicleDto) {
        log.info("Attempting to update vehicle with ID: {}", id);
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Vehicle update failed: Vehicle not found with ID: {}", id);
                    return new ResourceNotFoundException("Vehicle not found with ID: " + id, "Vehicle");
                });

        if (!vehicle.getLicensePlate().equals(vehicleDto.getLicensePlate()) &&
                vehicleRepository.findByLicensePlate(vehicleDto.getLicensePlate()).isPresent()) {
            log.warn("Vehicle update failed: A vehicle with license plate {} already exists.", vehicleDto.getLicensePlate());
            throw new DuplicateLicensePlateException("A vehicle with license plate '" + vehicleDto.getLicensePlate() + "' already exists.");
        }
        if (vehicleDto.getYear() > LocalDate.now().getYear()) {
            log.warn("Vehicle update failed: Year {} is in the future.", vehicleDto.getYear());
            throw new InvalidVehicleDataException("Vehicle year cannot be in the future.");
        }
        vehicleMapper.updateVehicleFromDto(vehicleDto, vehicle);
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle with ID {} updated successfully.", id);
        return vehicleMapper.toDto(updatedVehicle);
    }

    /**
     * Updates the status of a vehicle.
     *
     * @param id The ID of the vehicle.
     * @param newStatus The new status to set.
     * @return The DTO of the updated vehicle.
     * @throws ResourceNotFoundException if the vehicle is not found.
     * @throws InvalidVehicleStatusUpdateException if the status change is not allowed.
     * @throws VehicleDecommissionedException if the vehicle is decommissioned.
     */
    @Transactional
    public VehicleDto updateVehicleStatus(Long id, VehicleStatus newStatus) {
        log.info("Attempting to update status of vehicle with ID {} to {}", id, newStatus);
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
        log.info("Status of vehicle with ID {} updated to {} successfully.", id, newStatus);
        return vehicleMapper.toDto(updatedVehicle);
    }

    /**
     * Updates the mileage of a vehicle.
     *
     * @param id The ID of the vehicle.
     * @param newMileage The new mileage value.
     * @return The DTO of the updated vehicle.
     * @throws ResourceNotFoundException if the vehicle is not found.
     * @throws IllegalArgumentException if the new mileage is less than the current mileage.
     * @throws VehicleDecommissionedException if the vehicle is decommissioned.
     * @throws InvalidVehicleDataException if the mileage increase is implausible.
     */
    @Transactional
    public VehicleDto updateVehicleMileage(Long id, Float newMileage) {
        log.info("Attempting to update mileage of vehicle with ID {} to {}", id, newMileage);
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
        log.info("Mileage of vehicle with ID {} updated to {} successfully.", id, newMileage);
        return vehicleMapper.toDto(updatedVehicle);
    }

    /**
     * Deletes a vehicle by its ID.
     *
     * @param id The ID of the vehicle to delete.
     * @throws ResourceNotFoundException if the vehicle is not found.
     * @throws VehicleDecommissionedException if the vehicle is decommissioned.
     */
    @Transactional
    public void deleteVehicle(Long id) {
        log.info("Attempting to delete vehicle with ID: {}", id);
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
        log.info("Vehicle with ID {} deleted successfully.", id);
    }

    /**
     * Checks if a vehicle is available for a given date range.
     *
     * @param id The ID of the vehicle.
     * @param startDate The start date of the availability check.
     * @param endDate The end date of the availability check.
     * @return True if the vehicle is available, false otherwise.
     * @throws ResourceNotFoundException if the vehicle is not found.
     * @throws VehicleNotAvailableException if the vehicle is not available due to status or reservations.
     * @throws IllegalArgumentException if the date range is invalid.
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
            log.warn("Availability check failed: Start date {} is in the past.", startDate);
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
            log.warn("Vehicle with ID {} is not available due to conflicting reservations.", id);
            throw new VehicleNotAvailableException("Vehicle is not available due to existing reservations.");
        }

        boolean hasAvailableSlots = vehicle.getSlots().stream()
                .anyMatch(slot ->
                        slot.isAvailable() &&
                                !startDate.isBefore(slot.getStartTime()) &&
                                !endDate.isAfter(slot.getEndTime())
                );

        if (!hasAvailableSlots) {
            log.warn("Vehicle with ID {} has no available slots for the requested period.", id);
            throw new VehicleNotAvailableException("No available slots for the requested period.");
        }

        log.info("Vehicle with ID {} is available from {} to {}.", id, startDate, endDate);
        return true;
    }

    /**
     * Retrieves the reservation count for a vehicle.
     *
     * @param vehicle The vehicle entity.
     * @return The number of reservations for the vehicle.
     */
    public int getReservationCount(Vehicle vehicle) {
        return vehicle.getReservations().size();
    }

//    /**
//     * Retrieves the reservations for a vehicle with pagination.
//     *
//     * @param id The ID of the vehicle.
//     * @param page The page number (0-indexed).
//     * @param size The number of items per page.
//     * @param sortBy The field to sort by.
//     * @param sortDirection The sort direction (ASC or DESC).
//     * @return A page of Reservation entities.
//     * @throws ResourceNotFoundException if the vehicle is not found.
//     */
//    @Transactional(readOnly = true)
//    @Cacheable(value = "vehicleReservations", key = "#id + '-' + #page + '-' + #size + '-' + #sortBy + '-' + #sortDirection")
//    public Page<ReservationDto> getVehicleReservations(
//            Long id, int page, int size, String sortBy, String sortDirection
//    ) {
//        log.debug("Fetching reservations for vehicle with ID: {}: page={}, size={}, sortBy={}, sortDirection={}",
//                id, page, size, sortBy, sortDirection);
//        Vehicle vehicle = vehicleRepository.findById(id)
//                .orElseThrow(() -> {
//                    log.warn("Reservation fetch failed: Vehicle not found with ID: {}", id);
//                    return new ResourceNotFoundException("Vehicle not found with ID: " + id, "Vehicle");
//                });
//        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
//        Pageable pageable = PageRequest.of(page, size, sort);
//
//
//        // Map reservations to DTOs
//        List<ReservationDto> reservationDtos = vehicle.getReservations().stream()
//                .map(reservationMapper::toDto)
//                .toList();
//
//        return new PageImpl<>(reservationDtos, pageable, reservationDtos.size());
//    }

    /**
     * Retrieves the reservations for a vehicle with pagination.
     *
     * @param id The ID of the vehicle.
     * @param page The page number (0-indexed).
     * @param size The number of items per page.
     * @param sortBy The field to sort by.
     * @param sortDirection The sort direction (ASC or DESC).
     * @return A page of ReservationInfoForVehicle DTOs.
     * @throws ResourceNotFoundException if the vehicle is not found.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "vehicleReservations", key = "#id + '-' + #page + '-' + #size + '-' + #sortBy + '-' + #sortDirection")
    public Page<ReservationInfoForVehicleDto> getVehicleReservations(
            Long id, int page, int size, String sortBy, String sortDirection
    ) {
        log.debug("Fetching reservations for vehicle with ID: {}: page={}, size={}, sortBy={}, sortDirection={}",
                id, page, size, sortBy, sortDirection);
        if (!vehicleRepository.existsById(id)) {
            log.warn("Reservation fetch failed: Vehicle not found with ID: {}", id);
            throw new ResourceNotFoundException("Vehicle not found with ID: " + id, "Vehicle");
        }
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Reservation> reservations = reservationRepository.findByVehicleId(id, pageable);
        return reservations.map(reservationMapper::toReservationInfoDto);
    }
}