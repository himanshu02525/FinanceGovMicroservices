package com.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@Builder // This is great—it lets you do AuditLog.builder().action("LOGIN").build()
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The 'Who': Email of the Admin or Officer who performed the action
    private String actorEmail;   

    // The 'What': A clear tag like "CREATE_USER" or "PASSWORD_RESET"
    private String action;       

    // The 'Whom': The email of the person who was affected by this action
    private String targetEmail;  

    // The 'Where': The IP address of the machine that sent the request
    private String ipAddress;    

    // The 'When': The exact moment the action was recorded
    private LocalDateTime timestamp;
    
    // The 'Why/How': A longer explanation of what specifically changed
    @Column(length = 500) // We give this extra space for detailed descriptions
    private String details;      
}