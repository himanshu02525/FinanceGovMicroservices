package com.finance.config;
 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;
 
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
 
	private final JwtFilter jwtAuthFilter;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
 
        http
            // ✅ Stateless JWT setup
            .csrf(AbstractHttpConfigurer::disable)
 
            // ✅ Identity issues tokens, no sessions
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
 
            // ✅ ONLY authentication rules (NO authorization logic)
            .authorizeHttpRequests(auth -> auth
 
                // ✅ Public auth APIs
                .requestMatchers(
                        "/api/auth/**",
                        "/api/users/getuserbyid/{id}",
                        "/swagger-ui/**",
                        "/api/v3/**",
                        "/actuator/health",
                        "/actuator/info"
                ).permitAll()
 
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                
                // ✅ Everything else requires a valid token
                .anyRequest().authenticated()
            )
        .addFilterBefore(jwtAuthFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
 
        return http.build();
    }
}