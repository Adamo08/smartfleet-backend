package com.adamo.vrspfab.vehicles;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VehicleModelService {

    private final VehicleModelRepository modelRepository;
    private final VehicleBrandRepository brandRepository;
    private final VehicleModelMapper modelMapper;

    public Page<VehicleModelDto> getAllModels(Pageable pageable) {
        log.info("Fetching all vehicle models with pagination");
        Page<VehicleModel> modelsPage = modelRepository.findAll(pageable);
        return modelsPage.map(modelMapper::toDto);
    }

    public VehicleModelDto getModelById(Long id) {
        log.info("Fetching vehicle model with ID: {}", id);
        VehicleModel model = modelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle model not found with ID: " + id));
        return modelMapper.toDto(model);
    }

    public VehicleModelDto createModel(VehicleModelDto modelDto) {
        log.info("Creating new vehicle model: {}", modelDto.getName());

        // Check if brand exists
        VehicleBrand brand = brandRepository.findById(modelDto.getBrandId())
                .orElseThrow(() -> new RuntimeException("Vehicle brand not found with ID: " + modelDto.getBrandId()));

        if (modelRepository.existsByNameAndBrandId(modelDto.getName(), modelDto.getBrandId())) {
            throw new RuntimeException("Vehicle model with name '" + modelDto.getName() + "' already exists for this brand");
        }

        VehicleModel model = modelMapper.toEntity(modelDto);
        model.setBrand(brand);
        VehicleModel savedModel = modelRepository.save(model);
        return modelMapper.toDto(savedModel);
    }

    public VehicleModelDto updateModel(Long id, VehicleModelDto modelDto) {
        log.info("Updating vehicle model with ID: {}", id);
        VehicleModel existingModel = modelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle model not found with ID: " + id));

        // Check if brand exists if it's being changed
        if (!existingModel.getBrand().getId().equals(modelDto.getBrandId())) {
            VehicleBrand brand = brandRepository.findById(modelDto.getBrandId())
                    .orElseThrow(() -> new RuntimeException("Vehicle brand not found with ID: " + modelDto.getBrandId()));
            existingModel.setBrand(brand);
        }

        modelMapper.updateEntity(existingModel, modelDto);
        VehicleModel updatedModel = modelRepository.save(existingModel);
        return modelMapper.toDto(updatedModel);
    }

    public void deleteModel(Long id) {
        log.info("Deleting vehicle model with ID: {}", id);
        if (!modelRepository.existsById(id)) {
            throw new RuntimeException("Vehicle model not found with ID: " + id);
        }
        modelRepository.deleteById(id);
    }

    public void toggleModelStatus(Long id) {
        log.info("Toggling status for vehicle model with ID: {}", id);
        VehicleModel model = modelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle model not found with ID: " + id));

        model.setIsActive(!model.getIsActive());
        modelRepository.save(model);
    }
}
