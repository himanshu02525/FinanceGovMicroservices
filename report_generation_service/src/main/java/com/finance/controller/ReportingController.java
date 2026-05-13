package com.finance.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.finance.dto.AnalyticsDTO;
import com.finance.dto.ReportResponseDTO;
import com.finance.enums.ReportScope;
import com.finance.service.ReportingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportingController {

	private final ReportingService reportingService;

	@GetMapping
	public ResponseEntity<List<ReportResponseDTO>> fetchAll() {
		return ResponseEntity.ok(reportingService.getAll());
	}

	@PostMapping("/generate-by-scope")
	public ResponseEntity<ReportResponseDTO> generateReport(@RequestParam ReportScope scope,
			@RequestParam(required = false) Long id, @RequestParam(required = false) Integer year,
			@RequestParam(required = false) String reportName) {

		return ResponseEntity.ok(reportingService.generateReport(scope, id, year, reportName));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ReportResponseDTO> getReportById(@PathVariable Long id) {
		return ResponseEntity.ok(reportingService.getReportById(id));
	}

	@GetMapping("/summary")
	public ResponseEntity<Map<ReportScope, ReportResponseDTO>> getSummaryReports() {
		return ResponseEntity.ok(reportingService.getSummaryReports());
	}

	@GetMapping("/analytics")
	public ResponseEntity<AnalyticsDTO> getAnalytics() {
		return ResponseEntity.ok(reportingService.getAnalytics());
	}
}