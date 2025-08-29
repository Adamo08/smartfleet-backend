package com.adamo.vrspfab.vehicles;

import com.adamo.vrspfab.vehicles.dto.CreateVehicleModelDto;
import com.adamo.vrspfab.vehicles.dto.UpdateVehicleModelDto;
import com.adamo.vrspfab.vehicles.dto.VehicleModelResponseDto;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/vehicle-models")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Vehicle Models", description = "APIs for administrators to manage vehicle models")
@PreAuthorize("hasRole('ADMIN')")
public class AdminVehicleModelController {

    private final VehicleModelService modelService;

    @GetMapping
    @Operation(summary = "Get all vehicle models with pagination",
               description = "Retrieves a paginated list of all vehicle models. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved models"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    public ResponseEntity<Page<VehicleModelResponseDto>> getAllModels(Pageable pageable) {
        log.info("Admin requested all vehicle models with pagination. Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<VehicleModelResponseDto> models = modelService.getAllModels(pageable);
        return ResponseEntity.ok(models);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle model by ID",
               description = "Retrieves a single vehicle model by its ID. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved model"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "404", description = "Model not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    public ResponseEntity<VehicleModelResponseDto> getModelById(@PathVariable Long id) {
        log.info("Admin requested vehicle model with ID: {}", id);
        VehicleModelResponseDto model = modelService.getModelById(id);
        return ResponseEntity.ok(model);
    }

    @PostMapping
    @Operation(summary = "Create a new vehicle model",
               description = "Creates a new vehicle model. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "201", description = "Model created successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid model data"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "409", description = "Model name already exists for this brand"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<VehicleModelResponseDto> createModel(@Valid @RequestBody CreateVehicleModelDto createDto) {
        log.info("Admin creating new vehicle model: {}", createDto.getName());
        VehicleModelResponseDto createdModel = modelService.createModel(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdModel);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle model",
               description = "Updates an existing vehicle model. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Model updated successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid model data"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "404", description = "Model not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    public ResponseEntity<VehicleModelResponseDto> updateModel(@PathVariable Long id, @Valid @RequestBody UpdateVehicleModelDto updateDto) {
        log.info("Admin updating vehicle model with ID: {}", id);
        VehicleModelResponseDto updatedModel = modelService.updateModel(id, updateDto);
        return ResponseEntity.ok(updatedModel);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete vehicle model",
               description = "Deletes a vehicle model by its ID. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "204", description = "Model deleted successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "404", description = "Model not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteModel(@PathVariable Long id) {
        log.info("Admin deleting vehicle model with ID: {}", id);
        modelService.deleteModel(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-status")
    @Operation(summary = "Toggle model status",
               description = "Toggles the active status of a vehicle model. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Model status toggled successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "404", description = "Model not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    public ResponseEntity<VehicleModelResponseDto> toggleModelStatus(@PathVariable Long id) {
        log.info("Admin toggling status for vehicle model with ID: {}", id);
        VehicleModelResponseDto updatedModel = modelService.toggleModelStatus(id);
        return ResponseEntity.ok(updatedModel);
    }
}