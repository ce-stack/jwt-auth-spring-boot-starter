# JWT Auth Spring Boot Starter

A reusable JWT security starter for Spring Boot applications.

Current version: `0.1.0`

This starter removes the repeated JWT security setup that is normally copied from project to project. You keep your own user model, database, repositories, and `UserDetailsService`.

## Installation

Add the dependency to your Maven project:

```xml
<dependency>
    <groupId>io.github.ce-stack</groupId>
    <artifactId>jwt-auth-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

The package is published on Maven Central, so no extra repository configuration is required.

## Configuration

Add the JWT settings to `application.properties`:

```properties
jwt-auth.secret=12345678901234567890123456789012
jwt-auth.access-token-expiration=15m
jwt-auth.refresh-token-expiration=7d
jwt-auth.public-paths=/auth/**,/public/**
```

Use a secret with at least 32 bytes.

You can also use YAML:

```yaml
jwt-auth:
  secret: 12345678901234567890123456789012
  access-token-expiration: 15m
  refresh-token-expiration: 7d
  public-paths:
    - /auth/**
    - /public/**
```

## What the starter provides

| Feature | Included |
|---|---|
| JWT token generation | Yes |
| JWT token parsing | Yes |
| JWT token validation | Yes |
| Access token expiration handling | Yes |
| Refresh token generation | Yes |
| Bearer token extraction | Yes |
| JWT authentication filter | Yes |
| Spring Security filter registration | Yes |
| `SecurityContext` population | Yes |
| Stateless session configuration | Yes |
| BCrypt `PasswordEncoder` bean | Yes |
| Configurable public endpoints | Yes |
| Auto-configuration | Yes |
| Login endpoint | Not yet |
| Register endpoint | Not yet |
| Logout endpoint | Not yet |
| Refresh endpoint | Not yet |
| User database integration | Application-specific |

## What you no longer need to create in every project

Without this starter, a typical JWT project usually repeats:

```text
JwtUtils
JwtAuthenticationFilter
SecurityConfig
PasswordEncoder configuration
JWT parsing logic
JWT validation logic
Bearer header handling
SecurityContext setup
Stateless session configuration
```

With this starter, these parts are already provided.

Your application remains responsible for:

```text
User entity
User repository
Database
UserDetailsService
Login business logic
Registration business logic
Logout business logic
Refresh endpoint
Roles and permissions specific to your application
```

## Required UserDetailsService

The starter does not define how users are stored.

Every project can have a different user table, entity, repository, username field, email field, role model, or database.

Your application only needs to expose a normal Spring Security `UserDetailsService`.

Example:

```java
package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class UserConfiguration {

    @Bean
    public UserDetailsService userDetailsService() {

        return new InMemoryUserDetailsManager(
                User.withUsername("admin")
                        .password("{noop}123456")
                        .roles("USER")
                        .build()
        );
    }
}
```

In a real application, replace the in-memory implementation with your own repository.

Example:

```java
@Bean
public UserDetailsService userDetailsService(UserRepository userRepository) {
    return username -> userRepository
            .findByUsername(username)
            .orElseThrow(() ->
                    new UsernameNotFoundException(username)
            );
}
```

## Using JwtService

The starter exposes `JwtService` as a Spring bean.

```java
import io.github.cestack.jwtauth.service.JwtService;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private final JwtService jwtService;

    public TokenService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public String createToken(String username) {
        return jwtService.generateAccessToken(username);
    }
}
```

You can also add custom claims:

```java
import java.util.Map;

String token = jwtService.generateAccessToken(
        "amir",
        Map.of(
                "role", "ADMIN",
                "tenantId", "company-1"
        )
);
```

## Creating a refresh token

```java
String refreshToken = jwtService.generateRefreshToken("amir");
```

## Reading the token subject

```java
String username = jwtService.extractSubject(token);
```

## Validating a token

```java
boolean valid = jwtService.isValid(token);
```

## Example controller

```java
package com.example.demo.controller;

import io.github.cestack.jwtauth.service.JwtService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    private final JwtService jwtService;

    public DemoController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @GetMapping("/public/token")
    public String token() {
        return jwtService.generateAccessToken("admin");
    }

    @GetMapping("/private/test")
    public String privateEndpoint() {
        return "JWT authentication is working";
    }
}
```

Because `/public/**` is configured as public:

```properties
jwt-auth.public-paths=/auth/**,/public/**
```

you can call:

```text
GET /public/token
```

Then use the returned token:

```http
Authorization: Bearer YOUR_TOKEN
```

when calling:

```text
GET /private/test
```

The starter will:

```text
Read the Authorization header
Extract the Bearer token
Validate the JWT
Read the subject
Load the user through your UserDetailsService
Create the Spring Security Authentication object
Store it in SecurityContext
Continue the request
```

## Default security behavior

The starter configures Spring Security as stateless.

Protected requests are expected to send:

```http
Authorization: Bearer eyJhbGciOi...
```

Public endpoints can be changed from configuration:

```properties
jwt-auth.public-paths=/auth/**,/public/**,/swagger-ui/**,/v3/api-docs/**
```

## Override support

The starter uses Spring Boot auto-configuration.

If your application provides its own compatible bean, Spring can use your custom implementation instead of the starter default.

## Current scope of version 0.1.0

Version `0.1.0` focuses on JWT infrastructure.

It does not yet provide complete authentication endpoints.

The application still implements:

```text
POST /auth/login
POST /auth/register
POST /auth/logout
POST /auth/refresh
```

## Planned version 0.2.0

Planned features:

```text
Built-in login endpoint
Built-in register flow through a project-provided registration handler
Built-in logout endpoint
Built-in refresh endpoint
Refresh token rotation
Refresh token revocation
Token type validation
Reusable token revocation store
Default in-memory revocation implementation
Extension point for Redis or database-backed revocation
```

The goal is for most applications to provide only:

```text
UserDetailsService
RegistrationHandler
Application-specific user model and repository
```

while the starter handles the rest of the authentication flow.

## Package coordinates

```text
Group ID:    io.github.ce-stack
Artifact ID: jwt-auth-spring-boot-starter
Version:     0.1.0
```

## Source code

```text
https://github.com/ce-stack/jwt-auth-spring-boot-starter
```

## License

Apache License 2.0
