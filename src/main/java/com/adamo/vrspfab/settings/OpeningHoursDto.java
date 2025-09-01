package com.adamo.vrspfab.settings;

import lombok.Data;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
public class OpeningHoursDto {
    private Long id;
    private DayOfWeek dayOfWeek;
    private Boolean isOpen;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Boolean is24Hour;
    private String notes;
    private Boolean isActive;
}
