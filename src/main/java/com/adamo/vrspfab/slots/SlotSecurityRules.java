package com.adamo.vrspfab.slots;

import com.adamo.vrspfab.common.SecurityRules;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class SlotSecurityRules implements SecurityRules {
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        registry
                // Publicly accessible endpoint for available slots by vehicle (for customers to browse)
                .requestMatchers(HttpMethod.GET, "/slots/vehicle/{vehicleId}/available").permitAll()
                // All other GET operations on /slots/** require authentication (handled by service for ADMIN-only access)
                .requestMatchers(HttpMethod.GET, "/slots/**").authenticated()
                // POST, PUT, DELETE operations are restricted to ADMINs
                .requestMatchers(HttpMethod.POST, "/slots").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/slots/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/slots/**").hasRole("ADMIN");
    }
}
