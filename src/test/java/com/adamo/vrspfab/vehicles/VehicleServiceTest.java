package com.adamo.vrspfab.vehicles;

import com.adamo.vrspfab.notifications.NotificationService;
import com.adamo.vrspfab.vehicles.exceptions.DuplicateLicensePlateException;
import com.adamo.vrspfab.vehicles.exceptions.InvalidVehicleDataException;
import com.adamo.vrspfab.vehicles.mappers.VehicleMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock private VehicleRepository vehicleRepository;
    @Mock private VehicleMapper vehicleMapper;
    @Mock private com.adamo.vrspfab.reservations.ReservationRepository reservationRepository;
    @Mock private com.adamo.vrspfab.reservations.ReservationMapper reservationMapper;
    @Mock private VehicleCategoryRepository vehicleCategoryRepository;
    @Mock private VehicleBrandRepository vehicleBrandRepository;
    @Mock private VehicleModelRepository vehicleModelRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private VehicleService vehicleService;

    @Test
    void createVehicle_whenDuplicateLicense_throwsDuplicate() {
        VehicleDto dto = new VehicleDto();
        dto.setLicensePlate("ABC-123");
        given(vehicleRepository.findByLicensePlate("ABC-123")).willReturn(Optional.of(new Vehicle()));
        assertThrows(DuplicateLicensePlateException.class, () -> vehicleService.createVehicle(dto));
    }

    @Test
    void createVehicle_whenYearInFuture_throwsInvalid() {
        VehicleDto dto = new VehicleDto();
        dto.setLicensePlate("XYZ-999");
        dto.setYear(LocalDate.now().getYear() + 1);
        given(vehicleRepository.findByLicensePlate("XYZ-999")).willReturn(Optional.empty());
        assertThrows(InvalidVehicleDataException.class, () -> vehicleService.createVehicle(dto));
    }

    @Test
    void updateVehicle_whenYearInFuture_throwsInvalid() {
        VehicleDto dto = new VehicleDto();
        dto.setLicensePlate("AAA-111");
        dto.setYear(LocalDate.now().getYear() + 1);
        Vehicle entity = new Vehicle();
        entity.setLicensePlate("AAA-111");
        given(vehicleRepository.findById(1L)).willReturn(Optional.of(entity));
        assertThrows(InvalidVehicleDataException.class, () -> vehicleService.updateVehicle(1L, dto));
    }

    @Test
    void getVehicleById_whenNotFound_throws() {
        given(vehicleRepository.findById(42L)).willReturn(Optional.empty());
        assertThrows(com.adamo.vrspfab.common.ResourceNotFoundException.class, () -> vehicleService.getVehicleById(42L));
    }
}
