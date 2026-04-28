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

        // Fetch reports by scope
        List<Report> programReports =
                reportRepository.findByScope(ReportScope.PROGRAM);

        List<Report> subsidyReports =
                reportRepository.findByScope(ReportScope.SUBSIDY);

        List<Report> taxReports =
                reportRepository.findByScope(ReportScope.TAX);

        // Get latest report per scope
        Report programReport =
                programReports.isEmpty() ? null
                        : programReports.get(programReports.size() - 1);

        Report subsidyReport =
                subsidyReports.isEmpty() ? null
                        : subsidyReports.get(subsidyReports.size() - 1);

        Report taxReport =
                taxReports.isEmpty() ? null
                        : taxReports.get(taxReports.size() - 1);

        // ================= PROGRAM ANALYTICS =================
        double programUtilization = 0;

        if (programReport != null &&
            programReport.getTotalPrograms() != null &&
            programReport.getTotalPrograms() > 0) {

            programUtilization =
                    (programReport.getActivePrograms() * 100.0) /
                            programReport.getTotalPrograms();
        }

        // ================= SUBSIDY ANALYTICS =================
        double approvalRate = 0;
        double avgSubsidy = 0;

        if (subsidyReport != null &&
            subsidyReport.getApplicationsReceived() != null &&
            subsidyReport.getApplicationsReceived() > 0) {

            approvalRate =
                    (subsidyReport.getApprovedSubsidies() * 100.0) /
                            subsidyReport.getApplicationsReceived();

            if (subsidyReport.getApprovedSubsidies() != null &&
                subsidyReport.getApprovedSubsidies() > 0 &&
                subsidyReport.getAmountDistributed() != null) {

                avgSubsidy =
                        subsidyReport.getAmountDistributed() /
                                subsidyReport.getApprovedSubsidies();
            }
        }

        // ================= TAX ANALYTICS =================
        double avgRevenue = 0;

        if (taxReport != null &&
            taxReport.getTotalTaxpayers() != null &&
            taxReport.getTotalTaxpayers() > 0 &&
            taxReport.getRevenueCollected() != null) {

            avgRevenue =
                    taxReport.getRevenueCollected() /
                            taxReport.getTotalTaxpayers();
        }

        // ================= RESPONSE =================
        return new ReportAnalyticsDTO(

                // PROGRAM
                programReport != null ? programReport.getTotalPrograms() : 0,
                programReport != null ? programReport.getActivePrograms() : 0,
                programReport != null ? programReport.getBudgetUsed() : 0,
                programUtilization,

                // SUBSIDY
                subsidyReport != null ? subsidyReport.getApplicationsReceived() : 0,
                subsidyReport != null ? subsidyReport.getApprovedSubsidies() : 0,
                approvalRate,
                avgSubsidy,

                // TAX
                taxReport != null ? taxReport.getTotalTaxpayers() : 0,
                taxReport != null ? taxReport.getRevenueCollected() : 0,
                avgRevenue
        );
    }
}