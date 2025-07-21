package com.adamo.vrspfab.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {


    /**
     * This method handles validation exceptions for method arguments.
     * It collects field errors and returns them as a map in the response body.
     *
     * @param ex The MethodArgumentNotValidException containing validation errors.
     * @return A ResponseEntity with a map of field errors and their messages.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex
    ) {
        // Create a map to hold field errors
        var errors = new HashMap<String, String>();

        // Get the list of field errors from the exception
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

        // Iterate through the field errors and populate the map
        for (FieldError fieldError : fieldErrors) {
            String fieldName = fieldError.getField();
            String errorMessage = fieldError.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        }

        // Return a bad request response with the errors map
        return ResponseEntity.badRequest().body(errors);
    }
}
