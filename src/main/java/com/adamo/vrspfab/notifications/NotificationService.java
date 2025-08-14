package com.adamo.vrspfab.notifications;

import com.adamo.vrspfab.common.SecurityUtilsService;
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

    /**
     * The main method for creating and dispatching a notification.
     * It saves the notification to the DB, then sends it via enabled channels based on user preferences.
     */
    @Transactional
    public void createAndDispatchNotification(User user, NotificationType type, String message) {
        // Step 1: Save the notification to the database (source of truth)
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setMessage(message);
        notification.setRead(false);
        Notification savedNotification = notificationRepository.save(notification);
        log.info("Saved notification ID {} for user {}", savedNotification.getId(), user.getEmail());

        // Step 2: Fetch user preferences (or use defaults)
        UserNotificationPreferences preferences = user.getNotificationPreferences();
        if (preferences == null) {
            log.warn("User {} has no preferences record, using defaults (true/true)", user.getEmail());
            preferences = new UserNotificationPreferences(); // Default preferences are true
        }

        NotificationDto dto = notificationMapper.toDto(savedNotification);

        // Step 3: Dispatch via Real-Time Channel
        if (preferences.isRealTimeEnabled()) {
            messagingTemplate.convertAndSendToUser(
                    user.getEmail(), "/queue/notifications", dto
            );
            log.info("Dispatched real-time notification ID {} to user {}", savedNotification.getId(), user.getEmail());
        }

        // Step 4: Dispatch via Email Channel
        if (preferences.isEmailEnabled()) {
            Map<String, Object> emailModel = Map.of(
                    "username", user.getFirstName(),
                    "message", message,
                    "notificationType", type.toString(),
                    "subject", "You have a new notification: " + type.toString()
            );
            emailService.sendNotificationEmail(user.getEmail(), (String)emailModel.get("subject"), "notification-email.html", emailModel);
        }
    }

    /**
     * Admin action: Broadcast a notification to all users.
     */
    @Transactional
    public void broadcastNotification(AdminNotificationRequest request) {
        List<User> allUsers = userRepository.findAll();
        log.info("Broadcasting notification to {} users", allUsers.size());
        for (User user : allUsers) {

            if (user == null || user.getEmail() == null) {
                log.warn("Skipping user with null email");
                continue; // Skip users without an email
            }

            // Using a separate transaction for each user might be better for large user bases
            createAndDispatchNotification(user, request.getType(), request.getMessage());
        }
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
}