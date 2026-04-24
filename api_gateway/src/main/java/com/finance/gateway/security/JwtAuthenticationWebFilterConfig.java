package com.finance.gateway.security;

import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;

import reactor.core.publisher.Mono;

@Configuration
public class JwtAuthenticationWebFilterConfig {

    @Bean
    public AuthenticationWebFilter jwtAuthenticationWebFilter(
            JwtUtil jwtUtil,
            ServerSecurityContextRepository contextRepository
    ) {

        ReactiveAuthenticationManager authManager =
                authentication -> Mono.just(authentication);

        AuthenticationWebFilter filter =
                new AuthenticationWebFilter(authManager);

        filter.setSecurityContextRepository(contextRepository);

        filter.setServerAuthenticationConverter(exchange -> {

            String header = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            // ✅ No token → anonymous (will be blocked by authorizeExchange)
            if (header == null || !header.startsWith("Bearer ")) {
                return Mono.empty();
            }

            String token = header.substring(7);

            // ✅ Invalid token → authentication fails
            if (!jwtUtil.validateToken(token)) {
                return Mono.empty();
            }

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            jwtUtil.extractUsername(token),
                            token,
                            jwtUtil.extractRoles(token).stream()
                                    .map(SimpleGrantedAuthority::new)
                                    .collect(Collectors.toList())
                    );

            return Mono.just(authentication);
        });

        // ✅ DO NOT set success or failure handlers
        // ✅ Let Spring Security manage the flow

        return filter;
    }
}
