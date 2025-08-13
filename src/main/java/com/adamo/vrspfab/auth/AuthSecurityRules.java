package com.adamo.vrspfab.auth;

import com.adamo.vrspfab.common.SecurityRules;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class AuthSecurityRules implements SecurityRules {

    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        registry
                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/refresh").permitAll()

                // New: Forgot/reset password endpoints
                .requestMatchers(HttpMethod.POST, "/auth/forgot-password").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/reset-password").permitAll()

                // New: OAuth initiation endpoint
                .requestMatchers(HttpMethod.GET, "/auth/oauth/**").permitAll()

                // Existing OAuth2 login rules
                .requestMatchers(HttpMethod.GET, "/oauth2/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/oauth2/authorization/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/login/oauth2/**").permitAll();
    }
}
