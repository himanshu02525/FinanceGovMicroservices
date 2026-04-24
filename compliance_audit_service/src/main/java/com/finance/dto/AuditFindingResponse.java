package com.finance.dto;

import java.time.LocalDateTime;

import javax.print.attribute.standard.Severity;

import lombok.Data;

@Data
public class AuditFindingResponse {
	private Long id;
	private String title;
	private String description;
	private Severity severity;
	private String status;
	private LocalDateTime createdAt;

}
