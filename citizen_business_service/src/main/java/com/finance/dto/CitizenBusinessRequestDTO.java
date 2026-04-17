package com.finance.dto;

import com.finance.enums.Type;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitizenBusinessRequestDTO {
	@NotBlank(message = "Name is required")
	@Size(max = 15, message = "Name must not exceed 15 characters")
	private String name;

	@NotNull(message = "Type is required")
	@Enumerated(EnumType.STRING)
	private Type type;

	private String address;

	@NotBlank(message = "Contact number is required")
	@Pattern(regexp = "^[0-9]{10}$", message = "Only digits allowed")
	private String contactInfo;
}