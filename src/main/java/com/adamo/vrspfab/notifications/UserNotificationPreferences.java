package com.adamo.vrspfab.notifications;

import com.adamo.vrspfab.users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_notification_preferences")
public class UserNotificationPreferences {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @Column(name = "real_time_enabled", nullable = false)
    private boolean realTimeEnabled = true;

    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled = true;
}