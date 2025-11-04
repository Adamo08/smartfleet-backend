package com.adamo.vrspfab.auth;

import com.adamo.vrspfab.users.User;
import com.adamo.vrspfab.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;
    @Mock private SecurityContext securityContext;

    @InjectMocks private AuthService authService;

    private User user;
    private LoginRequest loginRequest;

    @BeforeEach
    void setup() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encoded")
                .build();
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void login_whenAuthenticationFails_throwsBadCredentials() {
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_whenUserNotFound_throwsException() {
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).willReturn(null);
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.empty());

        assertThrows(Exception.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_whenValid_returnsTokens() {
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).willReturn(null);
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(jwtService.generateAccessToken(user)).willReturn(org.mockito.Mockito.mock(Jwt.class));
        given(jwtService.generateRefreshToken(user)).willReturn(org.mockito.Mockito.mock(Jwt.class));

        LoginResponse response = authService.login(loginRequest);
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
    }

    @Test
    void refreshAccessToken_whenInvalidToken_throwsBadCredentials() {
        given(jwtService.parseToken("invalid-token")).willReturn(null);

        assertThrows(BadCredentialsException.class, () -> authService.refreshAccessToken("invalid-token"));
    }

    @Test
    void refreshAccessToken_whenExpired_throwsBadCredentials() {
        Jwt expiredJwt = org.mockito.Mockito.mock(Jwt.class);
        given(expiredJwt.isExpired()).willReturn(true);
        given(jwtService.parseToken("expired-token")).willReturn(expiredJwt);

        assertThrows(BadCredentialsException.class, () -> authService.refreshAccessToken("expired-token"));
    }

    @Test
    void refreshAccessToken_whenUserNotFound_throwsException() {
        Jwt validJwt = org.mockito.Mockito.mock(Jwt.class);
        given(validJwt.isExpired()).willReturn(false);
        given(validJwt.getUserId()).willReturn(999L);
        given(jwtService.parseToken("valid-token")).willReturn(validJwt);
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThrows(Exception.class, () -> authService.refreshAccessToken("valid-token"));
    }
}

