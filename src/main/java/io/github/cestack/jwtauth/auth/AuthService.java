package io.github.cestack.jwtauth.auth;

import io.github.cestack.jwtauth.dto.AuthResponse;
import io.github.cestack.jwtauth.dto.LoginRequest;
import io.github.cestack.jwtauth.dto.RefreshRequest;
import io.github.cestack.jwtauth.service.JwtService;
import io.github.cestack.jwtauth.spi.TokenRevocationStore;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import io.github.cestack.jwtauth.dto.LogoutRequest;

public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenRevocationStore tokenRevocationStore;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService, TokenRevocationStore tokenRevocationStore) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.tokenRevocationStore = tokenRevocationStore;
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.email(),
                                request.password()
                        )
                );

        String username = authentication.getName();

        String accessToken =
                jwtService.generateAccessToken(username);

        String refreshToken =
                jwtService.generateRefreshToken(username);

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer"
        );
    }

    public AuthResponse refresh(RefreshRequest request) {

        String refreshToken = request.refreshToken();

        if (!jwtService.isValid(refreshToken) ||
                !jwtService.isRefreshToken(refreshToken)) {

            throw new IllegalArgumentException(
                    "Invalid refresh token"
            );
        }

        String tokenId =
                jwtService.extractTokenId(refreshToken);

        if (tokenRevocationStore.isRevoked(tokenId)) {

            throw new IllegalArgumentException(
                    "Refresh token has been revoked"
            );
        }

        String username =
                jwtService.extractSubject(refreshToken);

        tokenRevocationStore.revoke(
                tokenId,
                jwtService.extractExpiration(refreshToken)
        );

        String newAccessToken =
                jwtService.generateAccessToken(username);

        String newRefreshToken =
                jwtService.generateRefreshToken(username);

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer"
        );
    }

    public void logout(LogoutRequest request) {

        boolean hasToken = false;

        if (request.accessToken() != null &&
                !request.accessToken().isBlank()) {

            hasToken = true;

            String accessToken = request.accessToken();

            if (!jwtService.isValid(accessToken) ||
                    !jwtService.isAccessToken(accessToken)) {

                throw new IllegalArgumentException(
                        "Invalid access token"
                );
            }

            tokenRevocationStore.revoke(
                    jwtService.extractTokenId(accessToken),
                    jwtService.extractExpiration(accessToken)
            );
        }

        if (request.refreshToken() != null &&
                !request.refreshToken().isBlank()) {

            hasToken = true;

            String refreshToken = request.refreshToken();

            if (!jwtService.isValid(refreshToken) ||
                    !jwtService.isRefreshToken(refreshToken)) {

                throw new IllegalArgumentException(
                        "Invalid refresh token"
                );
            }

            tokenRevocationStore.revoke(
                    jwtService.extractTokenId(refreshToken),
                    jwtService.extractExpiration(refreshToken)
            );
        }

        if (!hasToken) {
            throw new IllegalArgumentException(
                    "At least one token is required"
            );
        }
    }
}