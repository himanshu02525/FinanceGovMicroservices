package com.finance.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.finance.enums.ReportScope;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(value = Include.NON_NULL)
public class ReportResponseDTO {

    private Long reportId;
    private ReportScope scope;
    private Map<String, Object> metrics;
    private LocalDateTime generatedDate;
}
