# JWT Auth Spring Boot Starter

Reusable JWT authentication and Spring Security auto-configuration for Spring Boot applications.

Current development release target: `0.2.1`

Repository:

```text
https://github.com/ce-stack/jwt-auth-spring-boot-starter
```

## Why this starter exists

JWT authentication in Spring Boot usually requires repeating the same infrastructure in every project:

```text
JWT utility/service
JWT signing
JWT parsing
JWT validation
Bearer token extraction
JWT authentication filter
SecurityFilterChain
PasswordEncoder
AuthenticationManager
SecurityContext setup
Stateless session configuration
Login flow
Refresh flow
Logout flow
Token revocation
Registration flow
```

This starter provides those reusable parts.

Your application keeps control of the parts that are different from one project to another:

```text
User entity
User repository
Database
UserDetailsService
RegistrationHandler
Roles and permissions
Application-specific business rules
```

## What the starter provides

| Feature | Included |
|---|---|
| Spring Security auto-configuration | Yes |
| Stateless authentication | Yes |
| BCrypt `PasswordEncoder` | Yes |
| `AuthenticationManager` | Yes |
| JWT access token generation | Yes |
| JWT refresh token generation | Yes |
| JWT parsing | Yes |
| JWT signature validation | Yes |
| JWT expiration validation | Yes |
| Access/refresh token type validation | Yes |
| Unique JWT ID (`jti`) | Yes |
| Bearer token extraction | Yes |
| JWT authentication filter | Yes |
| `SecurityContext` population | Yes |
| Configurable public paths | Yes |
| Login endpoint | Yes |
| Register endpoint | Yes |
| Refresh endpoint | Yes |
| Logout endpoint | Yes |
| Refresh token rotation | Yes |
| Token revocation | Yes |
| Default in-memory revocation store | Yes |
| Custom revocation store support | Yes |
| User persistence | Application-specific |
| `UserDetailsService` | Application-specific |
| Registration persistence | Application-specific |

## Installation

Add the dependency to your Maven project:

```xml
<dependency>
    <groupId>io.github.ce-stack</groupId>
    <artifactId>jwt-auth-spring-boot-starter</artifactId>
    <version>0.2.1</version>
</dependency>
```

You do not need to add JJWT manually. The starter already includes the JWT dependencies it needs.

## Configuration

Add the following to `application.properties`:

```properties
jwt-auth.secret=12345678901234567890123456789012
jwt-auth.access-token-expiration=15m
jwt-auth.refresh-token-expiration=7d
jwt-auth.public-paths=/auth/**
```

Use a strong secret of at least 32 bytes.

You can expose more public endpoints when needed:

```properties
jwt-auth.public-paths=/auth/**,/public/**,/swagger-ui/**,/v3/api-docs/**
```

YAML is also supported:

```yaml
jwt-auth:
  secret: 12345678901234567890123456789012
  access-token-expiration: 15m
  refresh-token-expiration: 7d
  public-paths:
    - /auth/**
    - /public/**
```

## What your application must provide

The starter intentionally does not decide how users are stored.

Your application provides:

```text
UserDetailsService
RegistrationHandler
User entity
User repository
Database
Application-specific roles and authorities
```

This allows the starter to work with different persistence strategies such as JPA, JDBC, MongoDB, external identity stores, in-memory users, or custom repositories.

## Minimal test setup

For a quick test, you can use an in-memory user store.

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

This is enough to test the built-in register, login, refresh, logout, and JWT authentication flows.

## Real application example

Example `UserDetailsService`:

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

Example `RegistrationHandler`:

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

Your returned user object must be usable as Spring Security `UserDetails`.

The starter passes an already encoded password to `RegistrationHandler`.

## Built-in authentication endpoints

```text
POST /auth/register
POST /auth/login
POST /auth/refresh
POST /auth/logout
```

`POST /auth/register` is created when your application provides a `RegistrationHandler`.

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

Flow:

```text
Request
  ↓
PasswordEncoder
  ↓
RegistrationHandler supplied by your application
  ↓
Your application stores the user
  ↓
Access token generated
  ↓
Refresh token generated
```

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

