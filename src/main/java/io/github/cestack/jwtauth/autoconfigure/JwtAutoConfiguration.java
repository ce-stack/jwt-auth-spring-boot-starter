package io.github.cestack.jwtauth.autoconfigure;


import io.github.cestack.jwtauth.auth.AuthService;
import io.github.cestack.jwtauth.config.JwtProperties;
import io.github.cestack.jwtauth.filter.JwtAuthenticationFilter;
import io.github.cestack.jwtauth.service.JwtService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import io.github.cestack.jwtauth.spi.TokenRevocationStore;
import io.github.cestack.jwtauth.token.InMemoryTokenRevocationStore;
import io.github.cestack.jwtauth.auth.AuthController;

@AutoConfiguration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AuthService authService(AuthenticationManager authenticationManager,JwtService jwtService , TokenRevocationStore tokenRevocationStore) {
        return new AuthService(
                authenticationManager,
                jwtService,
                tokenRevocationStore
        );
    }

    @Bean
    @ConditionalOnBean(UserDetailsService.class)
    @ConditionalOnMissingBean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService,UserDetailsService userDetailsService,JwtProperties properties,TokenRevocationStore tokenRevocationStore) {
        return new JwtAuthenticationFilter(
                jwtService,
                userDetailsService,
                properties,
                tokenRevocationStore
        );
    }

    @Bean
    @ConditionalOnMissingBean(TokenRevocationStore.class)
    public TokenRevocationStore tokenRevocationStore() {
        return new InMemoryTokenRevocationStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthController authController(AuthService authService) {
        return new AuthController(authService);
    }
}
