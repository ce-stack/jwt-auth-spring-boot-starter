package io.github.cestack.jwtauth.auth;

import io.github.cestack.jwtauth.dto.AuthResponse;
import io.github.cestack.jwtauth.dto.LoginRequest;
import io.github.cestack.jwtauth.dto.RefreshRequest;
import io.github.cestack.jwtauth.service.JwtService;
import io.github.cestack.jwtauth.spi.TokenRevocationStore;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

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
}