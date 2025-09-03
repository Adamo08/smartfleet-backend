package com.adamo.vrspfab.notifications;

import com.adamo.vrspfab.common.SecurityRules;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class NotificationSecurityRules implements SecurityRules {
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        registry
                // Admin-specific endpoints
                .requestMatchers(HttpMethod.POST, "/notifications/broadcast").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/notifications/admin").hasRole("ADMIN")
                
                // Enhanced notification admin endpoints
                .requestMatchers("/api/admin/notifications/**").hasRole("ADMIN")
                
                // User notification preferences endpoints
                .requestMatchers("/user/preferences/notifications/**").authenticated()
                
                // Authenticated user endpoints
                .requestMatchers(HttpMethod.GET, "/notifications").authenticated()
                .requestMatchers(HttpMethod.PATCH, "/notifications/{id}/read").authenticated()
                .requestMatchers(HttpMethod.POST, "/notifications/mark-all-as-read").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/notifications/{id}").authenticated()
                .requestMatchers(HttpMethod.POST, "/notifications/test").authenticated();
    }
}