package com.finance.gateway.security;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Jwts;
import reactor.core.publisher.Mono;

@Component
public class JwtFilter implements GlobalFilter {

	// ✅ Same secret key used by IAM service
	private static final String SECRET_KEY = "536b83133b8246d8e6319d707200ef0a3ad8aa563071de396d5d25e68c95c4bd";

	@SuppressWarnings("deprecation")
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

		String path = exchange.getRequest().getURI().getPath();

		// ✅ Public endpoints (no JWT required)
		if (path.startsWith("/api/auth/")) {
			return chain.filter(exchange);
		}

		// ✅ Get Authorization header safely
		String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return exchange.getResponse().setComplete();
		}

		// ✅ Extract token
		String token = authHeader.substring(7);

		try {
			// ✅ Validate JWT (throws exception if invalid)
			Jwts.parser().setSigningKey(SECRET_KEY.getBytes()).build().parseClaimsJws(token);

		} catch (Exception ex) {
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return exchange.getResponse().setComplete();
		}

		// ✅ Token valid → continue routing
		return chain.filter(exchange);
	}
}