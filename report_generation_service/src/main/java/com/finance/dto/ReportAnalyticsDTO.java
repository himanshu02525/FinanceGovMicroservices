package com.finance.dto;

import java.util.Map;

import com.finance.enums.ReportScope;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportAnalyticsDTO {
	private int totalReports;
	private Map<ReportScope, Long> reportsByScope;
}