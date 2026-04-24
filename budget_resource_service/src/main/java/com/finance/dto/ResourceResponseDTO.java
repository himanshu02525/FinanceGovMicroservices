package com.finance.dto;

import lombok.*;

@Getter
@Setter
@Builder
public class ResourceResponseDTO {
	private Long resourceId;
	private Long programId;
	private String type;
	private Integer quantity;
	private String status;
}