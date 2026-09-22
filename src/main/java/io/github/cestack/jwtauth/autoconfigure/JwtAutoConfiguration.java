package io.github.cestack.jwtauth.autoconfigure;


import io.github.cestack.jwtauth.config.JwtProperties;
import io.github.cestack.jwtauth.filter.JwtAuthenticationFilter;
import io.github.cestack.jwtauth.service.JwtService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetailsService;
import io.github.cestack.jwtauth.spi.TokenRevocationStore;
import io.github.cestack.jwtauth.token.InMemoryTokenRevocationStore;
@AutoConfiguration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtService jwtService(JwtProperties properties) {
        return new JwtService(properties);
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
}
