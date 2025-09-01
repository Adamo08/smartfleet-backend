package com.adamo.vrspfab.vehicles;

import com.adamo.vrspfab.vehicles.dto.CreateVehicleDto;
import com.adamo.vrspfab.vehicles.dto.UpdateVehicleDto;
import com.adamo.vrspfab.vehicles.dto.VehicleResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/vehicles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Vehicle Management", description = "APIs for administrators to manage vehicles")
@PreAuthorize("hasRole('ADMIN')")
public class AdminVehicleController {

    private final EnhancedVehicleService vehicleService;

    @Operation(summary = "Create a new vehicle",
               description = "Creates a new vehicle in the system. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "201", description = "Vehicle created successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid vehicle data"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "409", description = "Duplicate license plate"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<VehicleResponseDto> createVehicle(@Valid @RequestBody CreateVehicleDto createDto) {
        log.info("Admin creating new vehicle with license plate: {}", createDto.getLicensePlate());
        VehicleResponseDto createdVehicle = vehicleService.createVehicle(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVehicle);
    }

    @Operation(summary = "Create multiple vehicles in bulk",
               description = "Creates multiple vehicles in a single request. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "201", description = "Vehicles created successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid vehicle data in bulk request"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "409", description = "Duplicate license plates found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<List<VehicleResponseDto>> createVehiclesBulk(@Valid @RequestBody List<CreateVehicleDto> createDtos) {
        log.info("Admin creating {} vehicles in bulk", createDtos.size());
        List<VehicleResponseDto> createdVehicles = vehicleService.createVehiclesBulk(createDtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVehicles);
    }

    @Operation(summary = "Get vehicle by ID",
               description = "Retrieves a single vehicle by its ID. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved vehicle"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "404", description = "Vehicle not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponseDto> getVehicleById(@PathVariable Long id) {
        log.info("Admin requested vehicle with ID: {}", id);
        VehicleResponseDto vehicle = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(vehicle);
    }

    @Operation(summary = "Get all vehicles with filtering",
               description = "Retrieves a paginated and filtered list of all vehicles. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved list of vehicles"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping
    public Page<VehicleResponseDto> getAllVehicles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long modelId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String fuelType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minYear,
            @RequestParam(required = false) Integer maxYear,
            @RequestParam(required = false) Double minMileage,
            @RequestParam(required = false) Double maxMileage
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        PageRequest pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        VehicleFilter filters = VehicleFilter.builder()
                .search(search)
                .brandId(brandId)
                .modelId(modelId)
                .categoryId(categoryId)
                .fuelType(fuelType)
                .status(status)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .minYear(minYear)
                .maxYear(maxYear)
                .minMileage(minMileage)
                .maxMileage(maxMileage)
                .build();

        return vehicleService.getAllVehicles(page, size, sortBy, sortDirection, filters);
    }

    @Operation(summary = "Update vehicle details",
               description = "Updates the details of an existing vehicle. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Vehicle updated successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid vehicle data"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "404", description = "Vehicle not found"),
                       @ApiResponse(responseCode = "409", description = "Duplicate license plate"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponseDto> updateVehicle(@PathVariable Long id, @Valid @RequestBody UpdateVehicleDto updateDto) {
        log.info("Admin updating vehicle with ID: {}", id);
        VehicleResponseDto updatedVehicle = vehicleService.updateVehicle(id, updateDto);
        return ResponseEntity.ok(updatedVehicle);
    }

    @Operation(summary = "Update vehicle status",
               description = "Updates the status of a specific vehicle. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Vehicle status updated successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid status provided"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "404", description = "Vehicle not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PatchMapping("/{id}/status")
    public ResponseEntity<VehicleResponseDto> updateVehicleStatus(
            @PathVariable Long id,
            @Valid @RequestBody VehicleStatusUpdateDto statusUpdateDto
    ) {
        log.info("Admin updating vehicle with ID {} status to {}", id, statusUpdateDto.getStatus());
        VehicleResponseDto updatedVehicle = vehicleService.updateVehicleStatus(id, statusUpdateDto.getStatus());
        return ResponseEntity.ok(updatedVehicle);
    }

    @Operation(summary = "Update vehicle mileage",
               description = "Updates the mileage of a specific vehicle. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Vehicle mileage updated successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid mileage provided"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "404", description = "Vehicle not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PatchMapping("/{id}/mileage")
    public ResponseEntity<VehicleResponseDto> updateVehicleMileage(
            @PathVariable Long id,
            @RequestBody Map<String, Float> payload
    ) {
        Float mileage = payload.get("mileage");
        if (mileage == null) {
            log.warn("Invalid request: Missing mileage in payload for vehicle ID {}", id);
            return ResponseEntity.badRequest().build();
        }
        log.info("Admin updating vehicle with ID {} mileage to {}", id, mileage);
        VehicleResponseDto updatedVehicle = vehicleService.updateVehicleMileage(id, mileage);
        return ResponseEntity.ok(updatedVehicle);
    }

    @Operation(summary = "Delete a vehicle",
               description = "Deletes a vehicle by its ID. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "204", description = "Vehicle deleted successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "404", description = "Vehicle not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        log.info("Admin deleting vehicle with ID: {}", id);
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}

