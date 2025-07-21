package com.adamo.vrspfab.services;


import com.adamo.vrspfab.entities.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.util.Date;


public class Jwt {
    private final Claims claims;
    private final SecretKey secretKey;

    public Jwt(Claims claims, SecretKey secretKey) {
        this.claims = claims;
        this.secretKey = secretKey;
    }


    /**
     * Validates the JWT token.
     *
     * @return true if the token is valid, false otherwise
     */
    public boolean isExpired() {
        return claims.getExpiration().before(new Date());
    }

    /**
     * Retrieves the user ID from the JWT token.
     *
     * @return the user ID as a String
     */
    public Long getUserId() {
        return Long.valueOf(claims.getSubject());
    }


    /**
     * Retrieves the role from the JWT token.
     *
     * @return the role as a String
     */
    public Role getRole() {
        return Role.valueOf(claims.get("role", String.class));
    }



    /**
     * Retrieves the email from the JWT token.
     *
     * @return the email as a String
     */
    public String getEmail() {
        return claims.get("email", String.class);
    }


    /**
     * Converts the JWT claims to a String representation.
     *
     * @return the JWT token as a String
     */
    public String toString() {
        return Jwts
            .builder()
            .claims(claims)
            .signWith(secretKey)
            .compact();
    }

}
