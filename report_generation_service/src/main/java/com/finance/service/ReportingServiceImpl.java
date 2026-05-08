package com.finance.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.finance.client.SubsidyClient;
import com.finance.client.TaxClient;
import com.finance.dto.ReportAnalyticsDTO;
import com.finance.enums.ReportScope;
import com.finance.model.Report;
import com.finance.repository.ReportRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportingServiceImpl implements ReportingService {

	private final ReportRepository reportRepository;
	private final TaxClient taxClient;
	private final SubsidyClient subsidyClient;
	private final ObjectMapper objectMapper;

	@Override
	public Report generateReport(ReportScope scope) {
		try {
			Map<String, Object> reportData = new HashMap<>();

			// Aggregating data based on scope
			if (scope == ReportScope.TAX || scope == ReportScope.TAX) {
				reportData.put("taxMetrics", taxClient.getTaxStatistics());
			}

			if (scope == ReportScope.SUBSIDY || scope == ReportScope.PROGRAM) {
				reportData.put("programMetrics", subsidyClient.getProgramSummary());
			}
			if (scope == ReportScope.SUBSIDY || scope == ReportScope.SUBSIDY) {
				reportData.put("subsidyMetrics", subsidyClient.getSubsidySummary());
			}

			reportData.put("generatedAt", LocalDateTime.now());

			String jsonData = objectMapper.writeValueAsString(reportData);

			Report report = new Report();
			report.setScope(scope);
			report.setGeneratedDate(LocalDateTime.now());
			report.setMetrics(jsonData);

			return reportRepository.save(report);
		} catch (Exception e) {
			log.error("Failed to generate report for scope: {}", scope, e);
			throw new RuntimeException("Reporting Service Error: " + e.getMessage());
		}
	}

	@Override
	public ReportAnalyticsDTO getAnalytics() {
		List<Report> allReports = reportRepository.findAll();

		Map<ReportScope, Long> counts = allReports.stream()
				.collect(Collectors.groupingBy(Report::getScope, Collectors.counting()));

		ReportAnalyticsDTO dto = new ReportAnalyticsDTO();
		dto.setTotalReports(allReports.size());
		dto.setReportsByScope(counts);

		return dto;
	}

	@Override
	public List<Report> getReportsByScope(ReportScope scope) {
		log.info("Fetching all historical reports for scope: {}", scope);
		// Directly calls the repository to find reports by the Enum type
		return reportRepository.findByScope(scope);
	}

	@Override
	public Report getReportById(Long id) {
		log.info("Fetching detailed report for ID: {}", id);
		// Uses findById and throws an exception if the report doesn't exist
		return reportRepository.findById(id).orElseThrow(() -> new RuntimeException("Report not found with id: " + id));
	}

	@Override
	public Map<ReportScope, Report> getSummaryReports() {
		log.info("Generating summary of latest reports per scope");

		Map<ReportScope, Report> summary = new HashMap<>();

		// Iterate through all possible scopes (TAX, SUBSIDY, OVERALL)
		for (ReportScope scope : ReportScope.values()) {
			// Find reports for this scope, sorted by creation date descending, and take the
			// first
			reportRepository.findTopByScopeOrderByCreatedAtDesc(scope).ifPresent(report -> summary.put(scope, report));
		}

		return summary;
	}
}