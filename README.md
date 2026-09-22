# JWT Auth Spring Boot Starter

Reusable JWT authentication and Spring Security auto-configuration for Spring Boot applications.

Current version: `0.2.1`

Repository:

```text
https://github.com/ce-stack/jwt-auth-spring-boot-starter
```

## What this starter does

The starter removes the JWT and Spring Security code that is usually repeated in every project.

After adding the dependency, it provides:

- JWT access token generation
- JWT refresh token generation
- JWT parsing and validation
- Access/refresh token type validation
- Bearer token extraction
- JWT authentication filter
- Spring Security `SecurityFilterChain`
- Stateless authentication
- `SecurityContext` population
- BCrypt `PasswordEncoder`
- `AuthenticationManager`
- Configurable public endpoints
- Built-in login endpoint
- Built-in register endpoint
- Built-in refresh endpoint
- Built-in logout endpoint
- Refresh token rotation
- Token revocation
- Default in-memory revocation store
- Extension point for Redis/database revocation stores
- Spring Boot auto-configuration

Your application still owns:

- `User`
- `UserRepository`
- Database
- `UserDetailsService`
- `RegistrationHandler`
- Roles and permissions
- Application business rules

The goal is:

```text
Your application owns users and persistence.
The starter owns reusable JWT authentication infrastructure.
```

# Quick Start

## 1. Add the dependency

Add this to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.ce-stack</groupId>
    <artifactId>jwt-auth-spring-boot-starter</artifactId>
    <version>0.2.1</version>
