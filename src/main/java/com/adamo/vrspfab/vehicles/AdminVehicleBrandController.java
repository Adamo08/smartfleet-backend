package com.adamo.vrspfab.vehicles;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/vehicle-brands")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Vehicle Brands", description = "APIs for administrators to manage vehicle brands")
public class AdminVehicleBrandController {
    
    private final VehicleBrandService brandService;
    
    @GetMapping
    @Operation(summary = "Get all vehicle brands with pagination",
               description = "Retrieves a paginated list of all vehicle brands. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved brands"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    public ResponseEntity<Page<VehicleBrandDto>> getAllBrands(Pageable pageable) {
        log.info("Admin requested all vehicle brands with pagination. Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<VehicleBrandDto> brands = brandService.getAllBrands(pageable);
        return ResponseEntity.ok(brands);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle brand by ID",
               description = "Retrieves a single vehicle brand by its ID. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved brand"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "404", description = "Brand not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    public ResponseEntity<VehicleBrandDto> getBrandById(@PathVariable Long id) {
        log.info("Admin requested vehicle brand with ID: {}", id);
        VehicleBrandDto brand = brandService.getBrandById(id);
        return ResponseEntity.ok(brand);
    }
    
    @PostMapping
    @Operation(summary = "Create a new vehicle brand",
               description = "Creates a new vehicle brand. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "201", description = "Brand created successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid brand data"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "409", description = "Brand name already exists"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<VehicleBrandDto> createBrand(@Valid @RequestBody VehicleBrandDto brandDto) {
        log.info("Admin creating new vehicle brand: {}", brandDto.getName());
        VehicleBrandDto createdBrand = brandService.createBrand(brandDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBrand);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle brand",
               description = "Updates an existing vehicle brand. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Brand updated successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid brand data"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "404", description = "Brand not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    public ResponseEntity<VehicleBrandDto> updateBrand(@PathVariable Long id, @Valid @RequestBody VehicleBrandDto brandDto) {
        log.info("Admin updating vehicle brand with ID: {}", id);
        VehicleBrandDto updatedBrand = brandService.updateBrand(id, brandDto);
        return ResponseEntity.ok(updatedBrand);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete vehicle brand",
               description = "Deletes a vehicle brand by its ID. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "204", description = "Brand deleted successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "404", description = "Brand not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteBrand(@PathVariable Long id) {
        log.info("Admin deleting vehicle brand with ID: {}", id);
        brandService.deleteBrand(id);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/toggle-status")
    @Operation(summary = "Toggle brand status",
               description = "Toggles the active status of a vehicle brand. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Brand status toggled successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "404", description = "Brand not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    public ResponseEntity<Void> toggleBrandStatus(@PathVariable Long id) {
        log.info("Admin toggling status for vehicle brand with ID: {}", id);
        brandService.toggleBrandStatus(id);
        return ResponseEntity.ok().build();
    }
}
