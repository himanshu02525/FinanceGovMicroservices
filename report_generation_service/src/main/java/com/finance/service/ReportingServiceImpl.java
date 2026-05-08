package com.finance.service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.client.SubsidyClient;
import com.finance.client.TaxClient;
import com.finance.dto.ReportAnalyticsDTO;
import com.finance.enums.ReportScope;
import com.finance.exceptions.ReportNotFoundException;
import com.finance.model.Report;
import com.finance.repository.ReportRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportingServiceImpl implements ReportingService {

	private static final Logger log = LoggerFactory.getLogger(ReportingServiceImpl.class);

	private final ReportRepository reportRepository;
	private final SubsidyClient subsidyClient;
	private final TaxClient taxClient;

	private final ObjectMapper objectMapper = new ObjectMapper(); // ✅ for JSON

	// ✅ Generate Report
	@Override
	public Report generateReport(ReportScope scope) {

		log.info("Generating report for scope: {}", scope);

		Report report = new Report();
		report.setScope(scope);
		report.setGeneratedDate(LocalDateTime.now());

		Map<String, Object> metrics = new HashMap<>();

		try {

			if (scope == ReportScope.PROGRAM) {
				Map<String, Object> program = subsidyClient.getProgramSummary();
				metrics.putAll(program);
			}

			if (scope == ReportScope.SUBSIDY) {
				Map<String, Object> subsidy = subsidyClient.getSubsidySummary();
				metrics.putAll(subsidy);
			}

			if (scope == ReportScope.TAX) {
				Map<String, Object> tax = taxClient.getTaxStatistics();
				metrics.putAll(tax);
			}

			// ✅ Convert Map → JSON String
			String metricsJson = objectMapper.writeValueAsString(metrics);
			report.setMetrics(metricsJson);

		} catch (Exception e) {
			log.error("Error generating report metrics", e);
			throw new RuntimeException("Failed to generate report");
		}

		return reportRepository.save(report);
	}

	// ✅ Get reports by scope
	@Override
	public List<Report> getReportsByScope(ReportScope scope) {
		return reportRepository.findByScope(scope);
	}

	// ✅ Get single report
	@Override
	public Report getReportById(Long id) {
		return reportRepository.findById(id)
				.orElseThrow(() -> new ReportNotFoundException("Report not found with ID: " + id));
	}

	// ✅ Generate all reports
	@Override
	public Map<ReportScope, Report> getSummaryReports() {

		Map<ReportScope, Report> summary = new EnumMap<>(ReportScope.class);

		summary.put(ReportScope.PROGRAM, generateReport(ReportScope.PROGRAM));
		summary.put(ReportScope.SUBSIDY, generateReport(ReportScope.SUBSIDY));
		summary.put(ReportScope.TAX, generateReport(ReportScope.TAX));

		return summary;
	}

	@Override
	public ReportAnalyticsDTO getAnalytics() {

		try {

			Report programReport = getLatestReport(ReportScope.PROGRAM);
			Report subsidyReport = getLatestReport(ReportScope.SUBSIDY);
			Report taxReport = getLatestReport(ReportScope.TAX);

			Map<String, Object> programMetrics = programReport != null
					? objectMapper.readValue(programReport.getMetrics(), Map.class)
					: new HashMap<>();

			Map<String, Object> subsidyMetrics = subsidyReport != null
					? objectMapper.readValue(subsidyReport.getMetrics(), Map.class)
					: new HashMap<>();

			Map<String, Object> taxMetrics = taxReport != null
					? objectMapper.readValue(taxReport.getMetrics(), Map.class)
					: new HashMap<>();

			// ✅ Extract values
			int totalPrograms = ((Number) programMetrics.getOrDefault("totalPrograms", 0)).intValue();
			int activePrograms = ((Number) programMetrics.getOrDefault("activePrograms", 0)).intValue();
			double budgetUsed = ((Number) programMetrics.getOrDefault("budgetUsed", 0)).doubleValue();

			int applicationsReceived = ((Number) subsidyMetrics.getOrDefault("applicationsReceived", 0)).intValue();
			int approvedSubsidies = ((Number) subsidyMetrics.getOrDefault("approvedSubsidies", 0)).intValue();
			double amountDistributed = ((Number) subsidyMetrics.getOrDefault("amountDistributed", 0)).doubleValue();

			int totalTaxpayers = ((Number) taxMetrics.getOrDefault("totalTaxpayers", 0)).intValue();
			double revenueCollected = ((Number) taxMetrics.getOrDefault("revenueCollected", 0)).doubleValue();

			// ✅ Calculations
			double programUtilization = totalPrograms > 0 ? (activePrograms * 100.0) / totalPrograms : 0;

			double approvalRate = applicationsReceived > 0 ? (approvedSubsidies * 100.0) / applicationsReceived : 0;

			double avgSubsidy = approvedSubsidies > 0 ? amountDistributed / approvedSubsidies : 0;

			double avgRevenue = totalTaxpayers > 0 ? revenueCollected / totalTaxpayers : 0;

			return new ReportAnalyticsDTO(totalPrograms, activePrograms, budgetUsed, programUtilization,

					applicationsReceived, approvedSubsidies, approvalRate, avgSubsidy,

					totalTaxpayers, revenueCollected, avgRevenue);

		} catch (Exception e) {
			throw new RuntimeException("Error calculating analytics", e);
		}
	}

	// ✅ Helper method
	private Report getLatestReport(ReportScope scope) {
		List<Report> reports = reportRepository.findByScope(scope);
		return reports.isEmpty() ? null : reports.get(reports.size() - 1);
	}
}