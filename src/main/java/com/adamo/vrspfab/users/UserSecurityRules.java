package com.adamo.vrspfab.users;

import com.adamo.vrspfab.common.SecurityRules;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class UserSecurityRules implements SecurityRules {
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        registry
                // User registration is publicly accessible
                .requestMatchers(HttpMethod.POST, "/users").permitAll()
                
                // User management endpoints - ADMIN only
                .requestMatchers(HttpMethod.GET, "/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/users/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/users/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/users/{id}/role").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/users/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/users/{id}/change-password").hasRole("ADMIN");
    }
}
