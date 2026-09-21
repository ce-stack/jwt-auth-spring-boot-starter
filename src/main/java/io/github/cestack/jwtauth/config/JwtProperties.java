package io.github.cestack.jwtauth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

public class JwtProperties {

    private String secret;

    private Duration accessTokenExpiration = Duration.ofMinutes(15);

    private Duration refreshTokenExpiration = Duration.ofDays(7);

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Duration getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public void setAccessTokenExpiration(Duration accessTokenExpiration) {
        this.accessTokenExpiration = accessTokenExpiration;
    }

    public Duration getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public void setRefreshTokenExpiration(Duration refreshTokenExpiration) {
        this.refreshTokenExpiration = refreshTokenExpiration;
    }
}
