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

	@PostMapping("/generate")
	public ResponseEntity<ReportResponseDTO> generateReport(@RequestParam ReportScope scope) {
		return ResponseEntity.ok(reportingService.generateReport(scope));
	}

	@GetMapping("/scope/{scope}")
	public ResponseEntity<List<ReportResponseDTO>> getReportsByScope(@PathVariable ReportScope scope) {
		return ResponseEntity.ok(reportingService.getReportsByScope(scope));
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