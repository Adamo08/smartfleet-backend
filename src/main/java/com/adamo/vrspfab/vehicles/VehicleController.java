package com.adamo.vrspfab.vehicles;

import com.adamo.vrspfab.vehicles.dto.AvailabilityResponseDto;
import com.adamo.vrspfab.vehicles.dto.VehicleBrandResponseDto;
import com.adamo.vrspfab.vehicles.dto.VehicleCategoryResponseDto;
import com.adamo.vrspfab.vehicles.dto.VehicleModelResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vehicle Management", description = "Customer-facing APIs for vehicle browsing and booking")
public class VehicleController {

    private final VehicleService vehicleService;
    private final VehicleBrandService brandService;
    private final VehicleCategoryService categoryService;
    private final VehicleModelService modelService;

    @Operation(summary = "Get all available vehicles with filters",
               description = "Retrieves paginated list of vehicles that are available for booking. " +
                           "Automatically excludes vehicles with inactive brands, categories, or models.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved vehicles"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping
    public ResponseEntity<Page<VehicleDto>> getAllVehicles(
            Pageable pageable,
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
            @RequestParam(required = false) Double maxMileage) {

        VehicleFilter filter = VehicleFilter.builder()
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

        // Use customer specification that filters by active status
        Specification<Vehicle> specification = new VehicleSpecification(filter);
        Page<VehicleDto> vehicles = vehicleService.getAllVehicles(specification, pageable);
        
        return ResponseEntity.ok(vehicles);
    }

    @Operation(summary = "Get vehicle by ID",
               description = "Retrieves detailed information about a specific vehicle by its ID.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Vehicle retrieved successfully"),
                       @ApiResponse(responseCode = "404", description = "Vehicle not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/{id}")
    public ResponseEntity<VehicleDto> getVehicleById(@PathVariable Long id) {
        VehicleDto vehicle = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(vehicle);
    }

    @Operation(summary = "Get active vehicle brands",
               description = "Retrieves all active vehicle brands for customer dropdowns and filters. " +
                           "Inactive brands are excluded to prevent booking unavailable vehicles.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved active brands"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/brands/active")
    public ResponseEntity<List<VehicleBrandResponseDto>> getActiveBrands() {
        List<VehicleBrandResponseDto> activeBrands = brandService.getActiveBrands();
        return ResponseEntity.ok(activeBrands);
    }

    @Operation(summary = "Get active vehicle categories",
               description = "Retrieves all active vehicle categories for customer dropdowns and filters. " +
                           "Inactive categories are excluded to prevent booking unavailable vehicles.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved active categories"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/categories/active")
    public ResponseEntity<List<VehicleCategoryResponseDto>> getActiveCategories() {
        List<VehicleCategoryResponseDto> activeCategories = categoryService.getActiveCategories();
        return ResponseEntity.ok(activeCategories);
    }

    @Operation(summary = "Get active vehicle models",
               description = "Retrieves all active vehicle models for customer dropdowns and filters. " +
                           "Inactive models are excluded to prevent booking unavailable vehicles.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved active models"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/models/active")
    public ResponseEntity<List<VehicleModelResponseDto>> getActiveModels() {
        List<VehicleModelResponseDto> activeModels = modelService.getActiveModels();
        return ResponseEntity.ok(activeModels);
    }

    @Operation(summary = "Get active models by brand",
               description = "Retrieves active vehicle models for a specific brand for customer dropdowns. " +
                           "Only returns models that are active AND belong to an active brand.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved active models for brand"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/models/active/brand/{brandId}")
    public ResponseEntity<List<VehicleModelResponseDto>> getActiveModelsByBrand(@PathVariable Long brandId) {
        List<VehicleModelResponseDto> activeModels = modelService.getActiveModelsByBrandId(brandId);
        return ResponseEntity.ok(activeModels);
    }

    @Operation(summary = "Check vehicle availability",
               description = "Checks if a vehicle is available for booking during the specified date range. " +
                           "Considers vehicle status, existing reservations, and brand/category/model active status.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Availability check completed"),
                       @ApiResponse(responseCode = "404", description = "Vehicle not found"),
                       @ApiResponse(responseCode = "400", description = "Invalid date range"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/{id}/availability")
    public ResponseEntity<AvailabilityResponseDto> checkVehicleAvailability(
            @PathVariable Long id,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        
        boolean isAvailable = vehicleService.isVehicleAvailableForBooking(id, start, end);
        
        AvailabilityResponseDto response = AvailabilityResponseDto.builder()
                .vehicleId(id)
                .startDate(start)
                .endDate(end)
                .isAvailable(isAvailable)
                .build();
                
        return ResponseEntity.ok(response);
    }
}