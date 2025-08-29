package com.adamo.vrspfab.vehicles;

import com.adamo.vrspfab.vehicles.dto.CreateVehicleCategoryDto;
import com.adamo.vrspfab.vehicles.dto.UpdateVehicleCategoryDto;
import com.adamo.vrspfab.vehicles.dto.VehicleCategoryResponseDto;
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
@RequestMapping("/admin/vehicle-categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Vehicle Categories", description = "APIs for administrators to manage vehicle categories")
public class AdminVehicleCategoryController {
    
    private final VehicleCategoryService categoryService;
    
    @GetMapping
    @Operation(summary = "Get all vehicle categories with pagination",
               description = "Retrieves a paginated list of all vehicle categories. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved categories"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    public ResponseEntity<Page<VehicleCategoryResponseDto>> getAllCategories(Pageable pageable) {
        log.info("Admin requested all vehicle categories with pagination. Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<VehicleCategoryResponseDto> categories = categoryService.getAllCategories(pageable);
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle category by ID",
               description = "Retrieves a single vehicle category by its ID. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved category"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "404", description = "Category not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    public ResponseEntity<VehicleCategoryResponseDto> getCategoryById(@PathVariable Long id) {
        log.info("Admin requested vehicle category with ID: {}", id);
        VehicleCategoryResponseDto category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }
    
    @PostMapping
    @Operation(summary = "Create a new vehicle category",
               description = "Creates a new vehicle category. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "201", description = "Category created successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid category data"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "409", description = "Category name already exists"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<VehicleCategoryResponseDto> createCategory(@Valid @RequestBody CreateVehicleCategoryDto createDto) {
        log.info("Admin creating new vehicle category: {}", createDto.getName());
        VehicleCategoryResponseDto createdCategory = categoryService.createCategory(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle category",
               description = "Updates an existing vehicle category. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Category updated successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid category data"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "404", description = "Category not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    public ResponseEntity<VehicleCategoryResponseDto> updateCategory(@PathVariable Long id, @Valid @RequestBody UpdateVehicleCategoryDto updateDto) {
        log.info("Admin updating vehicle category with ID: {}", id);
        VehicleCategoryResponseDto updatedCategory = categoryService.updateCategory(id, updateDto);
        return ResponseEntity.ok(updatedCategory);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete vehicle category",
               description = "Deletes a vehicle category by its ID. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "404", description = "Category not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        log.info("Admin deleting vehicle category with ID: {}", id);
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/toggle-status")
    @Operation(summary = "Toggle category status",
               description = "Toggles the active status of a vehicle category. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Category status toggled successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "404", description = "Category not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    public ResponseEntity<VehicleCategoryResponseDto> toggleCategoryStatus(@PathVariable Long id) {
        log.info("Admin toggling status for vehicle category with ID: {}", id);
        VehicleCategoryResponseDto updatedCategory = categoryService.toggleCategoryStatus(id);
        return ResponseEntity.ok(updatedCategory);
    }
}
