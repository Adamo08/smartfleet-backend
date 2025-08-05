package com.adamo.vrspfab.bookmarks;

import com.adamo.vrspfab.common.SecurityRules;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class BookmarkSecurityRules implements SecurityRules {
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        registry
                .requestMatchers(HttpMethod.GET, "/bookmarks/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/bookmarks").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.DELETE, "/bookmarks/**").hasRole("CUSTOMER");
    }
}