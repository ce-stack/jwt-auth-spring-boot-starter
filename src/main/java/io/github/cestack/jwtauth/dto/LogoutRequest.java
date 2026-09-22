package io.github.cestack.jwtauth.dto;

public record LogoutRequest(
        String accessToken,
        String refreshToken
) {
}