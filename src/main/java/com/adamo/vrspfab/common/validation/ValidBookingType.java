package com.adamo.vrspfab.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validation annotation to ensure booking type is one of the allowed values.
 */
@Documented
@Constraint(validatedBy = BookingTypeValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBookingType {
    String message() default "Invalid booking type. Must be one of: HOURLY, DAILY, WEEKLY, CUSTOM";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