Flow:

```text
Request
  ↓
AuthenticationManager
  ↓
Your UserDetailsService
  ↓
Spring Security verifies the password
  ↓
Access token generated
  ↓
Refresh token generated
```

## Protected endpoints

Any endpoint that is not configured as public requires authentication.

Example:

```java
package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/hello")
    public String hello() {
        return "JWT WORKING";
    }
}
```

Without an access token:

```http
GET /api/hello
```

the request is rejected.

With an access token:

```http
GET /api/hello
Authorization: Bearer YOUR_ACCESS_TOKEN
```

the request is authenticated.

Internally:

```text
Authorization header
  ↓
Bearer token extraction
  ↓
JWT validation
  ↓
Require token type = ACCESS
  ↓
Check token revocation
  ↓
Read JWT subject
  ↓
Load user through UserDetailsService
  ↓
Create Spring Security Authentication
  ↓
Populate SecurityContext
  ↓
Continue the request
```

## Refresh token flow

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

A refresh token that has already been rotated cannot be reused.

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

The starter revokes the supplied tokens until their expiration time.

After logout:

```text
revoked access token -> rejected
revoked refresh token -> rejected
```

## Token structure

Generated tokens include:

```text
sub   username
jti   unique token ID
type  ACCESS or REFRESH
iat   issued-at time
exp   expiration time
```

`type` prevents a refresh token from being accepted as an access token.

`jti` allows an individual token to be revoked.

## Token revocation store

The starter provides:

```text
InMemoryTokenRevocationStore
```

It requires no additional setup and is useful for local development, testing, and single-instance applications.

The extension point is:

```java
TokenRevocationStore
```

You can provide another implementation such as Redis, a database, or a distributed cache.

If your application provides its own `TokenRevocationStore` bean, the starter backs off from the default in-memory implementation.

### Production behavior

The default in-memory revocation store:

```text
exists only in the current application instance
is cleared when the application restarts
is not shared between multiple application instances
```

For distributed or persistent deployments, use a shared revocation store such as Redis or a database.

## Using JwtService directly

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
String refreshToken = jwtService.generateRefreshToken("amir");
```

Read subject:

```java
String username = jwtService.extractSubject(token);
```

Validate token:

```java
boolean valid = jwtService.isValid(token);
```

Check token type:

```java
boolean access = jwtService.isAccessToken(token);
boolean refresh = jwtService.isRefreshToken(token);
```

## Auto-configuration

The starter separates responsibilities into three auto-configuration classes.

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

You do not need to component-scan the starter package.

## What you no longer need to implement

Typical repeated authentication code removed by the starter:

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
Registration flow
```

Your project focuses on:

```text
User
UserRepository
UserDetailsService
RegistrationHandler
Roles
Permissions
Application business rules
```

## Expected developer experience

Typical setup:

```text
1. Add one Maven dependency
2. Configure jwt-auth properties
3. Provide UserDetailsService
4. Provide RegistrationHandler
5. Run the application
6. Register or login
7. Use the returned access token
8. Refresh when needed
9. Logout to revoke tokens
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

## What the starter does not control

The starter intentionally leaves these concerns to the consuming application:

```text
database technology
User entity structure
repository design
email verification
account activation
password reset
MFA
tenant rules
organization rules
application-specific roles
application-specific authorization
profile data
business validation
```

## Current limitations

The current implementation uses the application's/default exception handling for authentication flow exceptions.

Applications that need a specific JSON error contract can provide their own exception handling.

The default token revocation store is in-memory and should be replaced for multi-instance production deployments.

## Compatibility

Current baseline:

```text
Java 17
Spring Boot 4.0.x
Spring Security supplied by Spring Boot
```

Test the starter with your exact Spring Boot version before production deployment.

## Package coordinates

```text
Group ID:    io.github.ce-stack
Artifact ID: jwt-auth-spring-boot-starter
Version:     0.2.1
```

## Source code

```text
https://github.com/ce-stack/jwt-auth-spring-boot-starter
```

## License

Apache License 2.0
