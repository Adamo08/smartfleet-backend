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
                
                // Publicly accessible endpoints for vehicle brands, categories, and models
                .requestMatchers(HttpMethod.GET, "/vehicles/brands/active").permitAll()
                .requestMatchers(HttpMethod.GET, "/vehicles/categories/active").permitAll()
                .requestMatchers(HttpMethod.GET, "/vehicles/models/active").permitAll()
                .requestMatchers(HttpMethod.GET, "/vehicles/models/active/brand/{brandId}").permitAll()

                // Admin-only access for creating, updating, deleting, and viewing reservations
                .requestMatchers(HttpMethod.GET, "/vehicles/{id}/reservations").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/vehicles").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/vehicles/bulk").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/vehicles/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/vehicles/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/vehicles/{id}/status").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/vehicles/{id}/mileage").hasRole("ADMIN")
                
                // Enhanced Admin Vehicle Management - all CRUD operations
                .requestMatchers("/admin/vehicles/**").hasRole("ADMIN")
                
                // Admin-only access for vehicle categories
                .requestMatchers(HttpMethod.GET, "/admin/vehicle-categories").permitAll() // Allow public access to view categories
                .requestMatchers("/admin/vehicle-categories/**").hasRole("ADMIN")
                
                // Admin-only access for vehicle brands
                .requestMatchers(HttpMethod.GET, "/admin/vehicle-brands").permitAll() // Allow public access to view brands
                .requestMatchers("/admin/vehicle-brands/**").hasRole("ADMIN")
                
                // Admin-only access for vehicle models
                .requestMatchers(HttpMethod.GET, "/admin/vehicle-models").permitAll() // Allow public access to view models
                .requestMatchers("/admin/vehicle-models/**").hasRole("ADMIN");
    }
}