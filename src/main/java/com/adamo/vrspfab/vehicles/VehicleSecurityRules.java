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
                .requestMatchers(HttpMethod.GET, "/vehicles/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/vehicles").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/vehicles/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/vehicles/**").hasRole("ADMIN");
    }
}