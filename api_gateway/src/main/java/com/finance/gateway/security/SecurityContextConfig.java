package com.finance.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;

@Configuration
public class SecurityContextConfig {

    @Bean
    public ServerSecurityContextRepository securityContextRepository() {
        // ✅ Stateless: no session, no cookie, no carry-over auth
        return NoOpServerSecurityContextRepository.getInstance();
    }
}
