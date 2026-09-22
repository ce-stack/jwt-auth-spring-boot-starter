package io.github.cestack.jwtauth.autoconfigure;

import io.github.cestack.jwtauth.auth.AuthController;
import io.github.cestack.jwtauth.auth.AuthService;
import io.github.cestack.jwtauth.auth.RegistrationController;
import io.github.cestack.jwtauth.auth.RegistrationService;
import io.github.cestack.jwtauth.service.JwtService;
import io.github.cestack.jwtauth.spi.RegistrationHandler;
import io.github.cestack.jwtauth.spi.TokenRevocationStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@AutoConfiguration
@AutoConfigureAfter({
        JwtAutoConfiguration.class,
        SecurityAutoConfiguration.class
})
public class AuthAutoConfiguration {

    @Bean
    @ConditionalOnBean({
            AuthenticationManager.class,
            JwtService.class,
            TokenRevocationStore.class
    })
    @ConditionalOnMissingBean
    public AuthService authService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            TokenRevocationStore tokenRevocationStore
    ) {
        return new AuthService(
                authenticationManager,
                jwtService,
                tokenRevocationStore
        );
    }

    @Bean
    @ConditionalOnBean(AuthService.class)
    @ConditionalOnMissingBean
    public AuthController authController(
            AuthService authService
    ) {
        return new AuthController(authService);
    }

    @Bean
    @ConditionalOnBean(RegistrationHandler.class)
    @ConditionalOnMissingBean
    public RegistrationService registrationService(
            RegistrationHandler registrationHandler,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        return new RegistrationService(
                registrationHandler,
                passwordEncoder,
                jwtService
        );
    }

    @Bean
    @ConditionalOnBean(RegistrationService.class)
    @ConditionalOnMissingBean
    public RegistrationController registrationController(
            RegistrationService registrationService
    ) {
        return new RegistrationController(
                registrationService
        );
    }
}