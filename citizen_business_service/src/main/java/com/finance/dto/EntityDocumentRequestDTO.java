package com.finance.dto;

import com.finance.enums.DocType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityDocumentRequestDTO {

    @NotNull(message = "Document type is required")
    private DocType docType;

    @NotNull(message = "File URI is required")
    @Pattern(
        regexp = ".*\\.pdf$",
        message = "Only PDF files are allowed"
    )
    private String fileURI;

    @NotNull(message = "Uploaded date is required")
    @Pattern(
        regexp = "^\\d{4}-\\d{2}-\\d{2}$",
        message = "Uploaded date must be in yyyy-MM-dd format"
    )
    private String uploadedDate;
}