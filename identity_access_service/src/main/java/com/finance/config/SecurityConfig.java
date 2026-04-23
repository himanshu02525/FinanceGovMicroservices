package com.finance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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

import lombok.RequiredArgsConstructor;

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

				.exceptionHandling(ex -> ex.authenticationEntryPoint(globalExceptionHandler).accessDeniedHandler(

						globalExceptionHandler))

				.authorizeHttpRequests(auth -> auth

						/* ================= PUBLIC ================= */

						.requestMatchers("/api/auth/**", "/swagger-ui/**", "/api/v3/**","/actuator/health","/actuator/info").permitAll()

						/* ================= COMPLIANCE ================= */

						// View Compliance (GET)
						.requestMatchers(HttpMethod.GET, "/compliance", "/compliance/{id}",
								"/compliance/entity/{entityId}", "/compliance/summary")
						.hasAnyAuthority("ROLE_COMPLIANCE_OFFICER", "ROLE_FINANCIAL_OFFICER", "ROLE_ADMIN")

						// Create Compliance (POST)
						.requestMatchers(HttpMethod.POST, "/compliance")
						.hasAnyAuthority("ROLE_COMPLIANCE_OFFICER", "ROLE_ADMIN")

						// Update Compliance (PUT / PATCH)

						.requestMatchers(HttpMethod.PATCH, "/compliance/{id}")
						.hasAnyAuthority("ROLE_COMPLIANCE_OFFICER")

						// Delete Compliance (DELETE)
						.requestMatchers(HttpMethod.DELETE, "/compliance/{id}").hasAuthority("ROLE_ADMIN")

						// ================= REPORTS =================

						// Compliance Reports
						.requestMatchers(HttpMethod.GET, "/api/reports/compliance")
						.hasAuthority("ROLE_COMPLIANCE_OFFICER")

						// Tax Reports
						.requestMatchers(HttpMethod.GET, "/api/reports/tax").hasAuthority("ROLE_COMPLIANCE_OFFICER")

						// ================= TAXATION =================

						// Verify Tax Records
						.requestMatchers(HttpMethod.POST, "/taxation/taxrecords/{entityId}/verify")
						.hasAuthority("ROLE_COMPLIANCE_OFFICER")

						// View Tax Record
						.requestMatchers(HttpMethod.GET, "/taxation/taxrecords/{taxId}")
						.hasAuthority("ROLE_COMPLIANCE_OFFICER")

						// Update Tax Record
						.requestMatchers(HttpMethod.PUT, "/taxation/taxrecords/{taxId}")
						.hasAuthority("ROLE_COMPLIANCE_OFFICER")

						.requestMatchers(HttpMethod.PATCH, "/taxation/taxrecords/{taxId}")
						.hasAuthority("ROLE_COMPLIANCE_OFFICER")

						// Delete Tax Record
						.requestMatchers(HttpMethod.DELETE, "/taxation/taxrecords/{taxId}").hasAuthority("ROLE_ADMIN")

						// Compliance Officer exclusive write access

						.requestMatchers("/compliance/**").hasAuthority("ROLE_COMPLIANCE_OFFICER")

						/* ================= AUDIT ================= */

						// View Audits
						.requestMatchers(HttpMethod.GET, "/audit", "/audit/{id}", "/audit/officer/{officerId}")
						.hasAuthority("ROLE_GOVERNMENT_AUDITOR")
						.requestMatchers(HttpMethod.POST, "/audit")
						.hasAuthority("ROLE_GOVERNMENT_AUDITOR")
						.requestMatchers(HttpMethod.POST, "/audit/{id}")
						.hasAuthority("ROLE_GOVERNMENT_AUDITOR")
						.requestMatchers(HttpMethod.DELETE, "/audit/{id}")
						.hasAuthority("ROLE_ADMIN")
						
						.requestMatchers(HttpMethod.GET, "/audit/summary")
						.hasAnyAuthority("ROLE_GOVERNMENT_AUDITOR", "ROLE_PROGRAM_MANAGER", "ROLE_ADMIN")

						// ================= REPORTS =================

						// Generate TAX Report
						.requestMatchers(HttpMethod.POST, "/reports/generate/TAX")
						.hasAuthority("ROLE_GOVERNMENT_AUDITOR")

						// TAX Report Scope
						.requestMatchers(HttpMethod.GET, "/reports/scope/TAX").hasAuthority("ROLE_GOVERNMENT_AUDITOR")

						// Reports Summary
						.requestMatchers(HttpMethod.GET, "/reports/summary").hasAuthority("ROLE_GOVERNMENT_AUDITOR")

						// View Report by ID
						.requestMatchers(HttpMethod.GET, "/reports/{id}").hasAuthority("ROLE_GOVERNMENT_AUDITOR")

						.requestMatchers("/reports/analytics")

						.hasAuthority("ROLE_GOVERNMENT_AUDITOR")

						.requestMatchers("/tax/summary")

						.hasAuthority("ROLE_GOVERNMENT_AUDITOR")

						/* ================= FINANCIAL_OFFICER ================= */

						.requestMatchers("/subsidy/*").hasAuthority("ROLE_FINANCIAL_OFFICER")

						// .requestMatchers("/applications/fetchAll").hasAuthority("ROLE_FINANCIAL_OFFICER")

						// .requestMatchers("/applications/fetchByProgram").hasAuthority("ROLE_FINANCIAL_OFFICER")

						.requestMatchers("/applications/approve/{id}").hasAuthority("ROLE_FINANCIAL_OFFICER")

						.requestMatchers("/applications/reject/{id}").hasAuthority("ROLE_FINANCIAL_OFFICER")

						.requestMatchers("/taxation/taxrecords/entity/{entityId}/verify")

						.hasAuthority("ROLE_FINANCIAL_OFFICER").requestMatchers("/taxation/taxrecords/{taxId}/verify")

						.hasAuthority("ROLE_FINANCIAL_OFFICER").requestMatchers("/disclosure/{disclosureId}")

						.hasAuthority("ROLE_FINANCIAL_OFFICER")

						.requestMatchers("/disclosure/{enitityId}/validate-disclosure")

						.hasAuthority("ROLE_FINANCIAL_OFFICER").requestMatchers("/disclosure/{disclosureId}/validate")

						.hasAuthority("ROLE_FINANCIAL_OFFICER").requestMatchers("/reports/generate/SUBSIDY")

						.hasAuthority("ROLE_FINANCIAL_OFFICER").requestMatchers("/reports/scope/SUBSIDY")

						.hasAuthority("ROLE_FINANCIAL_OFFICER").requestMatchers("/reports/summary")

						.hasAuthority("ROLE_FINANCIAL_OFFICER").requestMatchers("/applications/fetchByEntity/{entityId}")

						.hasAuthority("ROLE_FINANCIAL_OFFICER").requestMatchers("/taxation/taxrecords/{taxId}")
						.hasAuthority("ROLE_FINANCIAL_OFFICER").requestMatchers("/disclosure/all_disclosures")

						.hasAuthority("ROLE_FINANCIAL_OFFICER").requestMatchers("/taxation/admin/all_taxrecords")
						
						.hasAuthority("ROLE_FINANCIAL_OFFICER").requestMatchers("/subsidies/save")
						.hasAuthority("ROLE_FINANCIAL_OFFICER").requestMatchers("/subsidies/fetchAll")
						.hasAuthority("ROLE_FINANCIAL_OFFICER").requestMatchers("/subsidies/fetchByEntity/{entityId}")
						.hasAuthority("ROLE_FINANCIAL_OFFICER").requestMatchers("/subsidies/fetch/{id}")
						.hasAuthority("ROLE_FINANCIAL_OFFICER").requestMatchers("/subsidies/summary")

						.hasAuthority("ROLE_FINANCIAL_OFFICER").requestMatchers("/reports/analytics")

						.hasAuthority("ROLE_FINANCIAL_OFFICER")

						/* ================= PROGRAM MANAGER ================= */

						.requestMatchers("/api/resources/**", "/api/budget/**", "/programs/**")

						.hasAuthority("ROLE_PROGRAM_MANAGER").requestMatchers("/reports/generate/PROGRAM")

						.hasAuthority("ROLE_PROGRAM_MANAGER").requestMatchers("/reports/scope/PROGRAM")

						.hasAuthority("ROLE_PROGRAM_MANAGER").requestMatchers("/reports/summary")

						.hasAuthority("ROLE_PROGRAM_MANAGER").requestMatchers("/reports/analytics")
						
						.hasAuthority("ROLE_PROGRAM_MANAGER").requestMatchers("/programs/save")
						.hasAuthority("ROLE_PROGRAM_MANAGER").requestMatchers("/programs/update/{id}")
						.hasAuthority("ROLE_PROGRAM_MANAGER").requestMatchers("/programs/delete/{id}")
						.hasAuthority("ROLE_PROGRAM_MANAGER").requestMatchers("/programs/fetch/{id}")
						.hasAuthority("ROLE_PROGRAM_MANAGER").requestMatchers("/programs/fetchAll")
						.hasAuthority("ROLE_PROGRAM_MANAGER").requestMatchers("/programs/fetchByStatus/{status}")
						.hasAuthority("ROLE_PROGRAM_MANAGER").requestMatchers("/programs/summary")
						.hasAuthority("ROLE_PROGRAM_MANAGER").requestMatchers("/subsidies/fetchByProgram/{programId}")

						.hasAuthority("ROLE_PROGRAM_MANAGER")

						/* ================= ANALYTICS ================= */

						.requestMatchers("/api/analytics/reports", "/api/analytics/dashboard")

						.hasAnyAuthority("ROLE_ADMIN", "ROLE_GOVERNMENT_AUDITOR")

						/* ================= CITIZEN ================= */

						.requestMatchers("/compliance/entity/{entityId}").hasAuthority("ROLE_CITIZEN")

						.requestMatchers("/entities/createCitizen").hasAuthority("ROLE_CITIZEN")

						.requestMatchers("/entities/updateCitizenById").hasAuthority("ROLE_CITIZEN")

						.requestMatchers("/applications/fetchByEntity").hasAuthority("ROLE_CITIZEN")

						.requestMatchers("/programs/fetchAll").hasAuthority("ROLE_CITIZEN")

						.requestMatchers("/documents/uploadDoc").hasAuthority("ROLE_CITIZEN")

						.requestMatchers("/documents/updateDoc/**").hasAuthority("ROLE_CITIZEN")
						.requestMatchers("/enter_taxrecord/**").hasAuthority("ROLE_CITIZEN")
						.requestMatchers("/taxrecords/{taxId}").hasAuthority("ROLE_CITIZEN")

						.requestMatchers("/disclosure/enter_disclosure").hasAuthority("ROLE_CITIZEN")

						.requestMatchers("/disclosure/all_disclosures").hasAuthority("ROLE_CITIZEN")

						.requestMatchers("/disclosure/{disclosureId}").hasAuthority("ROLE_CITIZEN")

						/* ================= ADMIN ================= */

						.requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

						.requestMatchers("/entities/createCitizen").hasAuthority("ROLE_ADMIN")

						.requestMatchers("/entities/getAllEntity").hasAuthority("ROLE_ADMIN")

						.requestMatchers("/entities/getCitizenById").hasAuthority("ROLE_ADMIN")

						.requestMatchers("/entities/deleteById").hasAuthority("ROLE_ADMIN")

						.requestMatchers("/entities/approveCitizen").hasAuthority("ROLE_ADMIN")

						.requestMatchers("/reports/**").hasAuthority("ROLE_ADMIN").requestMatchers("/disclosure/all")

						.hasAuthority("ROLE_ADMIN").requestMatchers("/taxation/all_taxrecords")

						.hasAuthority("ROLE_ADMIN").requestMatchers("/documents/getAllDocument")

						.hasAuthority("ROLE_ADMIN").requestMatchers("/documents/getAllDocument")

						.hasAuthority("ROLE_ADMIN").requestMatchers("/documents/verify").hasAuthority("ROLE_ADMIN")

						.requestMatchers("/documents/reject").hasAuthority("ROLE_ADMIN")

						.requestMatchers("/reports/analytics").hasAuthority("ROLE_ADMIN")

						.requestMatchers("/disclosure/all_disclosures").hasAuthority("ROLE_ADMIN")
						
						.requestMatchers("/api/notifications/getAllNotification").hasAuthority("ROLE_ADMIN")

						/* ================= FALLBACK ================= */

						.anyRequest().authenticated())

				// Stateless JWT session

				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				.authenticationProvider(authenticationProvider)

				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

				.logout(logout -> logout.logoutUrl("/api/auth/logout").addLogoutHandler(logoutHandler)

						.logoutSuccessHandler((req, res, auth) -> {

							SecurityContextHolder.clearContext();

							res.setStatus(200);

							res.setContentType("application/json");

							res.getWriter().write("{\"message\":\"Logout successful\"}");

						}));

		return http.build();

	}

}
