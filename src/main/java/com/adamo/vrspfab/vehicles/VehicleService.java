package com.adamo.vrspfab.vehicles;

import com.adamo.vrspfab.common.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleService {
    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private VehicleMapper vehicleMapper;

    @Transactional
    public Vehicle createVehicle(VehicleDto vehicleDTO) {
        if (vehicleRepository.findByLicensePlate(vehicleDTO.getLicensePlate()).isPresent()) {
            throw new DuplicateLicensePlateException("License plate already exists");
        }
        Vehicle vehicle = vehicleMapper.toEntity(vehicleDTO);
        return vehicleRepository.save(vehicle);
    }

    @Transactional(readOnly = true)
    public Vehicle getVehicleById(Long id) {
        return vehicleRepository.findWithSlotsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
    }

    @Transactional
    public Vehicle updateVehicle(Long id, VehicleDto vehicleDto) {
        Vehicle vehicle = getVehicleById(id);
        if (!vehicle.getLicensePlate().equals(vehicleDto.getLicensePlate()) &&
                vehicleRepository.findByLicensePlate(vehicleDto.getLicensePlate()).isPresent()) {
            throw new DuplicateLicensePlateException("License plate already exists");
        }
        vehicleMapper.updateVehicleFromDto(vehicleDto, vehicle);
        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public void deleteVehicle(Long id) {
        Vehicle vehicle = getVehicleById(id);
        vehicleRepository.delete(vehicle);
    }

    @Transactional(readOnly = true)
    public List<VehicleDto> getAllVehicles(int page, int size, VehicleStatus status) {
        List<Vehicle> vehicles = vehicleRepository.findAllWithSlots();
        if (status != null) {
            vehicles = vehicleRepository.findByStatus(status);
        }
        return vehicles.stream()
                .skip((long) page * size)
                .limit(size)
                .map(vehicleMapper::toDto)
                .collect(Collectors.toList());
    }
}