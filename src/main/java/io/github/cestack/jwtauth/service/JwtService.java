package io.github.cestack.jwtauth.service;

import io.github.cestack.jwtauth.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.time.Instant;
public class JwtService {

    public static final String ACCESS_TOKEN = "ACCESS";
    public static final String REFRESH_TOKEN = "REFRESH";

    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                properties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateAccessToken(String subject) {
        return generateAccessToken(subject, Map.of());
    }

    public String generateAccessToken(String subject,Map<String, Object> claims) {

        return generateToken(
                subject,
                claims,
                ACCESS_TOKEN,
                properties.getAccessTokenExpiration().toMillis()
        );
    }

    public String generateRefreshToken(String subject) {

        return generateToken(
                subject,
                Map.of(),
                REFRESH_TOKEN,
                properties.getRefreshTokenExpiration().toMillis()
        );
    }

    private String generateToken(String subject,Map<String, Object> claims,String tokenType,long expirationMillis
    ) {

        Date now = new Date();

        Date expiration =
                new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .id(UUID.randomUUID().toString())
                .claim("type", tokenType)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }
    
    public Claims extractClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractSubject(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractTokenId(String token) {
        return extractClaims(token).getId();
    }

    public String extractTokenType(String token) {
        return extractClaims(token)
                .get("type", String.class);
    }

    public Instant extractExpiration(String token) {
        return extractClaims(token)
                .getExpiration()
                .toInstant();
    }

    public boolean isValid(String token) {

        try {
            extractClaims(token);
            return true;

        } catch (Exception exception) {
            return false;
        }
    }

    public boolean isAccessToken(String token) {

        try {
            return ACCESS_TOKEN.equals(
                    extractTokenType(token)
            );

        } catch (Exception exception) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {

        try {
            return REFRESH_TOKEN.equals(
                    extractTokenType(token)
            );

        } catch (Exception exception) {
            return false;
        }
    }
}
