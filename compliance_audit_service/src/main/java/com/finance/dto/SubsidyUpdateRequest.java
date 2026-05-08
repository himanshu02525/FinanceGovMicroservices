package com.finance.dto;

import com.finance.enums.SubsidyStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubsidyUpdateRequest {
	private SubsidyStatus status;
}
