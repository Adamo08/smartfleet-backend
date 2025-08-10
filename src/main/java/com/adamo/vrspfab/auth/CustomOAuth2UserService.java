package com.adamo.vrspfab.auth;

import com.adamo.vrspfab.users.Role;
import com.adamo.vrspfab.users.User;
import com.adamo.vrspfab.users.UserRepository;
import com.adamo.vrspfab.users.AuthProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.debug("CustomOAuth2UserService.loadUser start; registrationId={}",
                userRequest.getClientRegistration().getRegistrationId());
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oauthUser = delegate.loadUser(userRequest);

        Map<String, Object> attrs = oauthUser.getAttributes();
        log.debug("OAuth provider attributes: {}", attrs);

        String reg = userRequest.getClientRegistration().getRegistrationId().toLowerCase();
        String email = null;
        try {
            if ("google".equals(reg)) {
                email = (String) attrs.get("email");
            } else if ("facebook".equals(reg)) {
                email = (String) attrs.get("email"); // facebook returns email when scope provided and user allowed it
            } else {
                email = (String) attrs.get("email");
            }
        } catch (Exception e) {
            log.warn("Error extracting email from attributes: {}", e.getMessage(), e);
        }

        log.debug("Extracted email: {}", email);

        if (email == null) {
            log.warn("Email is null from provider (registrationId={}). Attributes: {}", reg, attrs);
            // return oauthUser anyway — let success handler decide (or you can throw)
            return oauthUser;
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            log.debug("Existing user found for email={}", email);
            User user = userOpt.get();
            // Optionally update provider info if not set
            if (user.getAuthProvider() == null) {
                user.setAuthProvider(AuthProvider.valueOf(reg.toUpperCase()));
                userRepository.save(user);
            }
        } else {
            log.debug("No user found for email={}, will create a new user", email);
            User newUser = User.builder()
                    .email(email)
                    .firstName((String) attrs.getOrDefault("given_name", attrs.get("first_name")))
                    .lastName((String) attrs.getOrDefault("family_name", attrs.get("last_name")))
                    .role(Role.CUSTOMER)
                    .authProvider(AuthProvider.valueOf(reg.toUpperCase()))
                    .providerId(String.valueOf(attrs.get(userRequest.getClientRegistration()
                            .getProviderDetails()
                            .getUserInfoEndpoint()
                            .getUserNameAttributeName())))
                    .build();
            userRepository.save(newUser);
            log.info("Created new user for email={}", email);
        }

        return oauthUser;
    }
}
