package com.adamo.vrspfab.auth;

import com.adamo.vrspfab.common.ErrorDto;
import com.adamo.vrspfab.users.UserDto;
import com.adamo.vrspfab.users.UserMapper;
import com.adamo.vrspfab.users.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.time.LocalDateTime;

@AllArgsConstructor
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "APIs for user authentication and authorization")
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
    @Operation(summary = "User login",
               description = "Authenticates a user and returns JWT access and refresh tokens.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Login successful"),
                       @ApiResponse(responseCode = "401", description = "Invalid credentials"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
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
    @Operation(summary = "Forgot password",
               description = "Initiates the password reset process by sending a reset email to the user.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Password reset email sent successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid email format"),
                       @ApiResponse(responseCode = "404", description = "User not found for the given email"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
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
    @Operation(summary = "Reset password",
               description = "Resets the user's password using a valid reset token.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Password reset successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid token or password"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Password reset successfully");
    }

    /**
     * This method handles OAuth login initiation.
     * Redirects user to OAuth provider for authentication.
     */
    @Operation(summary = "Initiate OAuth login",
               description = "Redirects the user to the specified OAuth provider's authorization endpoint.",
               responses = {
                       @ApiResponse(responseCode = "302", description = "Redirect to OAuth provider"),
                       @ApiResponse(responseCode = "400", description = "Invalid OAuth provider or redirect URI"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/oauth/{provider}")
    public void initiateOAuth(
            @PathVariable String provider,
            @RequestParam String redirect_uri,
            HttpServletResponse response) throws Exception {
        // This will be handled by Spring Security OAuth2
        // The user will be redirected to the OAuth provider
        // After successful authentication, they'll be redirected back to the callback URL
        response.sendRedirect("/oauth2/authorization/" + provider);
    }

    @Operation(summary = "Get current authenticated user",
               description = "Retrieves the details of the currently authenticated user.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved user details"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, no authenticated user"),
                       @ApiResponse(responseCode = "404", description = "User not found (should not happen for authenticated user)"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
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
    @Operation(summary = "Refresh access token",
               description = "Exchanges a valid refresh token for a new JWT access token.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Access token refreshed successfully"),
                       @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PostMapping("/refresh")
    public JwtResponse refresh(@CookieValue(value = "refreshToken") String refreshToken) {
        var accessToken = authService.refreshAccessToken(refreshToken);
        return new JwtResponse(accessToken.toString());
    }
}