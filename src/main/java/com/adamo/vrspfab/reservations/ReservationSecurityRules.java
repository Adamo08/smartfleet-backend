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
                .requestMatchers(HttpMethod.GET, "/reservations/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/reservations").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.PUT, "/reservations/**").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.DELETE, "/reservations/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/reservations/{id}/payment").authenticated();
    }
}