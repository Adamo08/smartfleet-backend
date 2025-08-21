package com.adamo.vrspfab.vehicles;

import com.adamo.vrspfab.reservations.Reservation;
import com.adamo.vrspfab.reservations.ReservationDto;
import com.adamo.vrspfab.reservations.ReservationInfoForVehicleDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/vehicles")
@Tag(name = "Vehicle Management", description = "APIs for managing vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    @Operation(summary = "Create a new vehicle",
               description = "Adds a new vehicle to the system.",
               responses = {
                       @ApiResponse(responseCode = "201", description = "Vehicle created successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid vehicle data"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleDto createVehicle(@Valid @RequestBody VehicleDto vehicleDto) {
        log.info("Received request to create vehicle: {}", vehicleDto);
        vehicleDto.setBrand(Encode.forHtml(vehicleDto.getBrand()));
        vehicleDto.setModel(Encode.forHtml(vehicleDto.getModel()));
        vehicleDto.setLicensePlate(Encode.forHtml(vehicleDto.getLicensePlate()));
        vehicleDto.setDescription(Encode.forHtml(vehicleDto.getDescription()));
        return vehicleService.createVehicle(vehicleDto);
    }

    @Operation(summary = "Create multiple vehicles in bulk",
               description = "Adds multiple vehicles to the system in a single request.",
               responses = {
                       @ApiResponse(responseCode = "201", description = "Vehicles created successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid vehicle data in bulk request"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<VehicleDto> createVehiclesBulk(@Valid @RequestBody List<VehicleDto> vehicleDtos) {
        log.info("Received request to create {} vehicles in bulk", vehicleDtos.size());
        vehicleDtos.forEach(dto -> {
            dto.setBrand(Encode.forHtml(dto.getBrand()));
            dto.setModel(Encode.forHtml(dto.getModel()));
            dto.setLicensePlate(Encode.forHtml(dto.getLicensePlate()));
            dto.setDescription(Encode.forHtml(dto.getDescription()));
        });
        return vehicleService.createVehiclesBulk(vehicleDtos);
    }

    @Operation(summary = "Get vehicle by ID",
               description = "Retrieves a single vehicle by its ID.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved vehicle"),
                       @ApiResponse(responseCode = "404", description = "Vehicle not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/{id}")
    public ResponseEntity<VehicleDto> getVehicleById(@PathVariable Long id) {
        log.info("Received request to get vehicle with ID: {}", id);
        return ResponseEntity.ok(vehicleService.getVehicleById(id));
    }

    @Operation(summary = "Get all vehicles",
               description = "Retrieves a paginated and filtered list of all vehicles.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved list of vehicles"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping
    public Page<VehicleDto> getAllVehicles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String vehicleType,
            @RequestParam(required = false) String fuelType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        PageRequest pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return vehicleService.getAllVehicles(page, size, sortBy, sortDirection, brand, model, vehicleType, fuelType, status, minPrice, maxPrice);
    }

    @Operation(summary = "Get vehicles by year range",
               description = "Retrieves a paginated list of vehicles manufactured within a specified year range.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved vehicles by year range"),
                       @ApiResponse(responseCode = "400", description = "Invalid year range"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/year/{startYear}/{endYear}")
    public ResponseEntity<Page<VehicleDto>> getVehiclesByYearRange(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @PathVariable int startYear,
            @PathVariable int endYear
    ) {
        log.info("Received request to get vehicles by year range: {} to {}", startYear, endYear);
        Page<VehicleDto> vehicles = vehicleService.getVehiclesByYearRange(page, size, sortBy, sortDirection, startYear, endYear);
        return ResponseEntity.ok(vehicles);
    }

    @Operation(summary = "Get vehicles by mileage range",
               description = "Retrieves a paginated list of vehicles within a specified mileage range.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved vehicles by mileage range"),
                       @ApiResponse(responseCode = "400", description = "Invalid mileage range"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/mileage/{minMileage}/{maxMileage}")
    public ResponseEntity<Page<VehicleDto>> getVehiclesByMileageRange(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @PathVariable float minMileage,
            @PathVariable float maxMileage
    ) {
        log.info("Received request to get vehicles by mileage range: {} to {}", minMileage, maxMileage);
        Page<VehicleDto> vehicles = vehicleService.getVehiclesByMileageRange(page, size, sortBy, sortDirection, minMileage, maxMileage);
        return ResponseEntity.ok(vehicles);
    }

    @Operation(summary = "Check vehicle availability",
               description = "Checks if a specific vehicle is available for a given date range.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved availability status"),
                       @ApiResponse(responseCode = "404", description = "Vehicle not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/{id}/availability")
    public ResponseEntity<Boolean> checkVehicleAvailability(
            @PathVariable Long id,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate
    ) {
        log.info("Received request to check availability for vehicle ID {} from {} to {}", id, startDate, endDate);
        boolean isAvailable = vehicleService.isVehicleAvailable(id, startDate, endDate);
        return ResponseEntity.ok(isAvailable);
    }


    @Operation(summary = "Get vehicle reservations",
               description = "Retrieves a paginated list of reservations for a specific vehicle.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved vehicle reservations"),
                       @ApiResponse(responseCode = "404", description = "Vehicle not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/{id}/reservations")
    public ResponseEntity<Page<ReservationInfoForVehicleDto>> getVehicleReservations(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        log.info("Received request to get reservations for vehicle ID {}: page={}, size={}, sortBy={}, sortDirection={}",
                id, page, size, sortBy, sortDirection);
        Page<ReservationInfoForVehicleDto> reservations = vehicleService.getVehicleReservations(id, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(reservations);
    }


    @Operation(summary = "Update vehicle details",
               description = "Updates the details of an existing vehicle.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Vehicle updated successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid vehicle data"),
                       @ApiResponse(responseCode = "404", description = "Vehicle not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PutMapping("/{id}")
    public ResponseEntity<VehicleDto> updateVehicle(@PathVariable Long id, @Valid @RequestBody VehicleDto vehicleDto) {
        log.info("Received request to update vehicle with ID: {}", id);
        vehicleDto.setBrand(Encode.forHtml(vehicleDto.getBrand()));
        vehicleDto.setModel(Encode.forHtml(vehicleDto.getModel()));
        vehicleDto.setLicensePlate(Encode.forHtml(vehicleDto.getLicensePlate()));
        vehicleDto.setDescription(Encode.forHtml(vehicleDto.getDescription()));
        VehicleDto updatedVehicle = vehicleService.updateVehicle(id, vehicleDto);
        return ResponseEntity.ok(updatedVehicle);
    }

    @Operation(summary = "Update vehicle status",
               description = "Updates the status of a specific vehicle.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Vehicle status updated successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid status provided"),
                       @ApiResponse(responseCode = "404", description = "Vehicle not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PatchMapping("/{id}/status")
    public ResponseEntity<VehicleDto> updateVehicleStatus(
            @PathVariable Long id,
            @Valid @RequestBody VehicleStatusUpdateDto statusUpdateDto
    ) {
        log.info("Received request to update vehicle with ID {} status to {}", id, statusUpdateDto.getStatus());
        VehicleDto updatedVehicle = vehicleService.updateVehicleStatus(id, statusUpdateDto.getStatus());
        return ResponseEntity.ok(updatedVehicle);
    }

    @Operation(summary = "Update vehicle mileage",
               description = "Updates the mileage of a specific vehicle.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Vehicle mileage updated successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid mileage provided"),
                       @ApiResponse(responseCode = "404", description = "Vehicle not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PatchMapping("/{id}/mileage")
    public ResponseEntity<VehicleDto> updateVehicleMileage(
            @PathVariable Long id,
            @RequestBody Map<String, Float> payload
    ) {
        Float mileage = payload.get("mileage");
        if (mileage == null) {
            log.warn("Invalid request: Missing mileage in payload for vehicle ID {}", id);
            return ResponseEntity.badRequest().build();
        }
        log.info("Received request to update vehicle with ID {} mileage to {}", id, mileage);
        VehicleDto updatedVehicle = vehicleService.updateVehicleMileage(id, mileage);
        return ResponseEntity.ok(updatedVehicle);
    }

    @Operation(summary = "Delete a vehicle",
               description = "Deletes a vehicle by its ID.",
               responses = {
                       @ApiResponse(responseCode = "204", description = "Vehicle deleted successfully"),
                       @ApiResponse(responseCode = "404", description = "Vehicle not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVehicle(@PathVariable Long id) {
        log.info("Received request to delete vehicle with ID: {}", id);
        vehicleService.deleteVehicle(id);
    }
}