package com.adamo.vrspfab.vehicles;

import com.adamo.vrspfab.vehicles.dto.CreateVehicleCategoryDto;
import com.adamo.vrspfab.vehicles.dto.UpdateVehicleCategoryDto;
import com.adamo.vrspfab.vehicles.dto.VehicleCategoryResponseDto;
import com.adamo.vrspfab.vehicles.exceptions.DuplicateVehicleCategoryException;
import com.adamo.vrspfab.vehicles.exceptions.VehicleCategoryNotFoundException;
import com.adamo.vrspfab.vehicles.mappers.EnhancedVehicleCategoryMapper;
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
public class VehicleCategoryService {

    private final VehicleCategoryRepository categoryRepository;
    private final EnhancedVehicleCategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    @Cacheable(value = "vehicleCategories", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<VehicleCategoryResponseDto> getAllCategories(Pageable pageable) {
        log.info("Fetching all vehicle categories with pagination");
        Page<VehicleCategory> categoriesPage = categoryRepository.findAll(pageable);
        return categoriesPage.map(categoryMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "vehicleCategories", key = "#id")
    public VehicleCategoryResponseDto getCategoryById(Long id) {
        log.info("Fetching vehicle category with ID: {}", id);
        VehicleCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new VehicleCategoryNotFoundException(id));
        return categoryMapper.toResponseDto(category);
    }

    @CacheEvict(value = "vehicleCategories", allEntries = true)
    public VehicleCategoryResponseDto createCategory(@Valid CreateVehicleCategoryDto createDto) {
        log.info("Creating new vehicle category: {}", createDto.getName());
        
        if (categoryRepository.existsByName(createDto.getName())) {
            log.warn("Category creation failed: Category with name '{}' already exists", createDto.getName());
            throw new DuplicateVehicleCategoryException(createDto.getName());
        }

        VehicleCategory category = categoryMapper.toEntity(createDto);
        VehicleCategory savedCategory = categoryRepository.save(category);
        log.info("Vehicle category created successfully with ID: {}", savedCategory.getId());
        return categoryMapper.toResponseDto(savedCategory);
    }

    @CacheEvict(value = "vehicleCategories", allEntries = true)
    public VehicleCategoryResponseDto updateCategory(Long id, @Valid UpdateVehicleCategoryDto updateDto) {
        log.info("Updating vehicle category with ID: {}", id);
        VehicleCategory existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new VehicleCategoryNotFoundException(id));

        // Check for name conflicts if name is being changed
        if (updateDto.getName() != null && 
            !existingCategory.getName().equals(updateDto.getName()) && 
            categoryRepository.existsByName(updateDto.getName())) {
            log.warn("Category update failed: Category with name '{}' already exists", updateDto.getName());
            throw new DuplicateVehicleCategoryException(updateDto.getName());
        }

        categoryMapper.updateEntity(existingCategory, updateDto);
        VehicleCategory updatedCategory = categoryRepository.save(existingCategory);
        log.info("Vehicle category with ID {} updated successfully", id);
        return categoryMapper.toResponseDto(updatedCategory);
    }

    @CacheEvict(value = "vehicleCategories", allEntries = true)
    public void deleteCategory(Long id) {
        log.info("Deleting vehicle category with ID: {}", id);
        if (!categoryRepository.existsById(id)) {
            throw new VehicleCategoryNotFoundException(id);
        }
        categoryRepository.deleteById(id);
        log.info("Vehicle category with ID {} deleted successfully", id);
    }

    @CacheEvict(value = "vehicleCategories", allEntries = true)
    public VehicleCategoryResponseDto toggleCategoryStatus(Long id) {
        log.info("Toggling status for vehicle category with ID: {}", id);
        VehicleCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new VehicleCategoryNotFoundException(id));

        category.setIsActive(!category.getIsActive());
        VehicleCategory updatedCategory = categoryRepository.save(category);
        log.info("Vehicle category status toggled successfully for ID: {}", id);
        return categoryMapper.toResponseDto(updatedCategory);
    }

    // Legacy method for backward compatibility - delegates to new implementation
    @Deprecated
    public VehicleCategoryDto getCategoryByIdLegacy(Long id) {
        VehicleCategoryResponseDto responseDto = getCategoryById(id);
        VehicleCategoryDto legacyDto = new VehicleCategoryDto();
        legacyDto.setId(responseDto.getId());
        legacyDto.setName(responseDto.getName());
        legacyDto.setDescription(responseDto.getDescription());
        legacyDto.setIconUrl(responseDto.getIconUrl());
        legacyDto.setIsActive(responseDto.getIsActive());
        legacyDto.setCreatedAt(responseDto.getCreatedAt().toString());
        legacyDto.setUpdatedAt(responseDto.getUpdatedAt().toString());
        return legacyDto;
    }
}
