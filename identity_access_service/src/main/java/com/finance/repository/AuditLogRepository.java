package com.finance.repository;

import com.finance.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByActorEmail(String email);
    List<AuditLog> findByAction(String action);
}