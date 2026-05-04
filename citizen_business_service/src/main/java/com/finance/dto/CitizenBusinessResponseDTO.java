package com.finance.dto;
import com.finance.enums.Status;
import com.finance.enums.Type;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitizenBusinessResponseDTO {
   private Long entityId;
   private String name;
   private Type type;
   private String address;
   private String contactInfo;
   private Status status;
}