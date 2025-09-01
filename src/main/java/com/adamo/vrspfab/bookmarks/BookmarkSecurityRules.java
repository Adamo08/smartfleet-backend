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
                // Authenticated users can view their OWN bookmarks
                .requestMatchers(HttpMethod.GET, "/bookmarks/my").authenticated()
                // Specific bookmark by ID: Authenticated user (owner or admin)
                // The fine-grained access control for /bookmarks/{id} is handled in BookmarkService
                .requestMatchers(HttpMethod.GET, "/bookmarks/{id}").authenticated()
                // Only ADMINs can view all bookmarks (with filters)
                .requestMatchers(HttpMethod.GET, "/bookmarks").hasRole("ADMIN")
                // Customers can create bookmarks (service will ensure it's for themselves and not a duplicate)
                .requestMatchers(HttpMethod.POST, "/bookmarks").authenticated()
                // Authenticated users (owner or admin) can delete bookmarks (service will ensure ownership/admin)
                .requestMatchers(HttpMethod.DELETE, "/bookmarks/**").authenticated();
    }
}
