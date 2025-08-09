package com.adamo.vrspfab.vehicles;

import com.adamo.vrspfab.reservations.Reservation;
import com.adamo.vrspfab.reservations.ReservationDto;
import com.adamo.vrspfab.reservations.ReservationInfoForVehicleDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

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

    @GetMapping("/{id}")
    public ResponseEntity<VehicleDto> getVehicleById(@PathVariable Long id) {
        log.info("Received request to get vehicle with ID: {}", id);
        return ResponseEntity.ok(vehicleService.getVehicleById(id));
    }

    @GetMapping
    public ResponseEntity<Page<VehicleDto>> getAllVehicles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        log.info("Received request to get all vehicles: page={}, size={}, sortBy={}, sortDirection={}",
                page, size, sortBy, sortDirection);
        Page<VehicleDto> vehicles = vehicleService.getAllVehicles(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<VehicleDto>> searchVehicles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) Optional<VehicleStatus> status,
            @RequestParam(required = false) Optional<VehicleType> type,
            @RequestParam(required = false) Optional<String> brand,
            @RequestParam(required = false) Optional<String> model,
            @RequestParam(required = false) Optional<Double> minPrice,
            @RequestParam(required = false) Optional<Double> maxPrice
    ) {
        log.info("Received request to search vehicles: status={}, type={}, brand={}, model={}, minPrice={}, maxPrice={}",
                status, type, brand, model, minPrice, maxPrice);
        Page<VehicleDto> vehicles = vehicleService.searchVehicles(
                page, size, sortBy, sortDirection,
                status, type,
                brand.map(Encode::forHtml),
                model.map(Encode::forHtml),
                minPrice, maxPrice
        );
        return ResponseEntity.ok(vehicles);
    }

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

    @PatchMapping("/{id}/status")
    public ResponseEntity<VehicleDto> updateVehicleStatus(
            @PathVariable Long id,
            @Valid @RequestBody VehicleStatusUpdateDto statusUpdateDto
    ) {
        log.info("Received request to update vehicle with ID {} status to {}", id, statusUpdateDto.getStatus());
        VehicleDto updatedVehicle = vehicleService.updateVehicleStatus(id, statusUpdateDto.getStatus());
        return ResponseEntity.ok(updatedVehicle);
    }

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

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVehicle(@PathVariable Long id) {
        log.info("Received request to delete vehicle with ID: {}", id);
        vehicleService.deleteVehicle(id);
    }
}