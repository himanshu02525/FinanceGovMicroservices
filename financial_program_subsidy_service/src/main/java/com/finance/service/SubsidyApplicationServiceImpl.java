package com.finance.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finance.client.CitizenClient;
import com.finance.client.NotificationFeignClient;
import com.finance.client.UserFeignClient;
import com.finance.dto.NotificationRequestDto;
import com.finance.dto.SubsidyApplicationRequest;
import com.finance.dto.SubsidyApplicationResponse;
import com.finance.dto.UserDto;
import com.finance.enums.ApplicationStatus;
import com.finance.enums.NotificationCategory;
import com.finance.enums.ProgramStatus;
import com.finance.exceptions.ApplicationNotFoundException;
import com.finance.model.FinancialProgram;
import com.finance.model.SubsidyApplication;
import com.finance.repository.FinancialProgramRepository;
import com.finance.repository.SubsidyApplicationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubsidyApplicationServiceImpl implements SubsidyApplicationService {

    private final FinancialProgramRepository programRepository;
    private final SubsidyApplicationRepository applicationRepository;
    private final CitizenClient citizenClient;
    public final NotificationFeignClient notificationFeignClient;
    public final UserFeignClient userFeignClient; 
    
  
    @Override
    @Transactional
    public SubsidyApplicationResponse saveApplication(SubsidyApplicationRequest request) {
        // Validate citizen externally
        Boolean isValid = citizenClient.validateCitizen(request.getEntityId());
        if (!isValid) {
            throw new IllegalStateException("Citizen entity is not valid.");
        }

        FinancialProgram program = programRepository.findById(request.getProgramId())
                .orElseThrow(() -> new IllegalArgumentException("Program not found"));

        if (program.getStatus() == ProgramStatus.CLOSED) {
            throw new IllegalStateException("Applications cannot be submitted. Program is CLOSED.");
        }

        SubsidyApplication app = new SubsidyApplication();
        app.setSubmittedDate(request.getSubmittedDate());
        app.setStatus(ApplicationStatus.PENDING);
        app.setProgram(program);
        app.setEntityId(request.getEntityId());

     // After saving the application
        SubsidyApplication saved = applicationRepository.save(app);

        // ✅ Fetch user details
        UserDto user = userFeignClient.getUserById(request.getUserId());
        String email = user.getEmail();
        Long id = user.getUserId();

        // ✅ Trigger notification
        NotificationRequestDto notification = NotificationRequestDto.builder()
                .userId(id)
                .entityId(saved.getEntityId())
                .category(NotificationCategory.SUBSIDY)
                .message("Your subsidy application has been submitted successfully.")
                .build();

        notificationFeignClient.sendNotification(notification, email);

        return toResponse(saved);

    }

    @Override
    public SubsidyApplicationResponse approveApplication(Long applicationId) {
        SubsidyApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException(applicationId));

        if (app.getStatus() == ApplicationStatus.REJECTED) {
            throw new IllegalStateException("Rejected applications cannot be approved.");
        }

        app.setStatus(ApplicationStatus.APPROVED);
        SubsidyApplication updated = applicationRepository.save(app);

        
        return toResponse(updated);
    }

    @Override
    public SubsidyApplicationResponse rejectApplication(Long applicationId) {
        SubsidyApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException(applicationId));

        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new IllegalStateException("Only pending applications can be rejected.");
        }

        app.setStatus(ApplicationStatus.REJECTED);
        SubsidyApplication updated = applicationRepository.save(app);

       
        return toResponse(updated);
    }

    @Override
    public List<SubsidyApplicationResponse> getApplicationsByEntity(Long entityId) {
        List<SubsidyApplication> applications = applicationRepository.findByEntityId(entityId);
        if (applications.isEmpty()) {
            throw new ApplicationNotFoundException(entityId);
        }
        return applications.stream()
                .map(this::toResponse)
                .toList();
    } 

    private SubsidyApplicationResponse toResponse(SubsidyApplication app) {
        return new SubsidyApplicationResponse(
                app.getApplicationId(),
                app.getEntityId(),
                app.getSubmittedDate(),
                app.getProgram().getProgramId(),
                app.getStatus()
        );
    }

    public long getApplicationsReceived(Long programId) {
        return applicationRepository.countByProgramProgramId(programId);
    }
}
