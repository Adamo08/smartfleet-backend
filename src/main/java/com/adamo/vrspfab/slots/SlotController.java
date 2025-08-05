package com.adamo.vrspfab.slots;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@AllArgsConstructor
@RestController
@RequestMapping("/slots")
public class SlotController {

    private final SlotService slotService;
    private final SlotMapper slotMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SlotDto createSlot(@RequestBody SlotDto slotDto) {
        Slot slot = slotService.createSlot(slotDto);
        return slotMapper.toDto(slot);
    }

    @GetMapping("/vehicle/{vehicleId}/available")
    public ResponseEntity<List<SlotDto>> getAvailableSlots(@PathVariable Long vehicleId) {
        List<SlotDto> slots = slotService.getAvailableSlotsByVehicleId(vehicleId);
        return ResponseEntity.ok(slots);
    }

    @PutMapping("/{id}/book")
    public ResponseEntity<SlotDto> bookSlot(@PathVariable Long id) {
        Slot slot = slotService.bookSlot(id);
        return ResponseEntity.ok(slotMapper.toDto(slot));
    }

    @PutMapping("/{id}/block")
    public ResponseEntity<SlotDto> blockSlot(@PathVariable Long id) {
        Slot slot = slotService.blockSlot(id);
        return ResponseEntity.ok(slotMapper.toDto(slot));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSlot(@PathVariable Long id) {
        slotService.deleteSlot(id);
    }

    @GetMapping
    public ResponseEntity<List<SlotDto>> getAllSlots(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean isAvailable) {
        List<SlotDto> slots = slotService.getAllSlots(page, size, isAvailable);
        return ResponseEntity.ok(slots);
    }
}