package com.finance.service;

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

		Map<String, Object> metrics = switch (scope) {
		case TAX -> taxClient.getTaxStatistics(year);
		case PROGRAM -> subsidyClient.getProgramSummary(id);
		case SUBSIDY -> subsidyClient.getSubsidySummary();
		default -> throw new IllegalArgumentException("Unexpected value: " + scope);
		};

		Report report = new Report();
		report.setMetrics(objectMapper.valueToTree(metrics));
		report.setScope(scope);
		report.setReportName(reportName);

		Report saved = reportRepository.save(report);
		return mapToDTO(saved);
	}

	private ReportResponseDTO mapToDTO(Report report) {

		Object metricsObject = objectMapper.convertValue(report.getMetrics(), Object.class);

		return new ReportResponseDTO(report.getReportId(), report.getScope(), metricsObject, report.getGeneratedDate(),
				report.getReportName());
	}

}