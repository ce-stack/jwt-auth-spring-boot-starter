package io.github.cestack.jwtauth.auth;

import io.github.cestack.jwtauth.dto.AuthResponse;
import io.github.cestack.jwtauth.dto.LoginRequest;
import io.github.cestack.jwtauth.service.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager,JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
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
}