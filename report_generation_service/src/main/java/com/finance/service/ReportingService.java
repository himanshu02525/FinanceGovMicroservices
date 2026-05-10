package com.finance.service;

import java.util.List;
import java.util.Map;

import com.finance.dto.AnalyticsDTO;
import com.finance.dto.ReportResponseDTO;
import com.finance.enums.ReportScope;

public interface ReportingService {

	ReportResponseDTO getReportById(Long id);

	Map<ReportScope, ReportResponseDTO> getSummaryReports();

	AnalyticsDTO getAnalytics();

	List<ReportResponseDTO> getAll();

	ReportResponseDTO generateReport(ReportScope scope, Long id, Integer year);
}