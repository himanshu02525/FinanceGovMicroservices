package com.finance.service;

import com.finance.entity.AuditLog;
import com.finance.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j // Adding logs here helps you see the audit happening in your console too
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Records a security-relevant action into the database.
     * Use this whenever a sensitive change is made (e.g., Role updates, Deletions).
     */
    public void logAction(String actor, String action, String target, String details, HttpServletRequest request) {
        
        // We use the Builder pattern here (from your AuditLog entity) 
        // to keep the code clean and readable.
        AuditLog auditEntry = AuditLog.builder()
                .actorEmail(actor)
                .action(action)
                .targetEmail(target)
                .details(details)
            
                .ipAddress(request.getRemoteAddr()) 
                .timestamp(LocalDateTime.now())
                .build();
        
        auditLogRepository.save(auditEntry);
        log.info("Audit Log Saved: {} performed {} on {}", actor, action, target);
    }
}