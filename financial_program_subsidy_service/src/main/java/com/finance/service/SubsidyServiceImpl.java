package com.finance.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finance.client.CitizenClient;
import com.finance.client.NotificationFeignClient;
import com.finance.dto.NotificationRequestDto;
import com.finance.dto.SubsidyRequest;
import com.finance.dto.SubsidyResponse;
import com.finance.enums.NotificationCategory;
import com.finance.enums.SubsidyStatus;
import com.finance.exceptions.SubsidyNotFoundException;
import com.finance.model.FinancialProgram;
import com.finance.model.Subsidy;
import com.finance.repository.FinancialProgramRepository;
import com.finance.repository.SubsidyApplicationRepository;
import com.finance.repository.SubsidyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class SubsidyServiceImpl implements SubsidyService {

    private final SubsidyRepository subsidyRepository;
    private final SubsidyApplicationRepository applicationRepository;
    private final FinancialProgramRepository programRepository;
    private final CitizenClient citizenClient;
    private final NotificationFeignClient notificationFeignClient;

    @Override
    @Transactional
    public SubsidyResponse saveSubsidy(SubsidyRequest request, Long userId, String email) {
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

        // ✅ Trigger notification: Subsidy granted (no amount shown)
        NotificationRequestDto notification = NotificationRequestDto.builder()
                .userId(userId)
                .entityId(saved.getEntityId())
                .message("Your subsidy has been granted.")
                .category(NotificationCategory.SUBSIDY)
                .build();

        notificationFeignClient.sendNotification(notification, email);

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

    
//    @Override
//    public BigDecimal getApprovedAmountByProgram(Long programId) {
//        return subsidyRepository.sumApprovedAmountByProgramId(programId);
//    }
    
    public long getApprovedSubsidies(Long programId) {
        return subsidyRepository.countByProgramProgramIdAndStatus(programId, SubsidyStatus.GRANTED);
    }
    
    @Override
    public Map<String, Object> getSubsidySummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("applicationsReceived", applicationRepository.count());
        summary.put("approvedSubsidies", subsidyRepository.countByStatus(SubsidyStatus.GRANTED));
        summary.put("amountDistributed", subsidyRepository.sumApprovedAmountAcrossAllPrograms());
        return summary;
    }
    
    
    

//    @Override
//    public SubsidyResponse approveSubsidy(Long subsidyId, Long userId, String email) {
//        Subsidy subsidy = subsidyRepository.findById(subsidyId)
//                .orElseThrow(() -> new IllegalArgumentException("Subsidy not found"));
//
//        subsidy.setStatus(SubsidyStatus.GRANTED);
//        subsidyRepository.save(subsidy);
//
//        // ✅ Trigger notification
//        NotificationRequestDto notification = NotificationRequestDto.builder()
//                .userId(userId)
//                .entityId(subsidy.getEntityId())
//                .message("Your subsidy has been approved. Amount: " + subsidy.getAmount())
//                .category(NotificationCategory.SUBSIDY)
//                .build();
//
//        notificationFeignClient.sendNotification(notification, email);
//
//        return toResponse(subsidy);
//    }
}


	




