package com.finance.model;
 
import jakarta.persistence.*;

import lombok.*;
 
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.finance.enums.DocType;

import com.finance.enums.VerificationStatus;
 
@Entity

@Data

@NoArgsConstructor

@AllArgsConstructor

public class EntityDocument {
 
    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long documentId;  
 
    @ManyToOne

    @JoinColumn(name = "entity_id")

    @JsonIgnore

    private CitizenBusiness citizenBusiness;
 
    @Enumerated(EnumType.STRING)

    private DocType docType;
 
    private String fileURI;
 
    private String uploadedDate;
 
    @Enumerated(EnumType.STRING)

    private VerificationStatus verificationStatus;

}
 