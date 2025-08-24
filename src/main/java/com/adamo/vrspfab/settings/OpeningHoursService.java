package com.adamo.vrspfab.settings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OpeningHoursService {

    private final OpeningHoursRepository openingHoursRepository;

    public List<OpeningHoursDto> getAllOpeningHours() {
        log.info("Fetching all opening hours");
        List<OpeningHours> hours = openingHoursRepository.findAllActiveOrdered();
        return hours.stream()
                .map(this::toDto)
                .toList();
    }

    public OpeningHoursDto getOpeningHoursByDay(DayOfWeek dayOfWeek) {
        log.info("Fetching opening hours for day: {}", dayOfWeek);
        OpeningHours hours = openingHoursRepository.findByDayOfWeek(dayOfWeek)
                .orElseThrow(() -> new RuntimeException("Opening hours not found for day: " + dayOfWeek));
        return toDto(hours);
    }

    public OpeningHoursDto createOpeningHours(OpeningHoursDto dto) {
        log.info("Creating opening hours for day: {}", dto.getDayOfWeek());
        if (openingHoursRepository.existsByDayOfWeek(dto.getDayOfWeek())) {
            throw new RuntimeException("Opening hours already exist for day: " + dto.getDayOfWeek());
        }

        OpeningHours hours = toEntity(dto);
        OpeningHours savedHours = openingHoursRepository.save(hours);
        return toDto(savedHours);
    }

    public OpeningHoursDto updateOpeningHours(Long id, OpeningHoursDto dto) {
        log.info("Updating opening hours with ID: {}", id);
        OpeningHours existingHours = openingHoursRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Opening hours not found with ID: " + id));

        updateEntity(existingHours, dto);
        OpeningHours updatedHours = openingHoursRepository.save(existingHours);
        return toDto(updatedHours);
    }

    public void deleteOpeningHours(Long id) {
        log.info("Deleting opening hours with ID: {}", id);
        if (!openingHoursRepository.existsById(id)) {
            throw new RuntimeException("Opening hours not found with ID: " + id);
        }
        openingHoursRepository.deleteById(id);
    }

    public void initializeDefaultOpeningHours() {
        log.info("Initializing default opening hours");
        if (openingHoursRepository.count() == 0) {
            for (DayOfWeek day : DayOfWeek.values()) {
                OpeningHours hours = OpeningHours.builder()
                        .dayOfWeek(day)
                        .isOpen(!day.equals(DayOfWeek.SUNDAY))
                        .openTime(day.equals(DayOfWeek.SATURDAY) ? LocalTime.of(10, 0) : LocalTime.of(9, 0))
                        .closeTime(day.equals(DayOfWeek.SATURDAY) ? LocalTime.of(16, 0) : LocalTime.of(17, 0))
                        .is24Hour(false)
                        .isActive(true)
                        .build();
                openingHoursRepository.save(hours);
            }
            log.info("Default opening hours initialized successfully");
        }
    }

    private OpeningHoursDto toDto(OpeningHours entity) {
        OpeningHoursDto dto = new OpeningHoursDto();
        dto.setId(entity.getId());
        dto.setDayOfWeek(entity.getDayOfWeek());
        dto.setIsOpen(entity.getIsOpen());
        dto.setOpenTime(entity.getOpenTime());
        dto.setCloseTime(entity.getCloseTime());
        dto.setIs24Hour(entity.getIs24Hour());
        dto.setNotes(entity.getNotes());
        dto.setIsActive(entity.getIsActive());
        return dto;
    }

    private OpeningHours toEntity(OpeningHoursDto dto) {
        return OpeningHours.builder()
                .dayOfWeek(dto.getDayOfWeek())
                .isOpen(dto.getIsOpen())
                .openTime(dto.getOpenTime())
                .closeTime(dto.getCloseTime())
                .is24Hour(dto.getIs24Hour())
                .notes(dto.getNotes())
                .isActive(dto.getIsActive())
                .build();
    }

    private void updateEntity(OpeningHours entity, OpeningHoursDto dto) {
        entity.setIsOpen(dto.getIsOpen());
        entity.setOpenTime(dto.getOpenTime());
        entity.setCloseTime(dto.getCloseTime());
        entity.setIs24Hour(dto.getIs24Hour());
        entity.setNotes(dto.getNotes());
        entity.setIsActive(dto.getIsActive());
    }
}
