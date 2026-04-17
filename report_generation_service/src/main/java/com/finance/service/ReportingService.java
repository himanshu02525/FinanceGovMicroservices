package com.finance.service;

import java.util.List;
import java.util.Map;

import com.finance.enums.ReportScope;
import com.finance.model.Report;

public interface ReportingService {

    Report generateReport(ReportScope scope);

    List<Report> getReportsByScope(ReportScope scope);

    Report getReportById(Long id);

    Map<ReportScope, Report> getSummaryReports();
}