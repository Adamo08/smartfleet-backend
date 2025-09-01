package com.adamo.vrspfab.notifications;

import com.adamo.vrspfab.common.SecurityUtilsService;
import com.adamo.vrspfab.dashboard.ActivityEventListener;
import com.adamo.vrspfab.users.User;
import com.adamo.vrspfab.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;
    private final SecurityUtilsService securityUtilsService;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailService emailService;
    private final ActivityEventListener activityEventListener;

    /**
     * The main method for creating and dispatching a notification.
     * It saves the notification to the DB, then sends it via enabled channels based on user preferences.
     */
    @Transactional
    public void createAndDispatchNotification(User user, NotificationType type, String message) {
        createAndDispatchNotification(user, type, message, null);
    }

    /**
     * Overload that accepts extra template model data for email rendering.
     */
    @Transactional
    public void createAndDispatchNotification(User user, NotificationType type, String message, Map<String, Object> extraModel) {
        log.info("=== NotificationService: Creating and Dispatching Notification ===");
        log.info("User: {} ({})", user.getEmail(), user.getId());
        log.info("Type: {}", type);
        log.info("Message: {}", message);
        
        // Step 1: Save the notification to the database (source of truth)
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setMessage(message);
        notification.setRead(false);
        Notification savedNotification = notificationRepository.save(notification);
        log.info("✅ Saved notification ID {} for user {}", savedNotification.getId(), user.getEmail());

        // Step 2: Fetch user preferences (or use defaults)
        UserNotificationPreferences preferences = user.getNotificationPreferences();
        if (preferences == null) {
            log.warn("⚠️ User {} has no preferences record, using defaults (true/true)", user.getEmail());
            preferences = new UserNotificationPreferences(); // Default preferences are true
        }
        
        log.info("User preferences - RealTime: {}, Email: {}", 
                preferences.isRealTimeEnabled(), preferences.isEmailEnabled());

        NotificationDto dto = notificationMapper.toDto(savedNotification);
        log.info("Notification DTO created: {}", dto);

        // Step 3: Dispatch via Real-Time Channel
        if (preferences.isRealTimeEnabled()) {
            String destination = "/user/" + user.getEmail() + "/queue/notifications";
            log.info("🚀 Attempting to send real-time notification to destination: {}", destination);
            
            try {
                messagingTemplate.convertAndSendToUser(
                        user.getEmail(), "/queue/notifications", dto
                );
                log.info("✅ Successfully dispatched real-time notification ID {} to user {} at destination: {}", 
                        savedNotification.getId(), user.getEmail(), destination);
            } catch (Exception e) {
                log.error("❌ Failed to dispatch real-time notification to user {}: {}", user.getEmail(), e.getMessage(), e);
            }
        } else {
            log.info("⏭️ Skipping real-time notification for user {} (disabled in preferences)", user.getEmail());
        }

        // Step 4: Dispatch via Email Channel
        if (preferences.isEmailEnabled()) {
            log.info("📧 Sending email notification to user: {}", user.getEmail());
            TemplateInfo templateInfo = resolveTemplate(type);
            Map<String, Object> emailModel = new HashMap<>();
            emailModel.put("username", user.getFirstName());
            emailModel.put("message", message);
            emailModel.put("notificationType", type.toString());
            emailModel.put("subject", templateInfo.subject);
            if (extraModel != null) {
                emailModel.putAll(extraModel);
            }
            try {
                emailService.sendNotificationEmail(user.getEmail(), templateInfo.subject, templateInfo.templateName, emailModel);
                log.info("✅ Email notification sent successfully to user: {}", user.getEmail());
            } catch (Exception e) {
                log.error("❌ Failed to send email notification to user {}: {}", user.getEmail(), e.getMessage(), e);
            }
        } else {
            log.info("⏭️ Skipping email notification for user {} (disabled in preferences)", user.getEmail());
        }
    }

    /**
     * Creates and dispatches a notification but only via real-time channel (no email), while still persisting it.
     */
    @Transactional
    public void createAndDispatchRealTimeOnly(User user, NotificationType type, String message) {
        createAndDispatchRealTimeOnly(user, type, message, null);
    }

    @Transactional
    public void createAndDispatchRealTimeOnly(User user, NotificationType type, String message, Map<String, Object> extraModel) {
        log.info("=== NotificationService: Real-Time Only Notification ===");
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setMessage(message);
        notification.setRead(false);
        Notification saved = notificationRepository.save(notification);

        UserNotificationPreferences preferences = user.getNotificationPreferences();
        if (preferences == null) {
            preferences = new UserNotificationPreferences();
        }
        if (preferences.isRealTimeEnabled()) {
            try {
                messagingTemplate.convertAndSendToUser(user.getEmail(), "/queue/notifications", notificationMapper.toDto(saved));
            } catch (Exception e) {
                log.error("Failed to dispatch real-time only notification: {}", e.getMessage(), e);
            }
        }
    }

    private TemplateInfo resolveTemplate(NotificationType type) {
        // Map notification types to specific templates and subjects. Fallback to generic template.
        return switch (type) {
            case PAYMENT_SUCCESS -> new TemplateInfo("payment-success-email", "Payment Successful");
            case PAYMENT_FAILURE -> new TemplateInfo("payment-failure-email", "Payment Failed");
            case REFUND_ISSUED -> new TemplateInfo("refund-issued-email", "Refund Issued");
            case RESERVATION_PENDING -> new TemplateInfo("reservation-pending-email", "Reservation Pending");
            case RESERVATION_CONFIRMED -> new TemplateInfo("reservation-confirmed-email", "Reservation Confirmed");
            case RESERVATION_CANCELLED -> new TemplateInfo("reservation-cancelled-email", "Reservation Cancelled");
            default -> new TemplateInfo("notification-email", "You have a new notification: " + type);
        };
    }

    private static class TemplateInfo {
        final String templateName;
        final String subject;
        TemplateInfo(String templateName, String subject) {
            this.templateName = templateName;
            this.subject = subject;
        }
    }

    /**
     * Admin action: Broadcast a notification to all users.
     */
    @Transactional
    public void broadcastNotification(AdminNotificationRequest request) {
        log.info("=== NotificationService: Broadcasting Notification ===");
        log.info("Type: {}", request.getType());
        log.info("Message: {}", request.getMessage());
        
        List<User> allUsers = userRepository.findAll();
        log.info("📢 Broadcasting notification to {} users", allUsers.size());
        
        int successCount = 0;
        int failureCount = 0;
        
        for (User user : allUsers) {
            if (user == null || user.getEmail() == null) {
                log.warn("⚠️ Skipping user with null email");
                failureCount++;
                continue; // Skip users without an email
            }

            log.info("Processing user: {} ({})", user.getEmail(), user.getId());
            
            try {
                // Using a separate transaction for each user might be better for large user bases
                createAndDispatchNotification(user, request.getType(), request.getMessage());
                successCount++;
                log.info("✅ Successfully processed notification for user: {}", user.getEmail());
            } catch (Exception e) {
                failureCount++;
                log.error("❌ Failed to process notification for user {}: {}", user.getEmail(), e.getMessage(), e);
            }
        }
        
        log.info("📊 Broadcast completed - Success: {}, Failures: {}", successCount, failureCount);
        
        // Record broadcast notification activity
        try {
            User currentUser = securityUtilsService.getCurrentAuthenticatedUser();
            activityEventListener.recordBroadcastNotification(request.getMessage(), successCount, currentUser);
        } catch (Exception e) {
            log.warn("Could not record broadcast notification activity: {}", e.getMessage());
        }
    }

    /**
     * Admin action: Notify all administrators about an event.
     */
    @Transactional
    public void notifyAllAdmins(NotificationType type, String message, Map<String, Object> extraModel) {
        log.info("=== NotificationService: Notifying All Admins ===");
        log.info("Type: {}", type);
        log.info("Message: {}", message);
        
        List<User> adminUsers = userRepository.findByRole(com.adamo.vrspfab.users.Role.ADMIN);
        log.info("📢 Notifying {} admin users", adminUsers.size());
        
        int successCount = 0;
        int failureCount = 0;
        
        for (User admin : adminUsers) {
            if (admin == null || admin.getEmail() == null) {
                log.warn("⚠️ Skipping admin with null email");
                failureCount++;
                continue;
            }

            log.info("Processing admin: {} ({})", admin.getEmail(), admin.getId());
            
            try {
                createAndDispatchNotification(admin, type, message, extraModel);
                successCount++;
                log.info("✅ Successfully sent notification to admin: {}", admin.getEmail());
            } catch (Exception e) {
                failureCount++;
                log.error("❌ Failed to send notification to admin {}: {}", admin.getEmail(), e.getMessage(), e);
            }
        }
        
        log.info("📊 Admin notification completed - Success: {}, Failures: {}", successCount, failureCount);
    }

    /**
     * Retrieves all notifications for the currently authenticated user.
     *
     * @param pageable Pagination information.
     * @return A paginated list of notification DTOs.
     */
    @Transactional(readOnly = true)
    public Page<NotificationDto> getNotificationsForCurrentUser(Pageable pageable) {
        User currentUser = securityUtilsService.getCurrentAuthenticatedUser();
        return notificationRepository.findByUserOrderByCreatedAtDesc(currentUser, pageable)
                .map(notificationMapper::toDto);
    }

    /**
     * Marks a specific notification as read.
     *
     * @param notificationId The ID of the notification to mark as read.
     * @return The updated notification DTO.
     */
    @Transactional
    public NotificationDto markNotificationAsRead(Long notificationId) {
        User currentUser = securityUtilsService.getCurrentAuthenticatedUser();
        Notification notification = notificationRepository.findByIdAndUser(notificationId, currentUser)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        notification.setRead(true);
        notificationRepository.save(notification);
        log.info("Marked notification ID {} as read for user {}", notificationId, currentUser.getEmail());
        return notificationMapper.toDto(notification);
    }

    /**
     * Marks all unread notifications for the current user as read.
     *
     * @return The number of notifications that were updated.
     */
    @Transactional
    public long markAllNotificationsAsRead() {
        User currentUser = securityUtilsService.getCurrentAuthenticatedUser();
        List<Notification> unreadNotifications = notificationRepository.findAllByUserAndReadFalse(currentUser);

        if (unreadNotifications.isEmpty()) {
            return 0;
        }

        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
        }

        notificationRepository.saveAll(unreadNotifications);
        log.info("Marked {} notifications as read for user {}", unreadNotifications.size(), currentUser.getEmail());
        return unreadNotifications.size();
    }

    /**
     * Deletes a notification.
     *
     * @param notificationId The ID of the notification to delete.
     */
    @Transactional
    public void deleteNotification(Long notificationId) {
        User currentUser = securityUtilsService.getCurrentAuthenticatedUser();
        Notification notification = notificationRepository.findByIdAndUser(notificationId, currentUser)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        notificationRepository.delete(notification);
        log.info("Deleted notification ID {} for user {}", notificationId, currentUser.getEmail());
    }


    /**
     * Helper method to get the current authenticated user.
     *
     * @return The current authenticated user.
     */
    public User getCurrentUser() {
        return securityUtilsService.getCurrentAuthenticatedUser();
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto> getAllNotificationsAdmin(NotificationFilter filter, Pageable pageable) {
        NotificationSpecification spec = new NotificationSpecification(filter);
        return notificationRepository.findAll(spec, pageable).map(notificationMapper::toDto);
    }
}