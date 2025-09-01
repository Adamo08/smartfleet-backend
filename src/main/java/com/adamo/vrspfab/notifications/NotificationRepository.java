package com.adamo.vrspfab.notifications;

import com.adamo.vrspfab.users.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {

    /**
     * Finds a notification by its ID and the user it belongs to.
     * This is crucial for security checks to ensure a user can only access their own notifications.
     */
    Optional<Notification> findByIdAndUser(Long id, User user);

    /**
     * Retrieves a paginated list of notifications for a specific user, ordered by creation date descending.
     */
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Finds all unread notifications for a specific user.
     */
    List<Notification> findAllByUserAndReadFalse(User user);
}