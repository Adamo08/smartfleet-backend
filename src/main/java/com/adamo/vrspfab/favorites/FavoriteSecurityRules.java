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
                // Authenticated users can view their OWN favorites
                .requestMatchers(HttpMethod.GET, "/favorites/my").authenticated()
                // Specific favorite by ID: Authenticated user (owner or admin)
                // The fine-grained access control for /favorites/{id} is handled in FavoriteService
                .requestMatchers(HttpMethod.GET, "/favorites/{id}").authenticated()
                // Only ADMINs can view all favorites (with filters)
                .requestMatchers(HttpMethod.GET, "/favorites").hasRole("ADMIN")
                // Customers can create favorites (service will ensure it's for themselves and not a duplicate)
                .requestMatchers(HttpMethod.POST, "/favorites").hasRole("CUSTOMER")
                // Customers can delete their own favorites (service will ensure ownership)
                // Admins can also delete any favorite
                .requestMatchers(HttpMethod.DELETE, "/favorites/**").authenticated(); // Authenticated user can delete (service checks ownership/admin)
    }
}
