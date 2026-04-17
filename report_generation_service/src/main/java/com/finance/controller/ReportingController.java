package com.finance.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.finance.enums.ReportScope;
import com.finance.model.Report;
import com.finance.service.ReportingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportingController {

    private final ReportingService reportingService;

    @PostMapping("/generate/{scope}")
    public Report generate(@PathVariable ReportScope scope) {
        return reportingService.generateReport(scope);
    }

    @GetMapping("/scope/{scope}")
    public List<Report> getByScope(@PathVariable ReportScope scope) {
        return reportingService.getReportsByScope(scope);
    }

    @GetMapping("/{id}")
    public Report getById(@PathVariable Long id) {
        return reportingService.getReportById(id);
    }

    @GetMapping("/summary")
    public Map<ReportScope, Report> getSummary() {
        return reportingService.getSummaryReports();
    }
}
