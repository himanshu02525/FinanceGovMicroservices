package com.finance.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.finance.dto.ReportAnalyticsDTO;
import com.finance.enums.ReportScope;
import com.finance.model.Report;
import com.finance.service.ReportingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportingController {

    private static final Logger log =
            LoggerFactory.getLogger(ReportingController.class);

    private final ReportingService reportingService;

    @PostMapping("/generate/{scope}")
    public ResponseEntity<Report> generate(@PathVariable ReportScope scope) {
        log.info("API call: Generate report for scope {}", scope);
        return ResponseEntity.ok(reportingService.generateReport(scope));
    }

    @GetMapping("/scope/{scope}")
    public ResponseEntity<List<Report>> getByScope(@PathVariable ReportScope scope) {
        log.info("API call: Get reports by scope {}", scope);
        return ResponseEntity.ok(reportingService.getReportsByScope(scope));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Report> getById(@PathVariable Long id) {
        log.info("API call: Get report by ID {}", id);
        return ResponseEntity.ok(reportingService.getReportById(id));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<ReportScope, Report>> getSummary() {
        log.info("API call: Get dashboard summary");
        return ResponseEntity.ok(reportingService.getSummaryReports());
    }

    // ✅ ANALYTICS API (FIXED)
    @GetMapping("/analytics")
    public ResponseEntity<ReportAnalyticsDTO> getAnalytics() {
        log.info("API call: Get analytics data");
        return ResponseEntity.ok(reportingService.getAnalytics());
    }
}