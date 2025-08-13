package com.adamo.vrspfab.auth;

import com.adamo.vrspfab.users.UserDto;
import com.adamo.vrspfab.users.UserMapper;
import com.adamo.vrspfab.users.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import com.adamo.vrspfab.common.dto.ForgotPasswordRequest;
import com.adamo.vrspfab.common.dto.ResetPasswordRequest;
import jakarta.servlet.http.HttpServletRequest;

@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtConfig jwtConfig;
    private final UserMapper userMapper;
    private final AuthService authService;
    private final UserService userService;

    /**
     * This method handles user login requests.
     *
     * @return ResponseEntity indicating the result of the login operation.
     */
    @PostMapping("/login")
    public JwtResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        var loginResult = authService.login(request);

        var refreshToken = loginResult.getRefreshToken().toString();
        var cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/auth/refresh");
        cookie.setMaxAge(jwtConfig.getRefreshTokenExpiration());
        cookie.setSecure(true);
        response.addCookie(cookie);

        return new JwtResponse(loginResult.getAccessToken().toString());
    }

    /**
     * This method handles forgot password requests.
     *
     * @param request the forgot password request
     * @return ResponseEntity indicating the result
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.sendPasswordResetEmail(request.getEmail());
        return ResponseEntity.ok("Password reset email sent successfully");
    }

    /**
     * This method handles password reset requests.
     *
     * @param request the reset password request
     * @return ResponseEntity indicating the result
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Password reset successfully");
    }

    /**
     * This method handles OAuth login initiation.
     * Redirects user to OAuth provider for authentication.
     */
    @GetMapping("/oauth/{provider}")
    public void initiateOAuth(@PathVariable String provider, 
                             @RequestParam String redirect_uri,
                             HttpServletResponse response) throws Exception {
        // This will be handled by Spring Security OAuth2
        // The user will be redirected to the OAuth provider
        // After successful authentication, they'll be redirected back to the callback URL
        response.sendRedirect("/oauth2/authorization/" + provider);
    }

    /**
     * Custom OAuth callback endpoint for Google
     */
    @GetMapping("/oauth2/callback/google")
    public void googleOAuthCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Redirect to the default Spring Security OAuth2 callback
        // This will trigger our OAuth2AuthenticationSuccessHandler
        response.sendRedirect("/login/oauth2/code/google");
    }

    /**
     * Custom OAuth callback endpoint for Facebook
     */
    @GetMapping("/oauth2/callback/facebook")
    public void facebookOAuthCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Redirect to the default Spring Security OAuth2 callback
        // This will trigger our OAuth2AuthenticationSuccessHandler
        response.sendRedirect("/login/oauth2/code/facebook");
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me() {
        var user = authService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        var userDto = userMapper.toDto(user);
        return ResponseEntity.ok(userDto);
    }

    /**
     * This method handles the refresh token request.
     *
     * @param refreshToken the refresh token from the cookie
     * @return ResponseEntity with the new JWT response
     */
    @PostMapping("/refresh")
    public JwtResponse refresh(@CookieValue(value = "refreshToken") String refreshToken) {
        var accessToken = authService.refreshAccessToken(refreshToken);
        return new JwtResponse(accessToken.toString());
    }

    /**
     * This method handles BadCredentialsException thrown during authentication.
     *
     * @return ResponseEntity with status 401 (Unauthorized).
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> handleBadCredentialsException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // Unauthorized
    }
}