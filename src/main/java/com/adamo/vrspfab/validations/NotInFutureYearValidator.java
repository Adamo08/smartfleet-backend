package com.adamo.vrspfab.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class NotInFutureYearValidator implements ConstraintValidator<NotInFutureYear, Integer> {

    @Override
    public void initialize(NotInFutureYear constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(Integer year, ConstraintValidatorContext context) {
        if (year == null) {
            return true; // Let @NotNull handle null validation
        }
        return year <= LocalDate.now().getYear();
    }
}