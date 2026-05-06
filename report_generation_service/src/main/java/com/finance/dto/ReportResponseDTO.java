package com.finance.dto;

import java.time.LocalDateTime;
import java.util.Map;

//github.com/himanshu02525/FinanceGovMicroservices.git
import com.finance.enums.ReportScope;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class ReportResponseDTO {

    private Long reportId;
    private ReportScope scope;
    private Map<String, Object> metrics;
    private LocalDateTime generatedDate;
}
