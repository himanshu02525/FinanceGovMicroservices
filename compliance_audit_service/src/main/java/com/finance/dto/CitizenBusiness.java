package com.finance.dto;

import java.util.List;

import com.finance.enums.Status;
import com.finance.enums.Type;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitizenBusiness {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long entityId;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    private String name;

    @Enumerated(EnumType.STRING)
    private Type type;

    private String address;

    private String contactInfo;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(mappedBy = "citizenBusiness", cascade = CascadeType.ALL)
    private List<EntityDocument> documents;
}
