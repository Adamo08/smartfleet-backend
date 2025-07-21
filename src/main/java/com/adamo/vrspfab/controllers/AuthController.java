package com.adamo.vrspfab.controllers;


import com.adamo.vrspfab.config.JwtConfig;
import com.adamo.vrspfab.dtos.JwtResponse;
import com.adamo.vrspfab.dtos.LoginRequest;
import com.adamo.vrspfab.dtos.UserDto;
import com.adamo.vrspfab.mappers.UserMapper;
import com.adamo.vrspfab.repositories.UserRepository;
import com.adamo.vrspfab.services.JwtService;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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


    /**
     * This method handles user login requests.
     *
     * @return ResponseEntity indicating the result of the login operation.
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow();



        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        // Create a cookie for the refresh token
        var cookie = new Cookie("refreshToken", refreshToken.toString());

        cookie.setHttpOnly(true); // Prevents JavaScript access
        cookie.setPath("/auth/refresh"); // Set the path for the cookie
        cookie.setMaxAge(jwtConfig.getRefreshTokenExpiration()); // Set cookie expiration
        cookie.setSecure(true); // Use secure cookies in production (HTTPS)

        response.addCookie(cookie); // Add the cookie to the response

        // Return the access token in the response body
        var jwt = new JwtResponse(accessToken.toString());
        return ResponseEntity.ok(jwt); // 200 OK with JWT response


    }




    @GetMapping("/me")
    public ResponseEntity<UserDto> me(){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 Unauthorized
        }

        var userId = (Long) authentication.getPrincipal();

        var user = userRepository
                .findById(userId)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 Not Found
        }

        var userDto = userMapper.toDto(user);
        return ResponseEntity.ok(userDto); // 200 OK with user details

    }


    @PostMapping("/validate")
    public boolean validateToken(
            @RequestHeader("Authorization") String authorizationHeader
    ) {

        System.out.println("Validate Token Called");

        String token = authorizationHeader.replace("Bearer ", "");
        var jwt = jwtService.parseToken(token);

        return !jwt.isExpired();
    }



    /**
     * This method handles the refresh token request.
     *
     * @param refreshToken the refresh token from the cookie
     * @return ResponseEntity with the new JWT response
     */
    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(
            @CookieValue(value = "refreshToken") String refreshToken
    ) {

        var jwt = jwtService.parseToken(refreshToken);

        if (jwt == null || jwt.isExpired()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var userId = jwt.getUserId();

        var user = userRepository.findById(userId).orElseThrow();
        var accessToken = jwtService.generateAccessToken(user);

        return ResponseEntity.ok(new JwtResponse(accessToken.toString()));
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