package com.finance.service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

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

    private final ReportRepository reportRepository;
    private final SubsidyClient subsidyClient;
    private final TaxClient taxClient;

    @Override
    public Report generateReport(ReportScope scope) {

        Report report = new Report();
        report.setScope(scope);
        report.setGeneratedDate(LocalDateTime.now());

        // -------- PROGRAM --------
        if (scope == ReportScope.PROGRAM) {
            Map<String, Object> program = subsidyClient.getProgramSummary();
            report.setTotalPrograms((Integer) program.get("totalPrograms"));
            report.setActivePrograms((Integer) program.get("activePrograms"));
            report.setBudgetUsed((Double) program.get("budgetUsed"));
        }

        // -------- SUBSIDY --------
        if (scope == ReportScope.SUBSIDY) {
            Map<String, Object> subsidy = subsidyClient.getSubsidySummary();
            report.setApplicationsReceived((Integer) subsidy.get("applicationsReceived"));
            report.setApprovedSubsidies((Integer) subsidy.get("approvedSubsidies"));
            report.setAmountDistributed((Double) subsidy.get("amountDistributed"));
        }

        // -------- TAX --------
        if (scope == ReportScope.TAX) {
            Map<String, Object> tax = taxClient.getTaxStatistics();
            report.setTotalTaxpayers((Integer) tax.get("totalTaxpayers"));
            report.setRevenueCollected((Double) tax.get("revenueCollected"));
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

        summary.put(ReportScope.PROGRAM,
                generateReport(ReportScope.PROGRAM));

        summary.put(ReportScope.SUBSIDY,
                generateReport(ReportScope.SUBSIDY));

        summary.put(ReportScope.TAX,
                generateReport(ReportScope.TAX));

        return summary;
    }
}