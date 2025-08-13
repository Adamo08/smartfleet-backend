package com.adamo.vrspfab.auth;

import com.adamo.vrspfab.users.AuthProvider;
import com.adamo.vrspfab.users.Role;
import com.adamo.vrspfab.users.User;
import com.adamo.vrspfab.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    
    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        log.debug("OAuth2 success handler invoked. Authentication class: {}", authentication.getClass().getName());

        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            log.error("Authentication is not OAuth2AuthenticationToken, but: {}", authentication.getClass().getName());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected authentication type");
            return;
        }

        OAuth2User oauthUser = oauthToken.getPrincipal();
        Map<String, Object> attrs = oauthUser.getAttributes();
        log.debug("OAuth2 principal attributes: {}", attrs);

        String email = (String) attrs.get("email");
        log.debug("Email from oauth attributes: {}", email);

        if (email == null || email.isBlank()) {
            log.warn("No email present in OAuth attributes, redirecting to frontend with error. Attributes: {}", attrs);
            String errorUrl = frontendUrl + "/auth/oauth-callback?error=email_not_provided&provider=" + 
                            oauthToken.getAuthorizedClientRegistrationId();
            response.sendRedirect(errorUrl);
            return;
        }

        // Determine provider (e.g. "google", "facebook") and convert to AuthProvider enum
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
        log.debug("OAuth2 registrationId (provider): {}", registrationId);
        AuthProvider provider;
        try {
            provider = AuthProvider.valueOf(registrationId.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown OAuth2 provider: {}", registrationId);
            String errorUrl = frontendUrl + "/auth/oauth-callback?error=unsupported_provider&provider=" + registrationId;
            response.sendRedirect(errorUrl);
            return;
        }

        // Extract provider user id ("sub" for Google, "id" for Facebook)
        String providerId;
        if (attrs.containsKey("sub")) {
            providerId = (String) attrs.get("sub");
        } else if (attrs.containsKey("id")) {
            providerId = String.valueOf(attrs.get("id"));
        } else {
            providerId = null;
        }
        log.debug("Provider user ID: {}", providerId);

        // Find or create user safely with provider details
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    log.info("Creating user for oauth email={}", email);
                    User newUser = User.builder()
                            .email(email)
                            .firstName((String) attrs.getOrDefault("given_name", attrs.get("first_name")))
                            .lastName((String) attrs.getOrDefault("family_name", attrs.get("last_name")))
                            .role(Role.CUSTOMER)
                            .authProvider(provider)     // set provider enum
                            .providerId(providerId)     // set provider user id
                            .build();
                    return userRepository.save(newUser);
                });

        // If user exists but provider info missing, update it (optional)
        if (user.getAuthProvider() == null || user.getProviderId() == null) {
            user.setAuthProvider(provider);
            user.setProviderId(providerId);
            userRepository.save(user);
            log.info("Updated existing user with provider info: id={}, provider={}, providerId={}", user.getId(), provider, providerId);
        }

        log.debug("User resolved from DB: id={}, email={}", user.getId(), user.getEmail());

        // generate tokens using your existing JwtService (returns Jwt object)
        String accessToken = jwtService.generateAccessToken(user).toString();
        String refreshToken = jwtService.generateRefreshToken(user).toString();

        log.debug("Generated accessToken length={} refreshToken length={}", accessToken.length(), refreshToken.length());

        // Redirect to frontend with tokens as query parameters
        String redirectUrl = frontendUrl + "/auth/oauth-callback?" +
                           "accessToken=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8) +
                           "&refreshToken=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8) +
                           "&provider=" + registrationId +
                           "&email=" + URLEncoder.encode(email, StandardCharsets.UTF_8);

        log.info("Redirecting to frontend OAuth callback for user id={}, email={}", user.getId(), user.getEmail());
        response.sendRedirect(redirectUrl);
    }
}
