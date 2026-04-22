package com.finance.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finance.client.CitizenClient;
import com.finance.client.ComplianceFeignClient;
import com.finance.client.NotificationFeignClient;
import com.finance.client.UserFeignClient;
import com.finance.dto.ComplianceCreateRequest;
import com.finance.dto.NotificationRequestDto;
import com.finance.dto.SubsidyRequest;
import com.finance.dto.SubsidyResponse;
import com.finance.dto.UserDto;
import com.finance.enums.ComplianceRecordType;
import com.finance.enums.NotificationCategory;
import com.finance.enums.SubsidyStatus;
import com.finance.exceptions.EntityNotFoundException;
import com.finance.exceptions.NoSubsidiesFoundException;
import com.finance.exceptions.ProgramNotFoundException;
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
	public final NotificationFeignClient notificationFeignClient;
	public final UserFeignClient userFeignClient;
	private final ComplianceFeignClient complianceFeignClient;

	@Override
	@Transactional
	public SubsidyResponse saveSubsidy(SubsidyRequest request) {
	    // ✅ Validate citizen externally
	    Boolean isValid = citizenClient.validateCitizen(request.getEntityId());
	    if (!isValid) {
	        throw new IllegalStateException("Citizen entity is not valid.");
	    }

	    // ✅ Validate program internally
	    FinancialProgram program = programRepository.findById(request.getProgramId())
	            .orElseThrow(() -> new IllegalArgumentException("Program not found"));

	    if (request.getAmount() > program.getBudget()) {
	        throw new IllegalStateException("Requested amount exceeds program budget.");
	    }

	    Subsidy subsidy = new Subsidy();
	    subsidy.setEntityId(request.getEntityId());
	    subsidy.setAmount(request.getAmount());
	    subsidy.setDate(request.getDate() != null ? request.getDate() : LocalDate.now());
	    subsidy.setStatus(SubsidyStatus.valueOf(request.getStatus().toUpperCase()));
	    subsidy.setProgram(program);

	    Subsidy saved = subsidyRepository.save(subsidy);

	    // ✅ Fetch user details
	    UserDto user = userFeignClient.getUserById(request.getUserId());
	    String emailAddr = user.getEmail();
	    Long id = user.getUserId();

	    // ✅ Trigger notification
	    NotificationRequestDto notification = NotificationRequestDto.builder()
	            .userId(id)
	            .entityId(saved.getEntityId())
	            .category(NotificationCategory.SUBSIDY)
	            .message("Your subsidy has been granted.")
	            .build();

	    notificationFeignClient.sendNotification(notification, emailAddr);

	    // ✅ Create compliance record
	    ComplianceCreateRequest complianceRequest = new ComplianceCreateRequest();
	    complianceRequest.setEntityId(saved.getEntityId());
	    complianceRequest.setReferenceId(saved.getSubsidyId());
	    complianceRequest.setType(ComplianceRecordType.SUBSIDY);
	    complianceRequest.setNotes("Subsidy granted and corresponding compliance record logged successfully.");

	    complianceFeignClient.create(complianceRequest);

	   

	    return toResponse(saved);
	}


	@Override
	public List<SubsidyResponse> getAllSubsidies() {
	    List<Subsidy> subsidies = subsidyRepository.findAll();
	    if (subsidies.isEmpty()) {
	        throw new NoSubsidiesFoundException();
	    }
	    return subsidies.stream().map(this::toResponse).toList();
	}

	@Override
	public List<SubsidyResponse> getSubsidiesByProgram(Long programId) {
	    List<Subsidy> subsidies = subsidyRepository.findByProgramProgramId(programId);
	    if (subsidies.isEmpty()) {
	        throw new ProgramNotFoundException(programId);
	    }
	    return subsidies.stream().map(this::toResponse).toList();
	}

	@Override
	public List<SubsidyResponse> getSubsidiesByEntity(Long entityId) {
	    List<Subsidy> subsidies = subsidyRepository.findByEntityId(entityId);
	    if (subsidies.isEmpty()) {
	        throw new EntityNotFoundException(entityId);
	    }
	    return subsidies.stream().map(this::toResponse).toList();
	}

	@Override
	public SubsidyResponse getSubsidyById(Long subsidyId) {
	    Subsidy subsidy = subsidyRepository.findById(subsidyId)
	            .orElseThrow(() -> new SubsidyNotFoundException(subsidyId));
	    return toResponse(subsidy);
	}
	private SubsidyResponse toResponse(Subsidy subsidy) {
		return new SubsidyResponse(subsidy.getSubsidyId(), subsidy.getEntityId(), subsidy.getAmount(),
				subsidy.getDate(), subsidy.getStatus().name(), subsidy.getProgram().getProgramId());
	}



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


}
