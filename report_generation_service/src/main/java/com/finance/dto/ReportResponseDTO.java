package com.finance.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.finance.enums.ReportScope;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportResponseDTO {

    private Long reportId;
    private ReportScope scope;
    private Map<String, Object> metrics;
    private LocalDateTime generatedDate;
}
