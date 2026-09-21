package io.github.cestack.jwtauth.service;

import io.github.cestack.jwtauth.config.JwtProperties;

public class JwtService {

    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }
}
