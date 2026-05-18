package com.finance.dto;

import com.finance.enums.DocType;
import com.finance.enums.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityDocumentResponseDTO {

    private Long documentId;
    private Long entityId;
    private DocType docType;
    private String fileURI;
    private String uploadedDate;
    private VerificationStatus verificationStatus;
}