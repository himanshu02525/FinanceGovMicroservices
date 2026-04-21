package com.finance.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import com.finance.exceptions.GlobalExceptionHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;
    private final GlobalExceptionHandler globalExceptionHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // JWT → disable CSRF
            .csrf(AbstractHttpConfigurer::disable)

            // Custom 401 / 403 handling
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(globalExceptionHandler)
                .accessDeniedHandler(globalExceptionHandler)
            )

            .authorizeHttpRequests(auth -> auth

                /* ================= PUBLIC ================= */
                .requestMatchers("/api/auth/**").permitAll()

                /* ================= COMPLIANCE ================= */
                .requestMatchers(
                    "/compliance",
                    "/compliance/{id}",
                    "/compliance/entity/{entityId}",
                    "/compliance/summary"
                ).hasAnyAuthority(
                    "ROLE_COMPLIANCE_OFFICER",
                    "ROLE_FINANCIAL_OFFICER",
                    "ROLE_ADMIN"
                )

                // Compliance Officer exclusive write access
                .requestMatchers(
                    "/compliance/**"
                ).hasAuthority("ROLE_COMPLIANCE_OFFICER")

                /* ================= AUDIT ================= */
                .requestMatchers(
                    "/audit",
                    "/audit/{id}",
                    "/audit/officer/{officerId}"
                ).hasAuthority("ROLE_GOVERNMENT_AUDITOR")

                .requestMatchers("/audit/summary").hasAnyAuthority(
                    "ROLE_GOVERNMENT_AUDITOR",
                    "ROLE_PROGRAM_MANAGER",
                    "ROLE_ADMIN"
                )

                /* ================= PROGRAM MANAGER ================= */
                .requestMatchers(
                    "/api/resources/**",
                    "/api/budget-allocations/**"
                ).hasAuthority("ROLE_PROGRAM_MANAGER")

                /* ================= ANALYTICS ================= */
                .requestMatchers(
                    "/api/analytics/reports",
                    "/api/analytics/dashboard"
                ).hasAnyAuthority(
                    "ROLE_ADMIN",
                    "ROLE_GOVERNMENT_AUDITOR"
                )

                /* ================= CITIZEN ================= */
                .requestMatchers(
                    "/compliance/entity/{entityId}"
                ).hasAuthority("ROLE_CITIZEN")

                /* ================= ADMIN ================= */
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

                /* ================= FALLBACK ================= */
                .anyRequest().authenticated()
            )

            // Stateless JWT session
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authenticationProvider(authenticationProvider)

            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .addLogoutHandler(logoutHandler)
                .logoutSuccessHandler((req, res, auth) -> {
                    SecurityContextHolder.clearContext();
                    res.setStatus(200);
                    res.setContentType("application/json");
                    res.getWriter().write("{\"message\":\"Logout successful\"}");
                })
            );

        return http.build();
    }
}


