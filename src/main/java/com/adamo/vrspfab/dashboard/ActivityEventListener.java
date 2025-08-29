package com.adamo.vrspfab.dashboard;

import com.adamo.vrspfab.payments.PaymentCompletedEvent;
import com.adamo.vrspfab.reservations.Reservation;
import com.adamo.vrspfab.reservations.ReservationRepository;
import com.adamo.vrspfab.users.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Listens to application events and automatically records activities
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ActivityEventListener {
    
    private final ActivityService activityService;
    private final ReservationRepository reservationRepository;
    
    /**
     * Record activity when payment is completed
     */
    @EventListener
    @Async
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        try {
            Reservation reservation = reservationRepository.findById(event.getReservationId())
                    .orElse(null);
            
            if (reservation != null) {
                User user = reservation.getUser();
                String description = String.format("Payment of reservation #%d completed successfully", 
                        reservation.getId());
                
                Map<String, Object> metadata = Map.of(
                        "paymentId", event.getPaymentId(),
                        "reservationId", event.getReservationId(),
                        "vehicleId", reservation.getVehicle().getId(),
                        "vehicleName", reservation.getVehicle().getBrand().getName() + " " + reservation.getVehicle().getModel().getName()
                );
                
                activityService.recordActivity(
                        ActivityType.PAYMENT_COMPLETED,
                        "Payment Completed", 
                        description,
                        user,
                        "PAYMENT",
                        event.getPaymentId(),
                        metadata
                );
                
                log.info("Recorded payment completed activity for reservation {}", event.getReservationId());
            }
        } catch (Exception e) {
            log.error("Failed to record payment completed activity", e);
        }
    }
    
    /**
     * Record activity for reservation creation
     */
    public void recordReservationCreated(Reservation reservation) {
        try {
            String description = String.format("New reservation created for %s %s", 
                    reservation.getVehicle().getBrand().getName(), reservation.getVehicle().getModel().getName());
            
            Map<String, Object> metadata = Map.of(
                    "reservationId", reservation.getId(),
                    "vehicleId", reservation.getVehicle().getId(),
                    "vehicleName", reservation.getVehicle().getBrand().getName() + " " + reservation.getVehicle().getModel().getName(),
                    "startDate", reservation.getStartDate().toString(),
                    "endDate", reservation.getEndDate().toString()
            );
            
            activityService.recordActivity(
                    ActivityType.RESERVATION_CREATED,
                    "New Reservation",
                    description,
                    reservation.getUser(),
                    "RESERVATION",
                    reservation.getId(),
                    metadata
            );
            
            log.info("Recorded reservation created activity for reservation {}", reservation.getId());
        } catch (Exception e) {
            log.error("Failed to record reservation created activity", e);
        }
    }
    
    /**
     * Record activity for user registration
     */
    public void recordUserRegistration(User user) {
        try {
            String description = String.format("New user %s registered", user.getFullName());
            
            Map<String, Object> metadata = Map.of(
                    "userId", user.getId(),
                    "email", user.getEmail(),
                    "role", user.getRole().getName()
            );
            
            activityService.recordActivity(
                    ActivityType.USER_REGISTRATION,
                    "User Registration",
                    description,
                    user,
                    "USER",
                    user.getId(),
                    metadata
            );
            
            log.info("Recorded user registration activity for user {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to record user registration activity", e);
        }
    }
    
    /**
     * Record activity for vehicle status changes
     */
    public void recordVehicleStatusChanged(Long vehicleId, String vehicleName, String oldStatus, String newStatus, User updatedBy) {
        try {
            String description = String.format("Vehicle %s status changed from %s to %s", 
                    vehicleName, oldStatus, newStatus);
            
            Map<String, Object> metadata = Map.of(
                    "vehicleId", vehicleId,
                    "vehicleName", vehicleName,
                    "oldStatus", oldStatus,
                    "newStatus", newStatus,
                    "updatedBy", updatedBy != null ? updatedBy.getFullName() : "System"
            );
            
            activityService.recordActivity(
                    ActivityType.VEHICLE_STATUS_CHANGED,
                    "Vehicle Status Updated",
                    description,
                    updatedBy,
                    "VEHICLE",
                    vehicleId,
                    metadata
            );
            
            log.info("Recorded vehicle status change activity for vehicle {}", vehicleId);
        } catch (Exception e) {
            log.error("Failed to record vehicle status change activity", e);
        }
    }
    
    /**
     * Record activity for user role changes
     */
    public void recordUserRoleChanged(User user, String oldRole, String newRole, User changedBy) {
        try {
            String description = String.format("User %s role changed from %s to %s", 
                    user.getFullName(), oldRole, newRole);
            
            Map<String, Object> metadata = Map.of(
                    "userId", user.getId(),
                    "email", user.getEmail(),
                    "oldRole", oldRole,
                    "newRole", newRole,
                    "changedBy", changedBy != null ? changedBy.getFullName() : "System"
            );
            
            activityService.recordActivity(
                    ActivityType.ADMIN_ACTION,
                    "User Role Updated",
                    description,
                    changedBy,
                    "USER",
                    user.getId(),
                    metadata
            );
            
            log.info("Recorded user role change activity for user {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to record user role change activity", e);
        }
    }
    
    /**
     * Record activity for vehicle creation
     */
    public void recordVehicleCreated(Long vehicleId, String vehicleName, User createdBy) {
        try {
            String description = String.format("New vehicle %s added to the fleet", vehicleName);
            
            Map<String, Object> metadata = Map.of(
                    "vehicleId", vehicleId,
                    "vehicleName", vehicleName,
                    "createdBy", createdBy != null ? createdBy.getFullName() : "System"
            );
            
            activityService.recordActivity(
                    ActivityType.VEHICLE_ADDED,
                    "Vehicle Added",
                    description,
                    createdBy,
                    "VEHICLE",
                    vehicleId,
                    metadata
            );
            
            log.info("Recorded vehicle creation activity for vehicle {}", vehicleId);
        } catch (Exception e) {
            log.error("Failed to record vehicle creation activity", e);
        }
    }
    
    /**
     * Record activity for admin testimonial actions
     */
    public void recordTestimonialAction(String action, Long testimonialId, String testimonialTitle, User actionBy) {
        try {
            String description = String.format("Testimonial '%s' was %s", testimonialTitle, action);
            
            Map<String, Object> metadata = Map.of(
                    "testimonialId", testimonialId,
                    "testimonialTitle", testimonialTitle,
                    "action", action,
                    "actionBy", actionBy != null ? actionBy.getFullName() : "System"
            );
            
            activityService.recordActivity(
                    ActivityType.ADMIN_ACTION,
                    "Testimonial " + action,
                    description,
                    actionBy,
                    "TESTIMONIAL",
                    testimonialId,
                    metadata
            );
            
            log.info("Recorded testimonial {} activity for testimonial {}", action, testimonialId);
        } catch (Exception e) {
            log.error("Failed to record testimonial action activity", e);
        }
    }
    
    /**
     * Record activity for admin broadcast notifications
     */
    public void recordBroadcastNotification(String message, int userCount, User sentBy) {
        try {
            String description = String.format("Broadcast notification sent to %d users", userCount);
            
            Map<String, Object> metadata = Map.of(
                    "message", message,
                    "userCount", userCount,
                    "sentBy", sentBy != null ? sentBy.getFullName() : "System"
            );
            
            activityService.recordActivity(
                    ActivityType.ADMIN_ACTION,
                    "Broadcast Notification",
                    description,
                    sentBy,
                    "NOTIFICATION",
                    null,
                    metadata
            );
            
            log.info("Recorded broadcast notification activity for {} users", userCount);
        } catch (Exception e) {
            log.error("Failed to record broadcast notification activity", e);
        }
    }
    
    /**
     * Record activity for refund processing
     */
    public void recordRefundActivity(String title, String description, User processedBy, Long refundId, Map<String, Object> metadata) {
        try {
            activityService.recordActivity(
                    ActivityType.REFUND_PROCESSED,
                    title,
                    description,
                    processedBy,
                    "REFUND",
                    refundId,
                    metadata
            );
            
            log.info("Recorded refund activity for refund {}", refundId);
        } catch (Exception e) {
            log.error("Failed to record refund activity", e);
        }
    }
}
