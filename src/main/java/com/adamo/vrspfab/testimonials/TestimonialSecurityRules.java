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
                .requestMatchers(HttpMethod.GET, "/testimonials/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/testimonials").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.PUT, "/testimonials/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/testimonials/**").hasRole("ADMIN");
    }
}