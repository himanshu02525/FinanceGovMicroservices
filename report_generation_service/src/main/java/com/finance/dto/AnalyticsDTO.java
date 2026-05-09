package com.finance.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsDTO {

	private Map<String, Object> taxDetails;
	private Map<String, Object> programDetails;
	private Map<String, Object> subsidyDetails;
}