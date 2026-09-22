package io.github.cestack.jwtauth.dto;

public record AuthResponse(String accessToken, String refreshToken , String tokenType) {
}
