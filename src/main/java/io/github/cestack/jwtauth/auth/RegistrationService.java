package io.github.cestack.jwtauth.auth;

import io.github.cestack.jwtauth.dto.AuthResponse;
import io.github.cestack.jwtauth.dto.RegisterRequest;
import io.github.cestack.jwtauth.service.JwtService;
import io.github.cestack.jwtauth.spi.RegistrationHandler;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

public class RegistrationService {

    private final RegistrationHandler registrationHandler;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public RegistrationService(RegistrationHandler registrationHandler,PasswordEncoder passwordEncoder,JwtService jwtService) {
        this.registrationHandler = registrationHandler;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {

        String encodedPassword =
                passwordEncoder.encode(request.password());

        Map<String, Object> attributes =
                request.attributes() != null
                        ? request.attributes()
                        : Map.of();

        UserDetails user =
                registrationHandler.register(
                        request.email(),
                        encodedPassword,
                        attributes
                );

        String username = user.getUsername();

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
