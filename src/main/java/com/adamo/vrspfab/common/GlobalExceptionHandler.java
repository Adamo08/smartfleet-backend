package com.adamo.vrspfab.common;

import com.adamo.vrspfab.favorites.DuplicateFavoriteException;
import com.adamo.vrspfab.testimonials.DuplicateTestimonialException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
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

    /**
     * Handles ResourceNotFoundException, returning a 404 Not Found status.
     * Provides a specific message indicating which resource type was not found.
     * @param ex The ResourceNotFoundException.
     * @param request The WebRequest.
     * @return ResponseEntity containing the error details.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDto> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request
    ) {
        String errorMessage = ex.getResourceType() + " not found: " + ex.getMessage();
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Not Found",
                errorMessage,
                request.getDescription(false).replace("uri=", "")
        );
    }

    /**
     * Handles IllegalStateException, returning a 400 Bad Request status.
     * Useful for business logic violations (e.g., trying to approve an already approved testimonial).
     * @param ex The IllegalStateException.
     * @param request The WebRequest.
     * @return ResponseEntity containing the error details.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorDto> handleIllegalStateException(
            IllegalStateException ex, WebRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }

    /**
     * Handles AccessDeniedException, returning a 403 Forbidden status.
     * This provides a consistent error response for authorization failures.
     * @param ex The AccessDeniedException.
     * @param request The WebRequest.
     * @return ResponseEntity containing the error details.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDto> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }

    /**
     * Handles DuplicateTestimonialException, returning a 409 Conflict status.
     * This is used when a user tries to submit multiple testimonials for the same vehicle.
     * @param ex The DuplicateTestimonialException.
     * @param request The WebRequest.
     * @return ResponseEntity containing the error details.
     */
    @ExceptionHandler(DuplicateTestimonialException.class)
    public ResponseEntity<ErrorDto> handleDuplicateTestimonialException(
            DuplicateTestimonialException ex, WebRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.CONFLICT, // 409 Conflict is appropriate for duplicate resource creation
                "Conflict",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }


    /**
     * Handles DuplicateFavoriteException, returning a 409 Conflict status.
     * This is used when a user tries to favorite the same vehicle multiple times.
     * @param ex The DuplicateFavoriteException.
     * @param request The WebRequest.
     * @return ResponseEntity containing the error details.
     */
    @ExceptionHandler(DuplicateFavoriteException.class)
    public ResponseEntity<ErrorDto> handleDuplicateFavoriteException(
            DuplicateFavoriteException ex, WebRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.CONFLICT, // 409 Conflict is appropriate for duplicate resource creation
                "Conflict",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }


    /**
     * Builds an error response entity with the provided details.
     * @param status HTTP status code for the response
     * @param error the error type (e.g., "Not Found", "Bad Request")
     * @param message detailed error message
     * @param path the request path that caused the error
     * @return ResponseEntity containing the error details
     */
    private ResponseEntity<ErrorDto> buildErrorResponse(
            HttpStatus status,
            String error,
            String message,
            String path
    ) {
        return new ResponseEntity<>(
                new ErrorDto(
                        LocalDateTime.now(),
                        status.value(),
                        error,
                        message,
                        path
                ),
                status
        );
    }

}
