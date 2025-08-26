package com.adamo.vrspfab.common;

import com.adamo.vrspfab.bookmarks.DuplicateBookmarkException;
import com.adamo.vrspfab.favorites.DuplicateFavoriteException;
import com.adamo.vrspfab.notifications.NotificationNotFoundException;
import com.adamo.vrspfab.payments.PaymentException;
import com.adamo.vrspfab.reservations.ReservationBusinessException;
import com.adamo.vrspfab.reservations.ReservationConflictException;
import com.adamo.vrspfab.slots.InvalidSlotStateException;
import com.adamo.vrspfab.slots.InvalidSlotTimeException;
import com.adamo.vrspfab.slots.NoAvailableSlotsException;
import com.adamo.vrspfab.testimonials.DuplicateTestimonialException;
import com.adamo.vrspfab.users.UserNotFoundException;
import com.adamo.vrspfab.vehicles.DuplicateLicensePlateException;
import com.adamo.vrspfab.vehicles.InvalidVehicleDataException;
import com.adamo.vrspfab.vehicles.InvalidVehicleStatusUpdateException;
import com.adamo.vrspfab.vehicles.VehicleDecommissionedException;
import com.adamo.vrspfab.vehicles.VehicleNotAvailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation exceptions for method arguments.
     * Collects field errors and returns them as a map in the response body.
     *
     * @param ex The MethodArgumentNotValidException containing validation errors.
     * @return A ResponseEntity with a map of field errors and their messages.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex
    ) {
        log.warn("Validation error: {}", ex.getMessage());
        var errors = new HashMap<String, String>();
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        for (FieldError fieldError : fieldErrors) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(errors);
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDto> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, WebRequest request
    ) {
        log.warn("Invalid method argument: {}", ex.getMessage());
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s.",
                ex.getValue(), ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                message,
                request.getDescription(false).replace("uri=", "")
        );
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDto> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, WebRequest request
    ) {
        log.warn("Malformed JSON request: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "Malformed JSON or invalid data type: " + ex.getMostSpecificCause().getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }


    /**
     * Handles ResourceNotFoundException, returning a 404 Not Found status.
     *
     * @param ex The ResourceNotFoundException.
     * @param request The WebRequest.
     * @return ResponseEntity containing the error details.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDto> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request
    ) {
        String errorMessage = ex.getResourceType() + " not found: " + ex.getMessage();
        log.warn("Resource not found: {}", errorMessage);
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Not Found",
                errorMessage,
                request.getDescription(false).replace("uri=", "")
        );
    }


    /**
     * Handles UserNotFoundException, returning a 404 Not Found status.
     *
     * @param ex The UserNotFoundException.
     * @param request The WebRequest.
     * @return ResponseEntity containing the error details.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorDto> handleUserNotFoundException(
            UserNotFoundException ex, WebRequest request
    ) {
        log.warn("User not found: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Not Found",
                "User not found: " + ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }

    /**
     * Handles IllegalStateException, returning a 400 Bad Request status.
     *
     * @param ex The IllegalStateException.
     * @param request The WebRequest.
     * @return ResponseEntity containing the error details.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorDto> handleIllegalStateException(
            IllegalStateException ex, WebRequest request
    ) {
        log.warn("Illegal state: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }

    /**
     * Handles AccessDeniedException, returning a 403 Forbidden status.
     *
     * @param ex The AccessDeniedException.
     * @param request The WebRequest.
     * @return ResponseEntity containing the error details.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDto> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request
    ) {
        log.warn("Access denied: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "Access denied: " + ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }


    /**
     * Handles DuplicateFieldException, returning a 409 Conflict status.
     *
     * @param ex The DuplicateFieldException.
     * @param request The WebRequest.
     * @return ResponseEntity containing the error details.
     */
    @ExceptionHandler(DuplicateFieldException.class)
    public ResponseEntity<ErrorDto> handleDuplicateFieldException(
            DuplicateFieldException ex, WebRequest request
    ) {
        log.warn("Duplicate field error: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Conflict",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }



    /**
     * Handles DuplicateBookmarkException, returning a 409 Conflict status.
     *
     * @param ex The DuplicateBookmarkException.
     * @param request The WebRequest.
     * @return ResponseEntity containing the error details.
     */
    @ExceptionHandler(DuplicateBookmarkException.class)
    public ResponseEntity<ErrorDto> handleDuplicateBookmarkException(
            DuplicateBookmarkException ex, WebRequest request
    ) {
        log.warn("Duplicate bookmark: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Conflict",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }

    /**
     * Handles DuplicateFavoriteException, returning a 409 Conflict status.
     *
     * @param ex The DuplicateFavoriteException.
     * @param request The WebRequest.
     * @return ResponseEntity containing the error details.
     */
    @ExceptionHandler(DuplicateFavoriteException.class)
    public ResponseEntity<ErrorDto> handleDuplicateFavoriteException(
            DuplicateFavoriteException ex, WebRequest request
    ) {
        log.warn("Duplicate favorite: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Conflict",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }

    /**
     * Handles DuplicateTestimonialException, returning a 409 Conflict status.
     *
     * @param ex The DuplicateTestimonialException.
     * @param request The WebRequest.
     * @return ResponseEntity containing the error details.
     */
    @ExceptionHandler(DuplicateTestimonialException.class)
    public ResponseEntity<ErrorDto> handleDuplicateTestimonialException(
            DuplicateTestimonialException ex, WebRequest request
    ) {
        log.warn("Duplicate testimonial: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Conflict",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }

    /**
     * Handles InvalidSlotStateException, returning a 400 Bad Request status.
     *
     * @param ex The InvalidSlotStateException.
     * @param request The WebRequest.
     * @return ResponseEntity containing the error details.
     */
    @ExceptionHandler(InvalidSlotStateException.class)
    public ResponseEntity<ErrorDto> handleInvalidSlotStateException(
            InvalidSlotStateException ex, WebRequest request
    ) {
        log.warn("Invalid slot state: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }

    /**
     * Handles InvalidSlotTimeException, returning a 400 Bad Request status.
     *
     * @param ex The InvalidSlotTimeException.
     * @param request The WebRequest.
     * @return ResponseEntity containing the error details.
     */
    @ExceptionHandler(InvalidSlotTimeException.class)
    public ResponseEntity<ErrorDto> handleInvalidSlotTimeException(
            InvalidSlotTimeException ex, WebRequest request
    ) {
        log.warn("Invalid slot time: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }

    @ExceptionHandler(NoAvailableSlotsException.class)
    public ResponseEntity<ErrorDto> handleNoAvailableSlotsException(
            NoAvailableSlotsException ex, WebRequest request
    ) {
        log.warn("No available slots available: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }


    /**
     * Handles DuplicateLicensePlateException, returning a 409 Conflict status.
     *
     * @param ex The DuplicateLicensePlateException.
     * @param request The WebRequest.
     * @return ResponseEntity containing the error details.
     */
    @ExceptionHandler(DuplicateLicensePlateException.class)
    public ResponseEntity<ErrorDto> handleDuplicateLicensePlateException(
            DuplicateLicensePlateException ex, WebRequest request
    ) {
        log.warn("Duplicate license plate: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Conflict",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }

    /**
     * Handles InvalidVehicleStatusUpdateException, returning a 400 Bad Request status.
     *
     * @param ex The InvalidVehicleStatusUpdateException.
     * @param request The WebRequest.
     * @return ResponseEntity containing the error details.
     */
    @ExceptionHandler(InvalidVehicleStatusUpdateException.class)
    public ResponseEntity<ErrorDto> handleInvalidVehicleStatusUpdateException(
            InvalidVehicleStatusUpdateException ex, WebRequest request
    ) {
        log.warn("Invalid vehicle status update: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }

    /**
     * Handles VehicleDecommissionedException, returning a 400 Bad Request status.
     *
     * @param ex The VehicleDecommissionedException.
     * @param request The WebRequest.
     * @return ResponseEntity containing the error details.
     */
    @ExceptionHandler(VehicleDecommissionedException.class)
    public ResponseEntity<ErrorDto> handleVehicleDecommissionedException(
            VehicleDecommissionedException ex, WebRequest request
    ) {
        log.warn("Vehicle decommissioned: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }

    /**
     * Handles VehicleNotAvailableException, returning a 400 Bad Request status.
     *
     * @param ex The VehicleNotAvailableException.
     * @param request The WebRequest.
     * @return ResponseEntity containing the error details.
     */
    @ExceptionHandler(VehicleNotAvailableException.class)
    public ResponseEntity<ErrorDto> handleVehicleNotAvailableException(
            VehicleNotAvailableException ex, WebRequest request
    ) {
        log.warn("Vehicle not available: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }

    /**
     * Handles InvalidVehicleDataException, returning a 400 Bad Request status.
     *
     * @param ex The InvalidVehicleDataException.
     * @param request The WebRequest.
     * @return ResponseEntity containing the error details.
     */
    @ExceptionHandler(InvalidVehicleDataException.class)
    public ResponseEntity<ErrorDto> handleInvalidVehicleDataException(
            InvalidVehicleDataException ex, WebRequest request
    ) {
        log.warn("Invalid vehicle data: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }


    /**
     * Handles NotificationNotFoundException, returning a 404 Not Found status.
     *
     * @param ex The NotificationNotFoundException.
     * @param request The WebRequest.
     * @return ResponseEntity containing the error details.
     */
    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ErrorDto> handleNotificationNotFoundException(
            NotificationNotFoundException ex, WebRequest request
    ) {
        log.warn("Notification not found: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Not Found",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }



    /**
     * Handles all reservation-specific business exceptions, returning a 400 Bad Request.
     * This serves as a catch-all for reservation logic errors that are not handled more specifically.
     *
     * @param ex The ReservationBusinessException.
     * @param request The WebRequest.
     * @return ResponseEntity containing the error details.
     */
    @ExceptionHandler(ReservationBusinessException.class)
    public ResponseEntity<ErrorDto> handleReservationBusinessException(
            ReservationBusinessException ex, WebRequest request
    ) {
        log.warn("Reservation business error: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }

    /**
     * Handles ReservationConflictException, returning a 409 Conflict status.
     *
     * @param ex The ReservationConflictException.
     * @param request The WebRequest.
     * @return ResponseEntity containing the error details.
     */
    @ExceptionHandler(ReservationConflictException.class)
    public ResponseEntity<ErrorDto> handleReservationConflictException(
            ReservationConflictException ex, WebRequest request
    ) {
        log.warn("Reservation conflict: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Conflict",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }



    /**
     * Handles PaymentException, returning a 400 Bad Request status.
     *
     * @param ex The PaymentException.
     * @param request The WebRequest.
     * @return ResponseEntity containing the error details.
     */
    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorDto> handlePaymentException(
            PaymentException ex, WebRequest request
    ) {
        log.warn("Payment error: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }


    /**
     * Builds an error response entity with the provided details.
     *
     * @param status HTTP status code for the response.
     * @param error The error type (e.g., "Not Found", "Bad Request").
     * @param message Detailed error message.
     * @param path The request path that caused the error.
     * @return ResponseEntity containing the error details.
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