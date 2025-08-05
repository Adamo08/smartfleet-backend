package com.adamo.vrspfab.favorites;

import com.adamo.vrspfab.common.SecurityRules;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class FavoriteSecurityRules implements SecurityRules {
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        registry
                .requestMatchers(HttpMethod.GET, "/favorites/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/favorites").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.DELETE, "/favorites/**").hasRole("CUSTOMER");
    }
}