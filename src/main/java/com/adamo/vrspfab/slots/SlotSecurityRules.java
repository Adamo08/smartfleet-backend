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
                .requestMatchers(HttpMethod.GET, "/slots/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/slots").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/slots/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/slots/**").hasRole("ADMIN");
    }
}