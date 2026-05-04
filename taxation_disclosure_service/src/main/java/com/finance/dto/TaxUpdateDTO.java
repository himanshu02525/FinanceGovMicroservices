package com.finance.dto;

import com.finance.enums.TaxStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class TaxUpdateDTO {
	private TaxStatus status;
}
