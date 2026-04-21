package com.finance.dto;

import javax.print.attribute.standard.Severity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddFindingCreateRequest {

	@NotBlank(message = "title is required")
	private String title;

	@NotBlank(message = "description is required")
	private String description;

	@NotNull(message = "severity is required")
	private Severity severity;

}
