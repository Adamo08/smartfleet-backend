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
public class VehicleCategoryService {

    private final VehicleCategoryRepository categoryRepository;
    private final VehicleCategoryMapper categoryMapper;

    public Page<VehicleCategoryDto> getAllCategories(Pageable pageable) {
        log.info("Fetching all vehicle categories with pagination");
        Page<VehicleCategory> categoriesPage = categoryRepository.findAll(pageable);
        return categoriesPage.map(categoryMapper::toDto);
    }

    public VehicleCategoryDto getCategoryById(Long id) {
        log.info("Fetching vehicle category with ID: {}", id);
        VehicleCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle category not found with ID: " + id));
        return categoryMapper.toDto(category);
    }

    public VehicleCategoryDto createCategory(VehicleCategoryDto categoryDto) {
        log.info("Creating new vehicle category: {}", categoryDto.getName());
        if (categoryRepository.existsByName(categoryDto.getName())) {
            throw new RuntimeException("Vehicle category with name '" + categoryDto.getName() + "' already exists");
        }

        VehicleCategory category = categoryMapper.toEntity(categoryDto);
        VehicleCategory savedCategory = categoryRepository.save(category);
        return categoryMapper.toDto(savedCategory);
    }

    public VehicleCategoryDto updateCategory(Long id, VehicleCategoryDto categoryDto) {
        log.info("Updating vehicle category with ID: {}", id);
        VehicleCategory existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle category not found with ID: " + id));

        categoryMapper.updateEntity(existingCategory, categoryDto);
        VehicleCategory updatedCategory = categoryRepository.save(existingCategory);
        return categoryMapper.toDto(updatedCategory);
    }

    public void deleteCategory(Long id) {
        log.info("Deleting vehicle category with ID: {}", id);
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Vehicle category not found with ID: " + id);
        }
        categoryRepository.deleteById(id);
    }

    public void toggleCategoryStatus(Long id) {
        log.info("Toggling status for vehicle category with ID: {}", id);
        VehicleCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle category not found with ID: " + id));

        category.setIsActive(!category.getIsActive());
        categoryRepository.save(category);
    }
}
