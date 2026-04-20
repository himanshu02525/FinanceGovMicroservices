package com.finance.service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.finance.client.SubsidyClient;
import com.finance.client.TaxClient;
import com.finance.enums.ReportScope;
import com.finance.exceptions.ReportNotFoundException;
import com.finance.model.Report;
import com.finance.repository.ReportRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportingServiceImpl implements ReportingService {

    private static final Logger log =
            LoggerFactory.getLogger(ReportingServiceImpl.class);

    private final ReportRepository reportRepository;
    private final SubsidyClient subsidyClient;
    private final TaxClient taxClient;

    @Override
    public Report generateReport(ReportScope scope) {

        log.info("Generating report for scope: {}", scope);

        Report report = new Report();
        report.setScope(scope);
        report.setGeneratedDate(LocalDateTime.now());

        try {

            if (scope == ReportScope.PROGRAM) {
                log.debug("Fetching PROGRAM summary from Subsidy Service");
                Map<String, Object> program = subsidyClient.getProgramSummary();

                report.setTotalPrograms((Integer) program.get("totalPrograms"));
                report.setActivePrograms((Integer) program.get("activePrograms"));
                report.setBudgetUsed((Double) program.get("budgetUsed"));
            }

            if (scope == ReportScope.SUBSIDY) {
                log.debug("Fetching SUBSIDY summary from Subsidy Service");
                Map<String, Object> subsidy = subsidyClient.getSubsidySummary();

                report.setApplicationsReceived((Integer) subsidy.get("applicationsReceived"));
                report.setApprovedSubsidies((Integer) subsidy.get("approvedSubsidies"));
                report.setAmountDistributed((Double) subsidy.get("amountDistributed"));
            }

            if (scope == ReportScope.TAX) {
                log.debug("Fetching TAX summary from Tax Service");
                Map<String, Object> tax = taxClient.getTaxStatistics();

                report.setTotalTaxpayers((Integer) tax.get("totalTaxpayers"));
                report.setRevenueCollected((Double) tax.get("revenueCollected"));
            }

        } catch (Exception e) {
            log.error("Error while generating report for scope {}", scope, e);
            throw e;
        }

        Report saved = reportRepository.save(report);
        log.info("Report generated successfully with ID {}", saved.getReportId());

        return saved;
    }

    @Override
    public List<Report> getReportsByScope(ReportScope scope) {
        log.info("Fetching reports for scope: {}", scope);
        return reportRepository.findByScope(scope);
    }

    @Override
    public Report getReportById(Long id) {
        log.info("Fetching report with ID: {}", id);
        return reportRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Report not found with ID {}", id);
                    return new ReportNotFoundException("Report not found with ID: " + id);
                });
    }

    @Override
    public Map<ReportScope, Report> getSummaryReports() {

        log.info("Generating dashboard summary");

        Map<ReportScope, Report> summary =
                new EnumMap<>(ReportScope.class);

        summary.put(ReportScope.PROGRAM, generateReport(ReportScope.PROGRAM));
        summary.put(ReportScope.SUBSIDY, generateReport(ReportScope.SUBSIDY));
        summary.put(ReportScope.TAX, generateReport(ReportScope.TAX));

        log.info("Dashboard summary generated successfully");
        return summary;
    }
}