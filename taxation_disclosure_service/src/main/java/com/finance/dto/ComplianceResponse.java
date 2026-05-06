package com.finance.dto;
 
import lombok.Data;
 
@Data
public class ComplianceResponse {
    private Long complianceId;
    private Long entityId;
    private Long referenceId;
    private String type;
    private String notes;
}