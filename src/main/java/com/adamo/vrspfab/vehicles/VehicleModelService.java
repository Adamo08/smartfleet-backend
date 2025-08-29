package com.adamo.vrspfab.vehicles;

import com.adamo.vrspfab.vehicles.dto.CreateVehicleModelDto;
import com.adamo.vrspfab.vehicles.dto.UpdateVehicleModelDto;
import com.adamo.vrspfab.vehicles.dto.VehicleModelResponseDto;
import com.adamo.vrspfab.vehicles.exceptions.DuplicateVehicleModelException;
import com.adamo.vrspfab.vehicles.exceptions.VehicleBrandNotFoundException;
import com.adamo.vrspfab.vehicles.exceptions.VehicleModelNotFoundException;
import com.adamo.vrspfab.vehicles.mappers.EnhancedVehicleModelMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VehicleModelService {

    private final VehicleModelRepository modelRepository;
    private final VehicleBrandRepository brandRepository;
    private final EnhancedVehicleModelMapper modelMapper;

    @Transactional(readOnly = true)
    @Cacheable(value = "vehicleModels", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<VehicleModelResponseDto> getAllModels(Pageable pageable) {
        log.info("Fetching all vehicle models with pagination");
        Page<VehicleModel> modelsPage = modelRepository.findAll(pageable);
        return modelsPage.map(modelMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "vehicleModels", key = "#id")
    public VehicleModelResponseDto getModelById(Long id) {
        log.info("Fetching vehicle model with ID: {}", id);
        VehicleModel model = modelRepository.findById(id)
                .orElseThrow(() -> new VehicleModelNotFoundException(id));
        return modelMapper.toResponseDto(model);
    }

    @CacheEvict(value = "vehicleModels", allEntries = true)
    public VehicleModelResponseDto createModel(@Valid CreateVehicleModelDto createDto) {
        log.info("Creating new vehicle model: {}", createDto.getName());

        // Check if brand exists
        VehicleBrand brand = brandRepository.findById(createDto.getBrandId())
                .orElseThrow(() -> new VehicleBrandNotFoundException(createDto.getBrandId()));

        if (modelRepository.existsByNameAndBrandId(createDto.getName(), createDto.getBrandId())) {
            log.warn("Model creation failed: Model with name '{}' already exists for brand '{}'", 
                     createDto.getName(), brand.getName());
            throw new DuplicateVehicleModelException(createDto.getName(), brand.getName());
        }

        VehicleModel model = modelMapper.toEntity(createDto);
        model.setBrand(brand);
        VehicleModel savedModel = modelRepository.save(model);
        log.info("Vehicle model created successfully with ID: {}", savedModel.getId());
        return modelMapper.toResponseDto(savedModel);
    }

    @CacheEvict(value = "vehicleModels", allEntries = true)
    public VehicleModelResponseDto updateModel(Long id, @Valid UpdateVehicleModelDto updateDto) {
        log.info("Updating vehicle model with ID: {}", id);
        VehicleModel existingModel = modelRepository.findById(id)
                .orElseThrow(() -> new VehicleModelNotFoundException(id));

        // Check if brand exists if it's being changed
        if (updateDto.getBrandId() != null && !existingModel.getBrand().getId().equals(updateDto.getBrandId())) {
            VehicleBrand brand = brandRepository.findById(updateDto.getBrandId())
                    .orElseThrow(() -> new VehicleBrandNotFoundException(updateDto.getBrandId()));
            existingModel.setBrand(brand);
        }

        // Check for name conflicts if name is being changed
        if (updateDto.getName() != null && 
            !existingModel.getName().equals(updateDto.getName()) && 
            modelRepository.existsByNameAndBrandId(updateDto.getName(), 
                updateDto.getBrandId() != null ? updateDto.getBrandId() : existingModel.getBrand().getId())) {
            log.warn("Model update failed: Model with name '{}' already exists for this brand", updateDto.getName());
            throw new DuplicateVehicleModelException(updateDto.getName(), existingModel.getBrand().getName());
        }

        modelMapper.updateEntity(existingModel, updateDto);
        VehicleModel updatedModel = modelRepository.save(existingModel);
        log.info("Vehicle model with ID {} updated successfully", id);
        return modelMapper.toResponseDto(updatedModel);
    }

    @CacheEvict(value = "vehicleModels", allEntries = true)
    public void deleteModel(Long id) {
        log.info("Deleting vehicle model with ID: {}", id);
        if (!modelRepository.existsById(id)) {
            throw new VehicleModelNotFoundException(id);
        }
        modelRepository.deleteById(id);
        log.info("Vehicle model with ID {} deleted successfully", id);
    }

    @CacheEvict(value = "vehicleModels", allEntries = true)
    public VehicleModelResponseDto toggleModelStatus(Long id) {
        log.info("Toggling status for vehicle model with ID: {}", id);
        VehicleModel model = modelRepository.findById(id)
                .orElseThrow(() -> new VehicleModelNotFoundException(id));

        model.setIsActive(!model.getIsActive());
        VehicleModel updatedModel = modelRepository.save(model);
        log.info("Vehicle model status toggled successfully for ID: {}", id);
        return modelMapper.toResponseDto(updatedModel);
    }

    // Legacy method for backward compatibility - delegates to new implementation
    @Deprecated
    public VehicleModelDto getModelByIdLegacy(Long id) {
        VehicleModelResponseDto responseDto = getModelById(id);
        VehicleModelDto legacyDto = new VehicleModelDto();
        legacyDto.setId(responseDto.getId());
        legacyDto.setName(responseDto.getName());
        legacyDto.setBrandId(responseDto.getBrandId());
        legacyDto.setBrandName(responseDto.getBrandName());
        legacyDto.setDescription(responseDto.getDescription());
        legacyDto.setIsActive(responseDto.getIsActive());
        legacyDto.setCreatedAt(responseDto.getCreatedAt().toString());
        legacyDto.setUpdatedAt(responseDto.getUpdatedAt().toString());
        return legacyDto;
    }
}
