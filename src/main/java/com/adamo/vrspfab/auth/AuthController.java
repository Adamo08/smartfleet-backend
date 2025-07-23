package com.adamo.vrspfab.auth;


import com.adamo.vrspfab.users.UserDto;
import com.adamo.vrspfab.users.UserMapper;
import com.adamo.vrspfab.users.UserRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;


@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthService authService;


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