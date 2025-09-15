package com.adamo.vrspfab.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;

/**
 * Validator for booking type validation.
 */
public class BookingTypeValidator implements ConstraintValidator<ValidBookingType, String> {
    
    private static final List<String> VALID_BOOKING_TYPES = Arrays.asList(
        "HOURLY", "DAILY", "WEEKLY", "CUSTOM"
    );

    @Override
    public boolean isValid(String bookingType, ConstraintValidatorContext context) {
        if (bookingType == null) {
            return true; // Let @NotNull handle null validation
        }
        return VALID_BOOKING_TYPES.contains(bookingType.toUpperCase());
    }
}
