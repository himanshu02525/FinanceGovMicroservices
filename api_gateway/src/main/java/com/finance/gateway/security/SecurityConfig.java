package com.finance.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
 
import lombok.RequiredArgsConstructor;
 
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {
 
    private final AuthenticationWebFilter jwtAuthenticationWebFilter;
 
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, CorsConfigurationSource corsConfigurationSource) {
 
        return http
            .csrf(csrf -> csrf.disable())
            // ✅ CHANGE 1: Use the injected source instead of .disable()
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .addFilterAt(jwtAuthenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .authorizeExchange(auth -> auth
                // ✅ CHANGE 2: Ensure this stays at the very top
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
 
                /* =====================================================
                 * 1. PUBLIC ENDPOINTS
                 * ===================================================== */
                .pathMatchers(
                    "/actuator/health",
                    "/actuator/info",
                    "/api/auth/**",
                    "/swagger-ui/**",
                    "/api/v3/**"
                ).permitAll()
                // ... (rest of your existing matchers)
                .pathMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
              /* =====================================================
               * 2. COMPLIANCE (Specific Methods & Paths)
               * ===================================================== */
              .pathMatchers(HttpMethod.GET,
                      "/compliance",
                      "/compliance/{id}",
                      "/compliance/entity/{entityId}",
                      "/compliance/summary")
              .hasAnyAuthority("ROLE_COMPLIANCE_OFFICER", "ROLE_FINANCIAL_OFFICER", "ROLE_ADMIN")
 
              .pathMatchers(HttpMethod.POST, "/compliance")
              .hasAnyAuthority("ROLE_COMPLIANCE_OFFICER", "ROLE_ADMIN")
 
              .pathMatchers(HttpMethod.PATCH, "/compliance/{id}")
              .hasAuthority("ROLE_COMPLIANCE_OFFICER")
 
              .pathMatchers(HttpMethod.DELETE, "/compliance/{id}")
              .hasAuthority("ROLE_ADMIN")
 
              /* =====================================================
               * 3. AUDIT (Specific Methods & Paths)
               * ===================================================== */
              .pathMatchers(HttpMethod.GET,
                      "/audit",
                      "/audit/{id}",
                      "/audit/officer/{officerId}")
              .hasAuthority("ROLE_GOVERNMENT_AUDITOR")
 
              .pathMatchers(HttpMethod.POST,
                      "/audit")
              .hasAuthority("ROLE_GOVERNMENT_AUDITOR")
              .pathMatchers(HttpMethod.PATCH,
                      "/audit/{id}")
              .hasAuthority("ROLE_GOVERNMENT_AUDITOR")
 
              .pathMatchers(HttpMethod.DELETE, "/audit/{id}")
              .hasAuthority("ROLE_ADMIN")
 
              .pathMatchers(HttpMethod.GET, "/audit/summary")
              .hasAnyAuthority("ROLE_GOVERNMENT_AUDITOR", "ROLE_PROGRAM_MANAGER", "ROLE_ADMIN")
 
              /* =====================================================
               * 4. REPORTS (Specific Paths)
               * ===================================================== */
              .pathMatchers(HttpMethod.POST, "/reports/generate/TAX")
              .hasAuthority("ROLE_GOVERNMENT_AUDITOR")
 
              .pathMatchers(HttpMethod.GET, 
                      "/reports/scope/TAX",
                      "/reports/{id}")
              .hasAuthority("ROLE_GOVERNMENT_AUDITOR")
 
              .pathMatchers(HttpMethod.GET, "/reports/summary")
              .hasAnyAuthority("ROLE_GOVERNMENT_AUDITOR", "ROLE_PROGRAM_MANAGER", "ROLE_FINANCIAL_OFFICER", "ROLE_ADMIN")
 
              .pathMatchers("/reports/analytics")
              .hasAnyAuthority("ROLE_GOVERNMENT_AUDITOR", "ROLE_PROGRAM_MANAGER", "ROLE_FINANCIAL_OFFICER", "ROLE_ADMIN")
 
              /* =====================================================
               * 5. PROGRAM MANAGER (Explicit First, then Broad)
               * ===================================================== */
              .pathMatchers(
                      "/reports/generate/PROGRAM",
                      "/reports/scope/PROGRAM",
                      "/subsidies/fetchByProgram/{programId}"
              ).hasAuthority("ROLE_PROGRAM_MANAGER")
 
              .pathMatchers("/programs/fetchAll").hasAnyAuthority("ROLE_PROGRAM_MANAGER","ROLE_CITIZEN")
              .pathMatchers(
                      "/api/resources/**",
                      "/api/budget/**",
                      "/programs/**"
              ).hasAuthority("ROLE_PROGRAM_MANAGER")
 
              /* =====================================================
               * 6. CITIZEN (Explicit First, then Broad)
               * ===================================================== */
              .pathMatchers("/applications/fetchByEntity/**","/applications/fetchByProgram/**").hasAnyAuthority("ROLE_FINANCIAL_OFFICER","ROLE_CITIZEN")
              .pathMatchers(
                      "/applications/save",
                      "/entities/createCitizen",
                      "/entities/updateCitizenById"
              ).hasAuthority("ROLE_CITIZEN")
              .pathMatchers(
            		  "/documents/**"
              ).permitAll()
 
 
              .pathMatchers(
                      "/api/taxation/enter_taxrecord",
                      "/api/disclosure/enter_disclosure"
              ).hasAuthority("ROLE_CITIZEN")
 
              .pathMatchers("/api/disclosure/entity/{entityId}").hasAnyAuthority("ROLE_CITIZEN","ROLE_FINANCIAL_OFFICER")
              .pathMatchers("/api/taxation/taxrecords/{taxId}").hasAnyAuthority("ROLE_CITIZEN","ROLE_FINANCIAL_OFFICER")
              .pathMatchers("/api/taxation/taxrecords/entity/{entityId}").hasAnyAuthority("ROLE_CITIZEN","ROLE_FINANCIAL_OFFICER")
              .pathMatchers("/api/disclosure/{disclosureId}").hasAnyAuthority("ROLE_CITIZEN","ROLE_FINANCIAL_OFFICER")
              .pathMatchers("/api/taxation/all_taxrecords").hasAnyAuthority("ROLE_ADMIN","ROLE_FINANCIAL_OFFICER")
              .pathMatchers("/api/disclosure/all_disclosures").hasAnyAuthority("ROLE_ADMIN","ROLE_FINANCIAL_OFFICER")

              /* =====================================================
               * 7. FINANCIAL OFFICER (Explicit First, then Broad)
               * ===================================================== */
              .pathMatchers(
                      "/reports/generate/SUBSIDY",
                      "/reports/scope/SUBSIDY"
              ).hasAuthority("ROLE_FINANCIAL_OFFICER")
 
              .pathMatchers(
                      "/applications/**",
                      "/subsidies/**",
                      "/api/taxation/taxrecords/verify/{taxId}",
                      "/api/disclosure/{disclosureId}/validate"
              ).hasAuthority("ROLE_FINANCIAL_OFFICER")
 
              /* =====================================================
               * 8. ADMIN (Explicit First, then Broad)
               * ===================================================== */

              .pathMatchers(
                      "/api/admin/**",
                      "/api/notifications/**"
              ).hasAuthority("ROLE_ADMIN")
 
              /* =====================================================
               * 9. FALLBACK
               * ===================================================== */
                .anyExchange().authenticated()
            )
            .build();
    }
}