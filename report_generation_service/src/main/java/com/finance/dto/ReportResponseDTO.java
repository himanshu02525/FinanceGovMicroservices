package com.finance.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.finance.enums.ReportScope;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportResponseDTO {

    private Long reportId;
    private ReportScope scope;
    private Map<String, Object> metrics;
    private LocalDateTime generatedDate;
}
