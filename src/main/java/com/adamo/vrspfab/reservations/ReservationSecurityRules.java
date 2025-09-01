package com.adamo.vrspfab.reservations;

import com.adamo.vrspfab.common.SecurityRules;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class ReservationSecurityRules implements SecurityRules {
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        registry
                // Publicly accessible endpoint for available slots by vehicle
                .requestMatchers(HttpMethod.GET, "/reservations/vehicles/{vehicleId}/available-slots").permitAll()
                // Rules for Authenticated Users (Customers)
                .requestMatchers(HttpMethod.POST, "/reservations").authenticated()
                .requestMatchers(HttpMethod.GET, "/reservations").authenticated()
                .requestMatchers(HttpMethod.GET, "/reservations/filtered").authenticated()
                .requestMatchers(HttpMethod.GET, "/reservations/{id}").authenticated()
                .requestMatchers(HttpMethod.POST, "/reservations/{id}/cancel").authenticated()
                // Rules for Administrators
                .requestMatchers("/admin/reservations/**").hasRole("ADMIN");
    }
}