package com.finance.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.finance.enums.DocType;
import com.finance.enums.VerificationStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "entity_document")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_id", nullable = false)
    @JsonIgnore
    private CitizenBusiness citizenBusiness;

    @Enumerated(EnumType.STRING)
    private DocType docType;

    private String fileURI;

    private String uploadedDate;

    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus;
}