package com.adamo.vrspfab.auth;


import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
@ConfigurationProperties(prefix = "spring.jwt")
@Data
public class JwtConfig {
    private String secret;
    private int accessTokenExpiration;
    private int refreshTokenExpiration;


    /**
     * Returns a SecretKey derived from the configured secret string.
     *
     * @return SecretKey for JWT signing
     */
    public SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}