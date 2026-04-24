//package com.financegov.dto;
//
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import lombok.AllArgsConstructor;
//
//@Data                     // Generates getters, setters, toString, equals, hashCode
//@NoArgsConstructor        // Generates a no-args constructor
//@AllArgsConstructor       // Generates an all-args constructor
//public class FinancialProgramResponse {
//
//    private Long programId;
//    private String title;
//    private String description;
//    private Double budget;
//    private String status;
//}


package com.finance.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data                     // Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor        // Generates a no-args constructor
@AllArgsConstructor       // Generates an all-args constructor
public class FinancialProgramResponse {

    private Long programId;
    private String title;
    private String description;
    private Double budget;
    private String status;
}

