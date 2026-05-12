package com.finance.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.client.SubsidyClient;
import com.finance.client.TaxClient;
import com.finance.dto.AnalyticsDTO;
import com.finance.dto.ReportResponseDTO;
import com.finance.enums.ReportScope;
import com.finance.exceptions.ReportNotFoundException;
import com.finance.model.Report;
import com.finance.repository.ReportRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReportingServiceImpl implements ReportingService {

	private final ReportRepository reportRepository;
	private final TaxClient taxClient;
	private final SubsidyClient subsidyClient;
	private final ObjectMapper objectMapper;

	@Override
	public AnalyticsDTO getAnalytics() {

		AnalyticsDTO dto = new AnalyticsDTO();

		try {
			Map<String, Object> taxStatistics = taxClient.getTaxStatistics(null);
			if (taxStatistics != null && !taxStatistics.isEmpty()) {
				dto.setTaxDetails(taxStatistics);
			}
		} catch (Exception e) {
			log.error("Tax service failed", e);
			dto.setTaxDetails(null);
		}

		try {
			Map<String, Object> programSummary = subsidyClient.getProgramSummary(null);
			if (programSummary != null && !programSummary.isEmpty()) {
				dto.setProgramDetails(programSummary);
			}
		} catch (Exception e) {
			log.error("Program service failed", e);
			dto.setProgramDetails(null);
		}

		try {
			Map<String, Object> subsidySummary = subsidyClient.getSubsidySummary();
			if (subsidySummary != null && !subsidySummary.isEmpty()) {
				dto.setSubsidyDetails(subsidySummary);
			}
		} catch (Exception e) {
			dto.setSubsidyDetails(null);
		}

		return dto;
	}

	@Override
	public ReportResponseDTO getReportById(Long id) {

		log.info("Fetching detailed report for ID: {}", id);

		Report report = reportRepository.findById(id)
				.orElseThrow(() -> new ReportNotFoundException("Report not found with id: " + id));

		return mapToDTO(report);
	}

	@Override
	public Map<ReportScope, ReportResponseDTO> getSummaryReports() {

		log.info("Generating summary of latest reports per scope");

		Map<ReportScope, ReportResponseDTO> summary = new HashMap<>();

		for (ReportScope scope : ReportScope.values()) {

			reportRepository.findTopByScopeOrderByGeneratedDateDesc(scope)
					.ifPresent(report -> summary.put(scope, mapToDTO(report)));
		}

		return summary;
	}

	@Override
	public List<ReportResponseDTO> getAll() {
		List<Report> reports = reportRepository.findAll();

		if (reports.isEmpty()) {
			throw new ReportNotFoundException("No reports currently exist in the database.");
		}
		return reports.stream().map(this::mapToDTO).toList();
	}

	@Override
	public ReportResponseDTO generateReport(ReportScope scope, Long id, Integer year, String reportName) {
		// 1. Initialize the map to hold nested metric objects
		Map<String, Object> reportMetrics = new HashMap<>();

		if (scope == ReportScope.OVERALL) {
			// Populating multiple professional key-object pairs
			reportMetrics.put("taxAnalytics", taxClient.getTaxStatistics(year));
			reportMetrics.put("subsidyOverview", subsidyClient.getSubsidySummary());
			reportMetrics.put("programSpecification", subsidyClient.getProgramSummary(id));
		} else {
			// For specific scopes, we still wrap the result in a descriptive key
			// to maintain the { "key": { "data" } } format requested
			switch (scope) {
			case TAX -> reportMetrics.put("taxAnalytics", taxClient.getTaxStatistics(year));
			case PROGRAM -> reportMetrics.put("programSpecification", subsidyClient.getProgramSummary(id));
			case SUBSIDY -> reportMetrics.put("subsidyOverview", subsidyClient.getSubsidySummary());
			default -> throw new IllegalArgumentException("Unsupported report scope: " + scope);
			}
		}

		// 2. Persistence Logic
		Report report = new Report();
		report.setGeneratedDate(LocalDateTime.now());
		report.setReportName(reportName);
		report.setScope(scope);

		// Converts the Map into a JsonNode for your @JdbcTypeCode(SqlTypes.JSON) field
		report.setMetrics(objectMapper.valueToTree(reportMetrics));

		Report savedReport = reportRepository.save(report);

		// 3. Return the mapped DTO
		return mapToDTO(savedReport);
	}

	private ReportResponseDTO mapToDTO(Report report) {

		Object metricsObject = objectMapper.convertValue(report.getMetrics(), Object.class);

		return new ReportResponseDTO(report.getReportId(), report.getScope(), metricsObject, report.getGeneratedDate(),
				report.getReportName());
	}

}