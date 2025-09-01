package com.adamo.vrspfab.users.events;

import com.adamo.vrspfab.users.Role;
import com.adamo.vrspfab.users.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserRoleChangedEvent extends ApplicationEvent {
    private final User user;
    private final Role oldRole;
    private final Role newRole;

    public UserRoleChangedEvent(Object source, User user, Role oldRole, Role newRole) {
        super(source);
        this.user = user;
        this.oldRole = oldRole;
        this.newRole = newRole;
    }
}
