package com.adamo.vrspfab.vehicles;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/vehicles")
@Tag(name = "Public Vehicle Catalog", description = "Public APIs for browsing and viewing vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    @Operation(summary = "Get vehicle by ID",
               description = "Retrieves a single vehicle by its ID for public viewing.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved vehicle"),
                       @ApiResponse(responseCode = "404", description = "Vehicle not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/{id}")
    public ResponseEntity<VehicleDto> getVehicleById(@PathVariable Long id) {
        log.info("Public request to get vehicle with ID: {}", id);
        return ResponseEntity.ok(vehicleService.getVehicleById(id));
    }

    @Operation(summary = "Get all vehicles",
               description = "Retrieves a paginated list of all available vehicles for public browsing.",
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
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long modelId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String fuelType,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minYear,
            @RequestParam(required = false) Integer maxYear
    ) {
        log.info("Public request for all vehicles: page={}, size={}, sortBy={}, sortDirection={}",
                page, size, sortBy, sortDirection);
        
        VehicleFilter filters = VehicleFilter.builder()
                .search(search)
                .brandId(brandId)
                .modelId(modelId)
                .categoryId(categoryId)
                .fuelType(fuelType)
                .status("AVAILABLE") // Only show available vehicles to public
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .minYear(minYear)
                .maxYear(maxYear)
                .build();

        return vehicleService.getAllVehicles(page, size, sortBy, sortDirection, filters);
    }

    @Operation(summary = "Search vehicles",
               description = "Searches for vehicles based on various criteria for public browsing.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved search results"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/search")
    public Page<VehicleDto> searchVehicles(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String fuelType,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minYear,
            @RequestParam(required = false) Integer maxYear,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        log.info("Public search for vehicles with query: {}", query);
        
        VehicleFilter filters = VehicleFilter.builder()
                .search(query)
                .categoryId(categoryId)
                .brandId(brandId)
                .fuelType(fuelType)
                .status("AVAILABLE") // Only show available vehicles to public
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .minYear(minYear)
                .maxYear(maxYear)
                .build();

        return vehicleService.getAllVehicles(page, size, sortBy, sortDirection, filters);
    }

    @Operation(summary = "Check vehicle availability",
               description = "Checks if a vehicle is available for the given date range.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Availability status retrieved successfully"),
                       @ApiResponse(responseCode = "404", description = "Vehicle not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/{id}/availability")
    public ResponseEntity<Boolean> checkVehicleAvailability(
            @PathVariable Long id,
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {
        log.info("Public availability check for vehicle ID {} from {} to {}", id, startDate, endDate);
        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        boolean isAvailable = vehicleService.isVehicleAvailable(id, start, end);
        return ResponseEntity.ok(isAvailable);
    }

    @Operation(summary = "Get vehicles by year range",
               description = "Retrieves a paginated list of vehicles manufactured within a specific year range.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved vehicles"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/year/{startYear}/{endYear}")
    public Page<VehicleDto> getVehiclesByYearRange(
            @PathVariable Integer startYear,
            @PathVariable Integer endYear,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        log.info("Public request for vehicles between years {} and {} with pagination: page={}, size={}", 
                startYear, endYear, page, size);
        return vehicleService.getVehiclesByYearRange(page, size, sortBy, sortDirection, startYear, endYear);
    }

    @Operation(summary = "Get vehicles by mileage range",
               description = "Retrieves a paginated list of vehicles within a specific mileage range.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved vehicles"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/mileage/{minMileage}/{maxMileage}")
    public Page<VehicleDto> getVehiclesByMileageRange(
            @PathVariable Float minMileage,
            @PathVariable Float maxMileage,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        log.info("Public request for vehicles with mileage between {} and {} with pagination: page={}, size={}", 
                minMileage, maxMileage, page, size);
        return vehicleService.getVehiclesByMileageRange(page, size, sortBy, sortDirection, minMileage, maxMileage);
    }
}