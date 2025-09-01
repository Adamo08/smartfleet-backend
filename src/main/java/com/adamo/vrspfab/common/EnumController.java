package com.adamo.vrspfab.common;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/enums")
@RequiredArgsConstructor
@Tag(name = "Enums", description = "APIs for retrieving system enum values")
public class EnumController {

    private final EnumService enumService;

    @GetMapping("/fuel-types")
    @Operation(summary = "Get fuel types", description = "Retrieves all available fuel types")
    public ResponseEntity<List<EnumService.EnumValue>> getFuelTypes() {
        return ResponseEntity.ok(enumService.getFuelTypes());
    }

    @GetMapping("/vehicle-statuses")
    @Operation(summary = "Get vehicle statuses", description = "Retrieves all available vehicle statuses")
    public ResponseEntity<List<EnumService.EnumValue>> getVehicleStatuses() {
        return ResponseEntity.ok(enumService.getVehicleStatuses());
    }

    @GetMapping("/reservation-statuses")
    @Operation(summary = "Get reservation statuses", description = "Retrieves all available reservation statuses")
    public ResponseEntity<List<EnumService.EnumValue>> getReservationStatuses() {
        return ResponseEntity.ok(enumService.getReservationStatuses());
    }

    @GetMapping("/user-roles")
    @Operation(summary = "Get user roles", description = "Retrieves all available user roles")
    public ResponseEntity<List<EnumService.EnumValue>> getUserRoles() {
        return ResponseEntity.ok(enumService.getUserRoles());
    }
}
