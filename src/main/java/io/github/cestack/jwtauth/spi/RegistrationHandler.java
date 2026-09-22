package io.github.cestack.jwtauth.spi;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

public interface RegistrationHandler {

    UserDetails register(
            String email,
            String encodedPassword,
            Map<String, Object> attributes
    );
}