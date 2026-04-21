package com.finance.controller;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finance.entity.AuditLog;
import com.finance.repository.AuditLogRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    /**
     * Fetches every single action recorded in the system.
     * Restricted to ADMIN or GOVERNMENT_AUDITOR.
     */
    @GetMapping("/logs")
    @PreAuthorize("hasAnyRole('ADMIN', 'GOVERNMENT_AUDITOR')")
    public ResponseEntity<List<AuditLog>> getAllLogs() {
        return ResponseEntity.ok(auditLogRepository.findAll());
    }

    /**
     * Filter logs by a specific user (e.g., see everything 'ritesh@gov.in' did).
     */
    @GetMapping("/logs/user/{email}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GOVERNMENT_AUDITOR')")
    public ResponseEntity<List<AuditLog>> getLogsByUser(@PathVariable String email) {
        return ResponseEntity.ok(auditLogRepository.findByActorEmail(email));
    }

    /**
     * Filter logs by action (e.g., see all 'USER_LOGOUT' events).
     */
    @GetMapping("/logs/action/{action}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GOVERNMENT_AUDITOR')")
    public ResponseEntity<List<AuditLog>> getLogsByAction(@PathVariable String action) {
        return ResponseEntity.ok(auditLogRepository.findByAction(action));
    }
}