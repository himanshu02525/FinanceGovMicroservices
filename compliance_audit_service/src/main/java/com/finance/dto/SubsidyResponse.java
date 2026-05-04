//package com.financegov.dto;
//
//import java.time.LocalDate;
//import lombok.*;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class SubsidyResponse {
//    private Long subsidyId;
//    private Long entityId;
//    private Double amount;
//    private LocalDate date;
//    private String status;
//    private Long programId;
//}

package com.finance.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubsidyResponse {
	private Long subsidyId; // Auto-generated ID
	private Long entityId; // Citizen/Business ID
	private Double amount; // Subsidy amount
	private LocalDate date; // Date of subsidy
	private String status;
	private Long programId; // Linked program ID
}
