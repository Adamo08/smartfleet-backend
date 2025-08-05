package com.adamo.vrspfab.vehicles;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@AllArgsConstructor
@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;
    private final VehicleMapper vehicleMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleDto createVehicle(@RequestBody VehicleDto vehicleDTO) {
        Vehicle vehicle = vehicleService.createVehicle(vehicleDTO);
        return vehicleMapper.toDto(vehicle);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleDto> getVehicle(@PathVariable Long id) {
        Vehicle vehicle = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(vehicleMapper.toDto(vehicle));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleDto> updateVehicle(@PathVariable Long id, @RequestBody VehicleDto vehicleDto) {
        Vehicle vehicle = vehicleService.updateVehicle(id, vehicleDto);
        return ResponseEntity.ok(vehicleMapper.toDto(vehicle));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
    }

    @GetMapping
    public ResponseEntity<List<VehicleDto>> getAllVehicles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) VehicleStatus status) {
        List<VehicleDto> vehicles = vehicleService.getAllVehicles(page, size, status);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/{id}/slots")
    public ResponseEntity<?> getVehicleSlots(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }
}