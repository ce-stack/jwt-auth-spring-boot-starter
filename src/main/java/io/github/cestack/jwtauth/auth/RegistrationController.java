package io.github.cestack.jwtauth.auth;


import io.github.cestack.jwtauth.dto.AuthResponse;
import io.github.cestack.jwtauth.dto.RegisterRequest;
import io.github.cestack.jwtauth.service.JwtService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.cestack.jwtauth.auth.RegistrationController;
import io.github.cestack.jwtauth.auth.RegistrationService;
import io.github.cestack.jwtauth.spi.RegistrationHandler;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/auth")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(
                registrationService.register(request)
        );
    }

    @Bean
    @ConditionalOnBean(RegistrationHandler.class)
    @ConditionalOnMissingBean
    public RegistrationService registrationService(RegistrationHandler registrationHandler,PasswordEncoder passwordEncoder,JwtService jwtService) {
        return new RegistrationService(
                registrationHandler,
                passwordEncoder,
                jwtService
        );
    }

    @Bean
    @ConditionalOnBean(RegistrationService.class)
    @ConditionalOnMissingBean
    public RegistrationController registrationController(RegistrationService registrationService) {
        return new RegistrationController(
                registrationService
        );
    }
}
