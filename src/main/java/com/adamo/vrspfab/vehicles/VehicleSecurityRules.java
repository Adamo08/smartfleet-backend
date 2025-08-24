package com.adamo.vrspfab.vehicles;

import com.adamo.vrspfab.common.SecurityRules;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class VehicleSecurityRules implements SecurityRules {
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        registry
                // Publicly accessible endpoints for browsing vehicles
                .requestMatchers(HttpMethod.GET, "/vehicles/search").permitAll()
                .requestMatchers(HttpMethod.GET, "/vehicles/year/{startYear}/{endYear}").permitAll()
                .requestMatchers(HttpMethod.GET, "/vehicles/mileage/{minMileage}/{maxMileage}").permitAll()
                .requestMatchers(HttpMethod.GET, "/vehicles").permitAll()
                .requestMatchers(HttpMethod.GET, "/vehicles/{id}").permitAll()
                .requestMatchers(HttpMethod.GET, "/vehicles/{id}/availability").permitAll()

                // Admin-only access for creating, updating, deleting, and viewing reservations
                .requestMatchers(HttpMethod.GET, "/vehicles/{id}/reservations").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/vehicles").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/vehicles/bulk").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/vehicles/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/vehicles/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/vehicles/{id}/status").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/vehicles/{id}/mileage").hasRole("ADMIN")
                
                // Admin-only access for vehicle categories
                .requestMatchers("/admin/vehicle-categories/**").hasRole("ADMIN")
                
                // Admin-only access for vehicle brands
                .requestMatchers("/admin/vehicle-brands/**").hasRole("ADMIN")
                
                // Admin-only access for vehicle models
                .requestMatchers("/admin/vehicle-models/**").hasRole("ADMIN");
    }
}