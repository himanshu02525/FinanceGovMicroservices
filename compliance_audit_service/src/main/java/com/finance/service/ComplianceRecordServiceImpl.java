package com.finance.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.finance.client.EntityFeignClient;
import com.finance.client.NotificationFeignClient;
import com.finance.client.ProgramSubsidyFeignClient;
import com.finance.client.TaxFeignClient;
import com.finance.dto.ComplianceCreateRequest;
import com.finance.dto.ComplianceResponse;
import com.finance.dto.ComplianceUpdateRequest;
import com.finance.dto.FinancialProgramResponse;
import com.finance.dto.NotificationRequestDto;
import com.finance.dto.SubsidyResponse;
import com.finance.dto.TaxResponseDTO;
import com.finance.enums.ComplianceRecordResult;
import com.finance.enums.ComplianceRecordType;
import com.finance.enums.NotificationCategory;
import com.finance.exceptions.AuditStatusConflictException;
import com.finance.exceptions.ComplianceNotFoundException;
import com.finance.exceptions.ComplianceStatusConflictException;
import com.finance.exceptions.EntityNotFoundException;
import com.finance.exceptions.ProgramNotFoundException;
import com.finance.exceptions.SubsidyNotFoundException;
import com.finance.exceptions.TaxRecordNotFoundException;
import com.finance.model.ComplianceRecord;
import com.finance.repository.ComplianceRecordRepository;
import com.finance.util.MessageUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ComplianceRecordServiceImpl implements ComplianceRecordService {

	private static final String COMPLIANCE = "Compliance";
	private static final String SUBSIDY = "Subsidy";
	private static final String PROGRAM = "Program";
	private static final String TAX = "Tax";

	private static final String NOT_FOUND_MESSAGE = "not.found.message";

	private final ComplianceRecordRepository repository;
	private final ModelMapper modelMapper;
	private final MessageUtil messageUtil;
	private final ProgramSubsidyFeignClient programSubsidyFeignClient;
	private final TaxFeignClient taxFeignClient;
	private final EntityFeignClient entityFeignClient;
	private final NotificationFeignClient notificationFeignClient;

	@Override
	public List<ComplianceResponse> findAll() {
		log.info("Fetching all compliance records");

		List<ComplianceResponse> result = repository.findAll().stream()
				.map(c -> modelMapper.map(c, ComplianceResponse.class)).toList();

		log.info("Total compliance records fetched: {}", result.size());
		return result;
	}

	private void fetchExternalDetails(ComplianceRecord complianceRecord, ComplianceResponse response) {
		Long refId = complianceRecord.getReferenceID();
		ComplianceRecordType type = complianceRecord.getType();

		switch (type) {
		case TAX -> {
			ResponseEntity<TaxResponseDTO> tax = taxFeignClient.getTaxById(refId);
			if (tax == null || !tax.hasBody()) {
				throw new TaxRecordNotFoundException(messageUtil.getMessage(NOT_FOUND_MESSAGE, TAX, refId));
			}
			response.setTaxResponseDTO(tax.getBody());
		}

		case SUBSIDY -> {
			ResponseEntity<SubsidyResponse> subsidy = programSubsidyFeignClient.getSubsidyById(refId);
			if (subsidy == null || !subsidy.hasBody()) {
				throw new SubsidyNotFoundException(messageUtil.getMessage(NOT_FOUND_MESSAGE, SUBSIDY, refId));
			}
			response.setSubsidyResponse(subsidy.getBody());
		}

		case PROGRAM -> {
			ResponseEntity<FinancialProgramResponse> program = programSubsidyFeignClient.getProgramById(refId);
			if (program == null || !program.hasBody()) {
				throw new ProgramNotFoundException(messageUtil.getMessage(NOT_FOUND_MESSAGE, PROGRAM, refId));
			}
			response.setFinancialProgramResponse(program.getBody());
		}

		default -> throw new IllegalArgumentException("Unsupported compliance type: " + type);
		}
	}

	@Override
	public ComplianceResponse findById(long complianceId) {

		ComplianceRecord complianceRecord = repository.findById(complianceId)
				.orElseThrow(() -> new ComplianceNotFoundException(
						messageUtil.getMessage(NOT_FOUND_MESSAGE, COMPLIANCE, complianceId)));

		ComplianceResponse response = modelMapper.map(complianceRecord, ComplianceResponse.class);

		fetchExternalDetails(complianceRecord, response);

		return response;
	}

	private void validateReference(ComplianceRecordType type, long referenceId) {
		log.debug("Validating {} reference for ID: {}", type, referenceId);

		switch (type) {
		case TAX -> {
			TaxResponseDTO tax = taxFeignClient.getTaxById(referenceId).getBody();
			if (tax == null) {
				throw new TaxRecordNotFoundException(messageUtil.getMessage(NOT_FOUND_MESSAGE, TAX, referenceId));
			}
		}
		case SUBSIDY -> {
			SubsidyResponse subsidy = programSubsidyFeignClient.getSubsidyById(referenceId).getBody();
			if (subsidy == null) {
				throw new SubsidyNotFoundException(messageUtil.getMessage(NOT_FOUND_MESSAGE, SUBSIDY, referenceId));
			}
		}
		case PROGRAM -> {
			FinancialProgramResponse program = programSubsidyFeignClient.getProgramById(referenceId).getBody();
			if (program == null) {
				throw new ProgramNotFoundException(messageUtil.getMessage(NOT_FOUND_MESSAGE, PROGRAM, referenceId));
			}
		}
		default -> throw new IllegalArgumentException("Unsupported compliance type: " + type);
		}
	}

	@Override
	public ComplianceResponse create(ComplianceCreateRequest request) {
		log.info("Creating compliance record");
		Boolean validateEntity = entityFeignClient.validateEntity(request.getEntityId());
		if (!validateEntity.equals(Boolean.TRUE)) {
			throw new EntityNotFoundException(
					messageUtil.getMessage(NOT_FOUND_MESSAGE, "Entity", request.getEntityId()));
		}
		validateReference(request.getType(), request.getReferenceId());
		ComplianceRecord saved = repository.save(modelMapper.map(request, ComplianceRecord.class));
		NotificationRequestDto notification = NotificationRequestDto.builder().userId(saved.getEntityId())
				.entityId(request.getEntityId()).category(NotificationCategory.COMPLIANCE)
				.message("New subsidy application submitted and pending approval.").build();

		notificationFeignClient.sendNotification(notification, "complianceOfficer@gmail.com");
		return modelMapper.map(saved, ComplianceResponse.class);
	}

	@Override
	public ComplianceResponse update(long complianceId, ComplianceUpdateRequest complianceBody) {
		log.info("Updating compliance record with ID: {}", complianceId);

		ComplianceRecord existingRecord = repository.findById(complianceId)
				.orElseThrow(() -> new ComplianceNotFoundException(
						messageUtil.getMessage(NOT_FOUND_MESSAGE, COMPLIANCE, complianceId)));

		if (existingRecord.getResult() == ComplianceRecordResult.PASS
				|| existingRecord.getResult() == ComplianceRecordResult.FAIL) {
			throw new ComplianceStatusConflictException(messageUtil.getMessage("record.update.invalid.message",
					COMPLIANCE, existingRecord.getResult().toString(), complianceId));
		}
		if (complianceBody.getResult() == ComplianceRecordResult.PENDING) {
			throw new AuditStatusConflictException(messageUtil.getMessage("record.status.pending.invalid", COMPLIANCE,
					ComplianceRecordResult.PENDING, complianceId));
		}

		if (complianceBody.getResult() == ComplianceRecordResult.PASS
				|| complianceBody.getResult() == ComplianceRecordResult.FAIL) {
			existingRecord.setClosedAt(LocalDateTime.now());
		}

		existingRecord.setNotes(complianceBody.getNotes());
		existingRecord.setResult(complianceBody.getResult());
		ComplianceRecord updated = repository.save(existingRecord);

		log.info("Compliance record updated successfully for ID: {}", complianceId);

		return modelMapper.map(updated, ComplianceResponse.class);
	}

	@Override
	public String delete(long complianceId) {
		log.info("Attempting to delete compliance record with ID: {}", complianceId);

		if (repository.findById(complianceId).isEmpty()) {
			log.warn("Delete failed — compliance ID {} not found", complianceId);
			throw new ComplianceNotFoundException(messageUtil.getMessage(NOT_FOUND_MESSAGE, COMPLIANCE, complianceId));
		}
		repository.deleteById(complianceId);
		log.info("Compliance record deleted successfully with ID: {}", complianceId);

		return messageUtil.getMessage("delete.message", COMPLIANCE, complianceId);
	}

	@Override
	public List<ComplianceResponse> findByEntityId(long entityId) {
		log.info("Fetching compliance records for Entity ID: {}", entityId);

		List<ComplianceRecord> complianceRecord = repository.findByEntityId(entityId);

		if (complianceRecord.isEmpty()) {
			throw new ComplianceNotFoundException("No compliance records found for Entity Id : " + entityId);
		}

		log.info("Total records found for Entity ID {}: {}", entityId, complianceRecord.size());

		return complianceRecord.stream().map(audit -> modelMapper.map(audit, ComplianceResponse.class)).toList();
	}

	@Override
	public Map<String, Integer> getSummary() {
		log.info("Generating compliance summary by result status");

		Map<String, Integer> summary = new LinkedHashMap<>();
		int allCount = 0;
		for (ComplianceRecordResult status : ComplianceRecordResult.values()) {
			int countByResult = repository.countByResult(status);
			allCount += countByResult;
			summary.put(status.toString(), countByResult);
			log.debug("Status: {}, Count: {}", status, countByResult);
		}
		summary.put("All", allCount);
		log.info("Compliance summary generated successfully");
		return summary;
	}

}