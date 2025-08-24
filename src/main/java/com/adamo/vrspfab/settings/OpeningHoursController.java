package com.adamo.vrspfab.settings;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.List;

@RestController
@RequestMapping("/admin/settings/opening-hours")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Opening Hours", description = "APIs for administrators to manage business opening hours")
public class OpeningHoursController {

    private final OpeningHoursService openingHoursService;

    @GetMapping
    @Operation(summary = "Get all opening hours",
               description = "Retrieves all business opening hours. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved opening hours"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    public ResponseEntity<List<OpeningHoursDto>> getAllOpeningHours() {
        log.info("Admin requested all opening hours");
        List<OpeningHoursDto> hours = openingHoursService.getAllOpeningHours();
        return ResponseEntity.ok(hours);
    }

    @GetMapping("/day/{dayOfWeek}")
    @Operation(summary = "Get opening hours by day",
               description = "Retrieves opening hours for a specific day. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved opening hours"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "404", description = "Opening hours not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    public ResponseEntity<OpeningHoursDto> getOpeningHoursByDay(@PathVariable DayOfWeek dayOfWeek) {
        log.info("Admin requested opening hours for day: {}", dayOfWeek);
        OpeningHoursDto hours = openingHoursService.getOpeningHoursByDay(dayOfWeek);
        return ResponseEntity.ok(hours);
    }

    @PostMapping
    @Operation(summary = "Create opening hours",
               description = "Creates new opening hours for a day. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "201", description = "Opening hours created successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid opening hours data"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "409", description = "Opening hours already exist for this day"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<OpeningHoursDto> createOpeningHours(@Valid @RequestBody OpeningHoursDto dto) {
        log.info("Admin creating opening hours for day: {}", dto.getDayOfWeek());
        OpeningHoursDto createdHours = openingHoursService.createOpeningHours(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdHours);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update opening hours",
               description = "Updates existing opening hours. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Opening hours updated successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid opening hours data"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "404", description = "Opening hours not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    public ResponseEntity<OpeningHoursDto> updateOpeningHours(@PathVariable Long id, @Valid @RequestBody OpeningHoursDto dto) {
        log.info("Admin updating opening hours with ID: {}", id);
        OpeningHoursDto updatedHours = openingHoursService.updateOpeningHours(id, dto);
        return ResponseEntity.ok(updatedHours);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete opening hours",
               description = "Deletes opening hours by ID. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "204", description = "Opening hours deleted successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "404", description = "Opening hours not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteOpeningHours(@PathVariable Long id) {
        log.info("Admin deleting opening hours with ID: {}", id);
        openingHoursService.deleteOpeningHours(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/initialize")
    @Operation(summary = "Initialize default opening hours",
               description = "Initializes default opening hours for all days. Requires admin privileges.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Default opening hours initialized successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    public ResponseEntity<Void> initializeDefaultOpeningHours() {
        log.info("Admin initializing default opening hours");
        openingHoursService.initializeDefaultOpeningHours();
        return ResponseEntity.ok().build();
    }
}
