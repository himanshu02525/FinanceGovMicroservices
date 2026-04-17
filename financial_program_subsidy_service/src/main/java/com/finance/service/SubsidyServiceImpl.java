package com.finance.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finance.client.CitizenClient;
import com.finance.dto.SubsidyRequest;
import com.finance.dto.SubsidyResponse;
import com.finance.enums.SubsidyStatus;
import com.finance.exceptions.SubsidyNotFoundException;
import com.finance.model.FinancialProgram;
import com.finance.model.Subsidy;
import com.finance.repository.FinancialProgramRepository;
import com.finance.repository.SubsidyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubsidyServiceImpl implements SubsidyService {

    private final SubsidyRepository subsidyRepository;
    private final FinancialProgramRepository programRepository;
    private final CitizenClient citizenClient;

    @Override
    @Transactional
    public SubsidyResponse saveSubsidy(SubsidyRequest request) {
        // Validate citizen externally
        Boolean isValid = citizenClient.validateCitizen(request.getEntityId());
        if (!isValid) {
            throw new IllegalStateException("Citizen entity is not valid.");
        }

        // Validate program internally
        FinancialProgram program = programRepository.findById(request.getProgramId())
                .orElseThrow(() -> new IllegalArgumentException("Program not found"));

        if (request.getAmount() > program.getBudget()) {
            throw new IllegalStateException("Requested amount exceeds program budget.");
        }

        Subsidy subsidy = new Subsidy();
        subsidy.setEntityId(request.getEntityId()); // ✅ directly store entityId
        subsidy.setAmount(request.getAmount());
        subsidy.setDate(request.getDate() != null ? request.getDate() : LocalDate.now());
        subsidy.setStatus(SubsidyStatus.valueOf(request.getStatus().toUpperCase()));
        subsidy.setProgram(program);

        Subsidy saved = subsidyRepository.save(subsidy);
        return toResponse(saved);
    }


    @Override
    public List<SubsidyResponse> getAllSubsidies() {
        return subsidyRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public List<SubsidyResponse> getSubsidiesByProgram(Long programId) {
        return subsidyRepository.findByProgramProgramId(programId).stream().map(this::toResponse).toList();
    }

    @Override
    public List<SubsidyResponse> getSubsidiesByEntity(Long entityId) {
        return subsidyRepository.findByEntityId(entityId).stream().map(this::toResponse).toList();
    }

    @Override
    public SubsidyResponse getSubsidyById(Long subsidyId) {
        Subsidy subsidy = subsidyRepository.findById(subsidyId)
                .orElseThrow(() -> new SubsidyNotFoundException(subsidyId));
        return toResponse(subsidy);
    }

    private SubsidyResponse toResponse(Subsidy subsidy) {
        return new SubsidyResponse(
                subsidy.getSubsidyId(),
                subsidy.getEntityId(),
                subsidy.getAmount(),
                subsidy.getDate(),
                subsidy.getStatus().name(),
                subsidy.getProgram().getProgramId()
        );
    }
    
    @Override
    public BigDecimal getApprovedAmountByProgram(Long programId) {
        return subsidyRepository.sumApprovedAmountByProgramId(programId);
    }

}
