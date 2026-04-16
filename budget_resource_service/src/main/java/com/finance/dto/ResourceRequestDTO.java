package com.finance.dto;

import lombok.*;

@Getter
@Setter
public class ResourceRequestDTO {
	private Long programId;
	private String type;
	private Integer quantity;
	private String status;
}