package io.github.cestack.jwtauth.dto;

import java.util.Map;

public record RegisterRequest(
        String email,
        String password,
        Map<String, Object> attributes
) {
}