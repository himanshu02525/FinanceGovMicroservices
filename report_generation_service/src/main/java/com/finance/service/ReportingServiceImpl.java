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
import com.finance.dto.ReportAnalyticsDTO;
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

        // ---------------- PROGRAM ----------------
        if (scope == ReportScope.PROGRAM) {
            Map<String, Object> program = subsidyClient.getProgramSummary();

            report.setTotalPrograms(((Number) program.get("totalPrograms")).intValue());
            report.setActivePrograms(((Number) program.get("activePrograms")).intValue());
            report.setBudgetUsed(((Number) program.get("budgetUsed")).doubleValue());
        }

        // ---------------- SUBSIDY ✅ FIXED ----------------
        if (scope == ReportScope.SUBSIDY) {
            Map<String, Object> subsidy = subsidyClient.getSubsidySummary();

            report.setApplicationsReceived(
                    ((Number) subsidy.get("applicationsReceived")).intValue()
            );

            report.setApprovedSubsidies(
                    ((Number) subsidy.get("approvedSubsidies")).intValue()
            );

            Number amount = (Number) subsidy.get("amountDistributed");
            report.setAmountDistributed(amount.doubleValue());
        }

        // ---------------- TAX ----------------
        if (scope == ReportScope.TAX) {
            Map<String, Object> tax = taxClient.getTaxStatistics();

            report.setTotalTaxpayers(((Number) tax.get("totalTaxpayers")).intValue());
            report.setRevenueCollected(((Number) tax.get("revenueCollected")).doubleValue());
        }

        return reportRepository.save(report);
    }

    @Override
    public List<Report> getReportsByScope(ReportScope scope) {
        return reportRepository.findByScope(scope);
    }

    @Override
    public Report getReportById(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() ->
                        new ReportNotFoundException("Report not found with ID: " + id));
    }

    @Override
    public Map<ReportScope, Report> getSummaryReports() {
        Map<ReportScope, Report> summary =
                new EnumMap<>(ReportScope.class);

        summary.put(ReportScope.PROGRAM, generateReport(ReportScope.PROGRAM));
        summary.put(ReportScope.SUBSIDY, generateReport(ReportScope.SUBSIDY));
        summary.put(ReportScope.TAX, generateReport(ReportScope.TAX));

        return summary;
    }

    @Override
    public ReportAnalyticsDTO getAnalytics() {
        // unchanged – already correct
        return null;
    }
}