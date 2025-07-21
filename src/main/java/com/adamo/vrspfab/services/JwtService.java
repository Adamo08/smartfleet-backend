package com.adamo.vrspfab.services;


import com.adamo.vrspfab.config.JwtConfig;
import com.adamo.vrspfab.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@AllArgsConstructor
@Service
public class JwtService {


    private final JwtConfig jwtConfig;


    /**
     * Generates a JWT token for the given email.
     *
     * @param user the user for whom the token is generated
     * @return a Jwt object representing the token
     */
    public Jwt generateAccessToken(User user) {
        return generateToken(user, jwtConfig.getAccessTokenExpiration());
    }


    /**
     * Generates a refresh JWT token for the given user.
     *
     * @param user the user for whom the refresh token is generated
     * @return a Jwt object representing the refresh token
     */
    public Jwt generateRefreshToken(User user) {
        return generateToken(user, jwtConfig.getRefreshTokenExpiration());
    }


    /**
     * Generates a JWT token for the given user with a specified expiration time.
     *
     * @param user the user for whom the token is generated
     * @param tokenExpiration the expiration time in seconds
     * @return a Jwt object
     */
    private Jwt generateToken(User user, long tokenExpiration) {

        var claims = Jwts
                         .claims()
                         .subject(user.getId().toString())
                         .add("email", user.getEmail())
                         .add("firstName", user.getFirstName())
                         .add("lastName", user.getLastName())
                         .add("role", user.getRole())
                         .issuedAt(new Date())
                         .expiration(new Date(System.currentTimeMillis() + 1000 * tokenExpiration))
                         .build();

        return new Jwt(claims, jwtConfig.getSecretKey());
    }


    /**
     * Parses the JWT token and returns a Jwt object if valid.
     * @param token the JWT token to parse
     * @return a Jwt object if the token is valid, null otherwise
     */
    public Jwt parseToken(String token) {
        try {
            var claims = getClaims(token);
            return new Jwt(claims, jwtConfig.getSecretKey());
        } catch (JwtException e) {
            return null;
        }
    }


    /**
     * Retrieves the claims from the JWT token.
     * @param token the JWT token
     * @return Claims object containing the token's claims
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtConfig.getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}