package com.finance.service;

import com.finance.repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutService implements LogoutHandler {

    private final TokenRepository tokenRepository;
    private final AuditService auditService; 

    @Override
    public void logout(
            HttpServletRequest request, 
            HttpServletResponse response, 
            Authentication auth
    ) {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        // 1. Check if the request even has a Bearer token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        jwt = authHeader.substring(7);
        
        // 2. Find the token in our database 'Whitelist'
        var storedToken = tokenRepository.findByToken(jwt).orElse(null);

        if (storedToken != null) {
            String email = storedToken.getUser().getEmail();

            // 3. Record the logout in the Audit Log for compliance
            auditService.logAction(
                email, 
                "USER_LOGOUT", 
                email, 
                "Session terminated by user", 
                request
            );

            // 4. "Kill" the token so the JwtFilter will reject it next time
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            tokenRepository.save(storedToken);
            
            // 5. Clear the security context for the current thread
            SecurityContextHolder.clearContext();
            
            log.info("User {} has been logged out and token revoked.", email);
        }
    }
}