package com.adamo.vrspfab.common;

import com.adamo.vrspfab.users.User;
import com.adamo.vrspfab.users.UserService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * A utility service for retrieving information about the currently authenticated user
 * from the Spring Security context.
 */
@Service
@AllArgsConstructor
public class SecurityUtilsService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityUtilsService.class);

    private final UserService userService; // Inject UserService to fetch User entity

    /**
     * Retrieves the currently authenticated User entity from the Spring Security context.
     *
     * @return The authenticated {@link User} entity.
     * @throws AccessDeniedException if no user is authenticated or if the authenticated
     * user cannot be found in the database.
     */
    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("Attempted operation by unauthenticated user.");
            throw new AccessDeniedException("User not authenticated.");
        }

        // The principal can be a String (user ID from JWT) or a UserDetails object
        Object principal = authentication.getPrincipal();
        Long userId;

        if (principal instanceof String) {
            try {
                userId = Long.parseLong((String) principal);
            } catch (NumberFormatException e) {
                logger.error("Authentication principal is a String but not a valid Long ID: {}. Error: {}", principal, e.getMessage(), e);
                throw new AccessDeniedException("Invalid authentication principal format. Expected a numeric user ID.");
            }
        } else if (principal instanceof Long) {
            userId = (Long) principal;
        } else {
            logger.error("Authentication principal is of unexpected type: {}. Type: {}", principal, principal.getClass().getName());
            throw new AccessDeniedException("Unexpected authentication principal type.");
        }

        return userService.getUserById(userId)
                .orElseThrow(() -> {
                    logger.error("Authenticated user with ID '{}' not found in database.", userId);
                    return new AccessDeniedException("Authenticated user not found in database.");
                });
    }
}
