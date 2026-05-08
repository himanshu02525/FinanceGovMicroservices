package com.finance.service;

import java.util.List;
import java.util.Map;

import com.finance.dto.AnalyticsDTO;
import com.finance.dto.ReportResponseDTO;
import com.finance.enums.ReportScope;

public interface ReportingService {
	ReportResponseDTO generateReport(ReportScope scope);

	List<ReportResponseDTO> getReportsByScope(ReportScope scope);

	ReportResponseDTO getReportById(Long id);

	Map<ReportScope, ReportResponseDTO> getSummaryReports();

	AnalyticsDTO getAnalytics();
}