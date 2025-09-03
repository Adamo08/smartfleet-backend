package com.adamo.vrspfab.testimonials;

import com.adamo.vrspfab.common.SecurityRules;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class TestimonialSecurityRules implements SecurityRules {
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        registry
                // Publicly accessible endpoints
                .requestMatchers(HttpMethod.GET, "/testimonials/public").permitAll()
                .requestMatchers(HttpMethod.GET, "/testimonials/public/**").permitAll()
                
                // Authenticated user endpoints
                .requestMatchers(HttpMethod.GET, "/testimonials/my").authenticated()
                .requestMatchers(HttpMethod.POST, "/testimonials").authenticated()
                
                // Admin-only endpoints
                .requestMatchers(HttpMethod.GET, "/testimonials").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/testimonials/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/testimonials/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/testimonials/{id}/approve").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/testimonials/{id}").hasRole("ADMIN")
                
                // Admin-specific testimonial endpoints
                .requestMatchers("/admin/testimonials/**").hasRole("ADMIN");
    }
}