</dependency>
```

You do not need to add JJWT dependencies manually.

The starter already brings the JWT dependencies it needs.

## 2. Configure JWT

Add this to `application.properties`:

```properties
jwt-auth.secret=12345678901234567890123456789012
jwt-auth.access-token-expiration=15m
jwt-auth.refresh-token-expiration=7d
jwt-auth.public-paths=/auth/**
```

Use a strong secret of at least 32 bytes.

Example with additional public endpoints:

```properties
jwt-auth.public-paths=/auth/**,/public/**,/swagger-ui/**,/v3/api-docs/**
```

YAML:

```yaml
jwt-auth:
  secret: 12345678901234567890123456789012
  access-token-expiration: 15m
  refresh-token-expiration: 7d
  public-paths:
    - /auth/**
    - /public/**
```

## 3. Provide UserDetailsService

The starter does not know how your users are stored.

Your application must provide a Spring Security `UserDetailsService`.

Example:

```java
@Bean
public UserDetailsService userDetailsService(
        UserRepository userRepository
) {
    return username -> userRepository
            .findByUsername(username)
            .orElseThrow(() ->
                    new UsernameNotFoundException(username)
            );
}
```

## 4. Provide RegistrationHandler

The starter does not know your `User` entity or database schema.

Provide a `RegistrationHandler`:

```java
@Bean
public RegistrationHandler registrationHandler(
        UserRepository userRepository
) {
    return (username, encodedPassword, attributes) -> {

        User user = new User();

        user.setUsername(username);
        user.setPassword(encodedPassword);

        userRepository.save(user);

        return user;
    };
}
```

The password passed to `RegistrationHandler` is already encoded.

Your returned user must be usable as Spring Security `UserDetails`.

## 5. Run the application

The starter now provides:

```text
POST /auth/register
POST /auth/login
POST /auth/refresh
POST /auth/logout
```

# Minimal In-Memory Example

For a quick test without a database:

```java
package com.example.demo.config;

import io.github.cestack.jwtauth.spi.RegistrationHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class TestAuthConfig {

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        return new InMemoryUserDetailsManager();
    }

    @Bean
    public RegistrationHandler registrationHandler(
            InMemoryUserDetailsManager users
    ) {
        return (username, encodedPassword, attributes) -> {

            if (users.userExists(username)) {
                throw new IllegalArgumentException("User already exists");
            }

            UserDetails user = User.builder()
                    .username(username)
                    .password(encodedPassword)
                    .roles("USER")
                    .build();

            users.createUser(user);

            return user;
        };
    }
}
```

This is enough to test register, login, refresh, logout, and protected endpoints.

# Built-in Endpoints

## Register

```http
POST /auth/register
Content-Type: application/json
```

```json
{
  "username": "amir@gmail.com",
  "password": "123456",
  "attributes": {
    "firstName": "Amir"
  }
}
```

Response:

```json
{
  "accessToken": "eyJhbGciOi...",
  "refreshToken": "eyJhbGciOi...",
  "tokenType": "Bearer"
}
```

If email is your login identifier, use the email value as `username`.

## Login

```http
POST /auth/login
Content-Type: application/json
```

```json
{
  "username": "amir@gmail.com",
  "password": "123456"
}
```

Response:

```json
{
  "accessToken": "eyJhbGciOi...",
  "refreshToken": "eyJhbGciOi...",
  "tokenType": "Bearer"
}
```

## Refresh Token

```http
POST /auth/refresh
Content-Type: application/json
```

```json
{
  "refreshToken": "YOUR_REFRESH_TOKEN"
}
```

Response:

```json
{
  "accessToken": "NEW_ACCESS_TOKEN",
  "refreshToken": "NEW_REFRESH_TOKEN",
  "tokenType": "Bearer"
}
```

The starter uses refresh token rotation.

```text
Validate refresh token
    ↓
Require token type = REFRESH
    ↓
Check revocation status
    ↓
Revoke old refresh token
    ↓
Generate new access token
    ↓
Generate new refresh token
```

The old refresh token cannot be reused after a successful refresh.

## Logout

```http
POST /auth/logout
Content-Type: application/json
```

```json
{
  "accessToken": "YOUR_ACCESS_TOKEN",
  "refreshToken": "YOUR_REFRESH_TOKEN"
}
```

Successful response:

```text
204 No Content
```

The supplied tokens are revoked until they expire.

# Adding Your Own Endpoints

The starter does not limit your application to the built-in auth endpoints.

You can add any controller or endpoint you need.

## Protected endpoint

```java
@RestController
public class ProfileController {

    @GetMapping("/api/profile")
    public String profile() {
        return "Protected profile";
    }
}
```

Because `/api/profile` is not in `jwt-auth.public-paths`, it is protected automatically.

Use:

```http
Authorization: Bearer YOUR_ACCESS_TOKEN
```

You do not need to write JWT validation code inside the controller.

## Public custom endpoint

```java
@RestController
@RequestMapping("/public")
public class PublicController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello";
    }
}
```

Add:

```properties
jwt-auth.public-paths=/auth/**,/public/**
```

Now `/public/hello` is public.

## Custom auth-related endpoint

You can add endpoints such as:

```text
POST /auth/forgot-password
POST /auth/verify-email
POST /auth/resend-code
GET  /api/me
POST /api/change-password
```

Example:

```java
@RestController
@RequestMapping("/auth")
public class CustomAuthController {

    @PostMapping("/forgot-password")
    public String forgotPassword() {
        return "Reset flow started";
    }
}
```

# What happens on protected requests

```text
Authorization header
    ↓
Extract Bearer token
    ↓
Validate JWT signature
    ↓
Validate expiration
    ↓
Require token type = ACCESS
    ↓
Check revocation status
    ↓
Read subject
    ↓
Load user through your UserDetailsService
    ↓
Create Spring Security Authentication
    ↓
Populate SecurityContext
    ↓
Continue request
```

# Token Structure

Generated JWTs contain:

```text
sub   username
jti   unique token ID
type  ACCESS or REFRESH
iat   issued-at time
exp   expiration time
```

`type` prevents a refresh token from being used as an access token.

`jti` allows individual tokens to be revoked.

# Token Revocation

The starter provides:

```text
InMemoryTokenRevocationStore
```

This requires no additional setup.

It is suitable for:

```text
local development
testing
single-instance applications
small deployments
```

You can provide your own `TokenRevocationStore` implementation.

Examples:

```text
RedisTokenRevocationStore
DatabaseTokenRevocationStore
Distributed cache implementation
```

If your application provides its own `TokenRevocationStore`, the starter backs off from the default in-memory implementation.

## Production note

The in-memory store:

```text
is local to one application instance
is cleared when the application restarts
is not shared between multiple application instances
```

For multi-instance deployments, use Redis or another shared store.

# Using JwtService Directly

The starter exposes `JwtService` as a Spring bean.

```java
@Service
public class TokenService {

    private final JwtService jwtService;

    public TokenService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public String createAccessToken(String username) {
        return jwtService.generateAccessToken(username);
    }
}
```

Custom claims:

```java
String token = jwtService.generateAccessToken(
        "amir",
        Map.of(
                "role", "ADMIN",
                "tenantId", "company-1"
        )
);
```

Generate refresh token:

```java
String refreshToken =
        jwtService.generateRefreshToken("amir");
```

Read subject:

```java
String username =
        jwtService.extractSubject(token);
```

Validate token:

```java
boolean valid =
        jwtService.isValid(token);
```

Check token type:

```java
boolean access =
        jwtService.isAccessToken(token);

boolean refresh =
        jwtService.isRefreshToken(token);
```

# Auto-Configuration

The starter separates responsibilities into three auto-configuration classes:

```text
JwtAutoConfiguration
    ↓
JwtProperties
JwtService
TokenRevocationStore
JwtAuthenticationFilter

SecurityAutoConfiguration
    ↓
PasswordEncoder
AuthenticationManager
SecurityFilterChain

AuthAutoConfiguration
    ↓
AuthService
AuthController
RegistrationService
RegistrationController
```

Spring Boot loads these automatically when the dependency is present.

You do not need to component-scan the starter packages.

# What You No Longer Need to Implement

Typical repeated code removed by the starter:

```text
JwtUtils
JWT signing
JWT parsing
JWT validation
Access/refresh token distinction
JwtAuthenticationFilter
Bearer header handling
SecurityFilterChain
PasswordEncoder configuration
AuthenticationManager configuration
SecurityContext setup
Stateless session configuration
Login controller
Login service
Refresh endpoint
Refresh token rotation
Logout endpoint
Token revocation
Registration controller
Registration service
```

Your application focuses on:

```text
User
UserRepository
UserDetailsService
RegistrationHandler
Roles
Permissions
Application business rules
```

# Spring Boot Version Compatibility

The starter version and your Spring Boot version are different things.

Starter version:

```xml
<dependency>
    <groupId>io.github.ce-stack</groupId>
    <artifactId>jwt-auth-spring-boot-starter</artifactId>
    <version>0.2.1</version>
</dependency>
```

Your application can have its own Spring Boot version:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.8</version>
</parent>
```

These versions do not need to match.

Changing your application's Spring Boot version does not mean you should change the starter version automatically.

Compatibility can still differ between Spring Boot versions because Spring Boot and Spring Security APIs can change.

## Verified baseline

Version `0.2.1` is currently verified against:

```text
Java 17
Spring Boot 4.0.8
Spring Security supplied by Spring Boot 4.0.8
```

Compatibility status:

| Starter | Spring Boot | Java | Status |
|---|---|---|---|
| `0.2.1` | `4.0.8` | `17` | Verified |
| `0.2.1` | other `4.0.x` versions | `17+` | Test before production |
| `0.2.1` | `4.1.x` | Boot-required Java version | Not certified yet |
| `0.2.1` | `3.x` | Boot-required Java version | Not certified yet |

Do not assume compatibility across Spring Boot major or minor lines until that combination has been tested.

For production usage, test your exact Spring Boot version.

## Maven dependency behavior

The consuming Spring Boot application's dependency management can control Spring dependency versions used at runtime.

That means the starter does not necessarily force the consuming project to use the exact dependency versions used when the starter was built.

This is useful, but it also means compatibility testing matters.

# Expected Developer Experience

```text
1. Create Spring Boot project
2. Add jwt-auth-spring-boot-starter
3. Configure jwt-auth properties
4. Provide UserDetailsService
5. Provide RegistrationHandler
6. Start application
7. Register or login
8. Use Bearer access token
9. Add any custom protected/public endpoints
10. Refresh token when needed
11. Logout to revoke tokens
```

Architecture:

```text
Spring Boot application
        +
jwt-auth-spring-boot-starter
        +
application.properties
        +
UserDetailsService
        +
RegistrationHandler
        ↓
JWT authentication flow ready
```

# Current Limitations

- Default token revocation is in-memory.
- Multi-instance deployments should provide a shared revocation store.
- Authentication-flow exceptions currently use the application's/default exception handling unless customized.
- Built-in endpoint paths are currently fixed under `/auth`.
- Fine-grained enable/disable switches for individual built-in endpoints are not yet provided.
- Replacing a built-in endpoint with the same HTTP method/path may require custom configuration to avoid mapping conflicts.

# Package Coordinates

```text
Group ID:    io.github.ce-stack
Artifact ID: jwt-auth-spring-boot-starter
Version:     0.2.1
```

# Source Code

```text
https://github.com/ce-stack/jwt-auth-spring-boot-starter
```

# License

Apache License 2.0
