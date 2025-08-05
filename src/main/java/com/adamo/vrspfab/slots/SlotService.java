package com.adamo.vrspfab.slots;

import com.adamo.vrspfab.common.ResourceNotFoundException;
import com.adamo.vrspfab.vehicles.VehicleService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class SlotService {

    private final SlotRepository slotRepository;
    private final SlotMapper slotMapper;
    private final VehicleService vehicleService;

    @Transactional
    public Slot createSlot(SlotDto slotDto) {
        Slot slot = slotMapper.toEntity(slotDto);
        slot.setVehicle(vehicleService.getVehicleById(slotDto.getVehicleId()));
        slot.setAvailable(true); // Default to available
        return slotRepository.save(slot);
    }

    @Transactional(readOnly = true)
    public Slot getSlotById(Long id) {
        return slotRepository.findWithVehicleById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));
    }

    @Transactional
    public Slot bookSlot(Long id) {
        Slot slot = getSlotById(id);
        if (!slot.isAvailable()) {
            throw new IllegalStateException("Slot must be available to book");
        }
        slot.setAvailable(false);
        return slotRepository.save(slot);
    }

    @Transactional
    public Slot blockSlot(Long id) {
        Slot slot = getSlotById(id);
        if (!slot.isAvailable()) {
            throw new IllegalStateException("Slot must be available to block");
        }
        slot.setAvailable(false); // Treat as blocked
        return slotRepository.save(slot);
    }

    @Transactional
    public void deleteSlot(Long id) {
        Slot slot = getSlotById(id);
        slotRepository.delete(slot);
    }

    @Transactional(readOnly = true)
    public List<SlotDto> getAllSlots(int page, int size, Boolean isAvailable) {
        List<Slot> slots = slotRepository.findAllWithVehicle();
        if (isAvailable != null) {
            slots = slotRepository.findByAvailable(isAvailable);
        }
        return slots.stream()
                .skip((long) page * size)
                .limit(size)
                .map(slotMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SlotDto> getAvailableSlotsByVehicleId(Long vehicleId) {
        List<Slot> slots = slotRepository.findAvailableSlotsByVehicleId(vehicleId);
        return slots.stream().map(slotMapper::toDto).toList();
    }
}