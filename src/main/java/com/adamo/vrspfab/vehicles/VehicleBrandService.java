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
public class VehicleBrandService {

    private final VehicleBrandRepository brandRepository;
    private final VehicleBrandMapper brandMapper;

    public Page<VehicleBrandDto> getAllBrands(Pageable pageable) {
        log.info("Fetching all vehicle brands with pagination");
        Page<VehicleBrand> brandsPage = brandRepository.findAll(pageable);
        return brandsPage.map(brandMapper::toDto);
    }

    public VehicleBrandDto getBrandById(Long id) {
        log.info("Fetching vehicle brand with ID: {}", id);
        VehicleBrand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle brand not found with ID: " + id));
        return brandMapper.toDto(brand);
    }

    public VehicleBrandDto createBrand(VehicleBrandDto brandDto) {
        log.info("Creating new vehicle brand: {}", brandDto.getName());
        if (brandRepository.existsByName(brandDto.getName())) {
            throw new RuntimeException("Vehicle brand with name '" + brandDto.getName() + "' already exists");
        }

        VehicleBrand brand = brandMapper.toEntity(brandDto);
        VehicleBrand savedBrand = brandRepository.save(brand);
        return brandMapper.toDto(savedBrand);
    }

    public VehicleBrandDto updateBrand(Long id, VehicleBrandDto brandDto) {
        log.info("Updating vehicle brand with ID: {}", id);
        VehicleBrand existingBrand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle brand not found with ID: " + id));

        brandMapper.updateEntity(existingBrand, brandDto);
        VehicleBrand updatedBrand = brandRepository.save(existingBrand);
        return brandMapper.toDto(updatedBrand);
    }

    public void deleteBrand(Long id) {
        log.info("Deleting vehicle brand with ID: {}", id);
        if (!brandRepository.existsById(id)) {
            throw new RuntimeException("Vehicle brand not found with ID: " + id);
        }
        brandRepository.deleteById(id);
    }

    public void toggleBrandStatus(Long id) {
        log.info("Toggling status for vehicle brand with ID: {}", id);
        VehicleBrand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle brand not found with ID: " + id));

        brand.setIsActive(!brand.getIsActive());
        brandRepository.save(brand);
    }
}
