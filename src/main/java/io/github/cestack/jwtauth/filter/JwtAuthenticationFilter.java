package io.github.cestack.jwtauth.filter;

import io.github.cestack.jwtauth.config.JwtProperties;
import io.github.cestack.jwtauth.service.JwtService;
import io.github.cestack.jwtauth.spi.TokenRevocationStore;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRevocationStore tokenRevocationStore;
    private final JwtProperties properties;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService,
            JwtProperties properties,
            TokenRevocationStore tokenRevocationStore
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.properties = properties;
        this.tokenRevocationStore = tokenRevocationStore;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader =
                request.getHeader(properties.getHeader());

        if (authHeader == null ||
                !authHeader.startsWith(properties.getTokenPrefix())) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(
                properties.getTokenPrefix().length()
        );

        if (!jwtService.isValid(token) ||
                !jwtService.isAccessToken(token)) {

            filterChain.doFilter(request, response);
            return;
        }

        String tokenId = jwtService.extractTokenId(token);

        if (tokenRevocationStore.isRevoked(tokenId)) {

            filterChain.doFilter(request, response);
            return;
        }

        String username =
                jwtService.extractSubject(token);

        if (username != null &&
                SecurityContextHolder
                        .getContext()
                        .getAuthentication() == null) {

            UserDetails userDetails =
                    userDetailsService
                            .loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}