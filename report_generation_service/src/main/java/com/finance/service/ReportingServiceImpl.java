package com.finance.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.finance.client.SubsidyClient;
import com.finance.client.TaxClient;
import com.finance.dto.AnalyticsDTO;
import com.finance.dto.ReportResponseDTO;
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
	private final ModelMapper modelMapper;

	@Override
	public ReportResponseDTO generateReport(ReportScope scope) {
		try {
			Map<String, Object> reportData = new HashMap<>();

			switch (scope) {

			case TAX -> reportData.put("taxMetrics", taxClient.getTaxStatistics());

			case PROGRAM -> reportData.put("programMetrics", subsidyClient.getProgramSummary());

			case SUBSIDY -> reportData.put("subsidyMetrics", subsidyClient.getSubsidySummary());
			}

			reportData.put("generatedAt", LocalDateTime.now());

			String jsonData = objectMapper.writeValueAsString(reportData);

			Report report = new Report();
			report.setScope(scope);
			report.setGeneratedDate(LocalDateTime.now());
			report.setMetrics(jsonData);

			return modelMapper.map(reportRepository.save(report), ReportResponseDTO.class);

		} catch (Exception e) {
			log.error("Failed to generate report for scope: {}", scope, e);
			throw new RuntimeException("Reporting Service Error: " + e.getMessage());
		}
	}

	@Override
	public AnalyticsDTO getAnalytics() {

		AnalyticsDTO dto = new AnalyticsDTO();

		try {
			Map<String, Object> taxStatistics = taxClient.getTaxStatistics();
			if (taxStatistics != null && !taxStatistics.isEmpty()) {
				dto.setTaxDetails(taxStatistics);
			}
		} catch (Exception e) {
			log.error("Tax service failed", e);
			dto.setTaxDetails(null);
		}

		try {
			Map<String, Object> programSummary = subsidyClient.getProgramSummary();
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
	public List<ReportResponseDTO> getReportsByScope(ReportScope scope) {

		log.info("Fetching all historical reports for scope: {}", scope);

		List<Report> reports = reportRepository.findByScope(scope);

		return reports.stream().map(report -> modelMapper.map(report, ReportResponseDTO.class)).toList();
	}

	@Override
	public ReportResponseDTO getReportById(Long id) {

		log.info("Fetching detailed report for ID: {}", id);

		Report report = reportRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Report not found with id: " + id));

		return modelMapper.map(report, ReportResponseDTO.class);
	}

	@Override
	public Map<ReportScope, ReportResponseDTO> getSummaryReports() {

		log.info("Generating summary of latest reports per scope");

		Map<ReportScope, ReportResponseDTO> summary = new HashMap<>();

		for (ReportScope scope : ReportScope.values()) {
			reportRepository.findTopByScopeOrderByGeneratedDateDesc(scope)
					.ifPresent(report -> summary.put(scope, modelMapper.map(report, ReportResponseDTO.class)));
		}

		return summary;
	}
}