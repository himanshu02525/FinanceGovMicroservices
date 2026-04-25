package com.finance.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.finance.client.EntityFeignClient;
import com.finance.client.ProgramSubsidyFeignClient;
import com.finance.client.TaxFeignClient;
import com.finance.dto.ComplianceCreateRequest;
import com.finance.dto.ComplianceResponse;
import com.finance.dto.ComplianceUpdateRequest;
import com.finance.dto.FinancialProgramResponse;
import com.finance.dto.SubsidyResponse;
import com.finance.dto.TaxResponseDTO;
import com.finance.enums.ComplianceRecordResult;
import com.finance.enums.ComplianceRecordType;
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
	private static final String REF_NOT_FOUND_MESSAGE = "validation.reference.notfound";
	private static final String UPDATE_INVALID_MESSAGE = "record.update.invalid.message";
	private static final String UPDATE_STATUS_PENDING_INVALID = "record.status.pending.invalid";

	private static final String VALIDATION_REFERENCE_NOT_FOUND = "validation.reference.notfound";
	private static final String UNSUPPORTED_COMPLIANCE_TYPE = "Unsupported compliance type: ";
	private final ComplianceRecordRepository repository;
	private final ModelMapper modelMapper;
	private final MessageUtil messageUtil;
	private final ProgramSubsidyFeignClient programSubsidyFeignClient;
	private final TaxFeignClient taxFeignClient;
	private final EntityFeignClient entityFeignClient;

	/* ================= FEIGN RESPONSE VALIDATION ================= */
	private <T> T validateFeignResponse(ResponseEntity<T> response, String resourceName, long referenceId) {

		if (response == null || !response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {

			switch (resourceName) {

			case "TAX":
				throw new TaxRecordNotFoundException(
						messageUtil.getMessage(VALIDATION_REFERENCE_NOT_FOUND, resourceName, referenceId));

			case "PROGRAM":
				throw new ProgramNotFoundException(
						messageUtil.getMessage(VALIDATION_REFERENCE_NOT_FOUND, resourceName, referenceId));

			case "SUBSIDY":
				throw new SubsidyNotFoundException(
						messageUtil.getMessage(VALIDATION_REFERENCE_NOT_FOUND, resourceName, referenceId));

			default:
				throw new EntityNotFoundException(
						messageUtil.getMessage(REF_NOT_FOUND_MESSAGE, resourceName, referenceId));
			}
		}

		return response.getBody();
	}

	/* ================= FETCH EXTERNAL DETAILS ================= */
	private void fetchExternalDetails(ComplianceRecord complianceRecord, ComplianceResponse response) {

		Long refId = complianceRecord.getReferenceID();
		ComplianceRecordType type = complianceRecord.getType();

		switch (type) {

		case TAX -> {
			TaxResponseDTO tax = validateFeignResponse(taxFeignClient.getTaxById(refId), TAX, refId);
			response.setTaxResponseDTO(tax);
		}

		case SUBSIDY -> {
			SubsidyResponse subsidy = validateFeignResponse(programSubsidyFeignClient.getSubsidyById(refId), SUBSIDY,
					refId);
			response.setSubsidyResponse(subsidy);
		}

		case PROGRAM -> {
			FinancialProgramResponse program = validateFeignResponse(programSubsidyFeignClient.getProgramById(refId),
					PROGRAM, refId);
			response.setFinancialProgramResponse(program);
		}

		default -> throw new IllegalArgumentException(UNSUPPORTED_COMPLIANCE_TYPE + type);
		}
	}

	/* ================= VALIDATE REFERENCE BEFORE CREATE ================= */
	private void validateReference(ComplianceRecordType type, long referenceId) {

		switch (type) {

		case TAX -> validateFeignResponse(taxFeignClient.getTaxById(referenceId), TAX, referenceId);

		case SUBSIDY ->
			validateFeignResponse(programSubsidyFeignClient.getSubsidyById(referenceId), SUBSIDY, referenceId);

		case PROGRAM ->
			validateFeignResponse(programSubsidyFeignClient.getProgramById(referenceId), PROGRAM, referenceId);

		default -> throw new IllegalArgumentException(UNSUPPORTED_COMPLIANCE_TYPE + type);
		}
	}

	/* ================= READ OPERATIONS ================= */
	@Override
	public List<ComplianceResponse> findAll() {

		List<ComplianceResponse> responses = repository.findAll().stream().map(entity -> {
			ComplianceResponse response = modelMapper.map(entity, ComplianceResponse.class);
			fetchExternalDetails(entity, response);
			return response;
		}).toList();

		if (responses.isEmpty()) {
			throw new ComplianceNotFoundException("No compliance records available");
		}

		return responses;
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

	/*
	 * ================= CREATE =================
	 */
	@Override
	public ComplianceResponse create(ComplianceCreateRequest request) {

		if (Boolean.FALSE.equals(entityFeignClient.validateEntity(request.getEntityId()))) {
			log.warn("valid entity");
			throw new EntityNotFoundException(
					messageUtil.getMessage(NOT_FOUND_MESSAGE, "Entity", request.getEntityId()));
		}

		validateReference(request.getType(), request.getReferenceId());

		ComplianceRecord saved = repository.save(modelMapper.map(request, ComplianceRecord.class));

		return modelMapper.map(saved, ComplianceResponse.class);
	}

	/*
	 * ================= UPDATE =================
	 */
	@Override
	public ComplianceResponse update(long complianceId, ComplianceUpdateRequest body) {

		ComplianceRecord record = repository.findById(complianceId).orElseThrow(() -> new ComplianceNotFoundException(
				messageUtil.getMessage(NOT_FOUND_MESSAGE, COMPLIANCE, complianceId)));

		if (record.getResult() == ComplianceRecordResult.PASS || record.getResult() == ComplianceRecordResult.FAIL) {

			throw new ComplianceStatusConflictException(messageUtil.getMessage(UPDATE_INVALID_MESSAGE, COMPLIANCE,
					record.getResult().name(), record.getComplianceId()));
		}

		if (body.getResult() == ComplianceRecordResult.PENDING) {

			throw new AuditStatusConflictException(messageUtil.getMessage(UPDATE_STATUS_PENDING_INVALID, COMPLIANCE,
					ComplianceRecordResult.PENDING.name(), record.getComplianceId()));
		}

		if (body.getResult() == ComplianceRecordResult.FAIL || body.getResult() == ComplianceRecordResult.PASS) {
			record.setClosedAt(LocalDateTime.now());
		}

		record.setNotes(body.getNotes());
		record.setResult(body.getResult());

		return modelMapper.map(repository.save(record), ComplianceResponse.class);
	}

	/*
	 * ================= DELETE =================
	 */
	@Override
	public String delete(long complianceId) {

		if (!repository.existsById(complianceId)) {
			throw new ComplianceNotFoundException(messageUtil.getMessage(NOT_FOUND_MESSAGE, COMPLIANCE, complianceId));
		}

		repository.deleteById(complianceId);

		return messageUtil.getMessage("delete.message", COMPLIANCE, complianceId);
	}

	/*
	 * ================= FIND BY ENTITY =================
	 */
	@Override
	public List<ComplianceResponse> findByEntityId(long entityId) {

		List<ComplianceRecord> records = repository.findByEntityId(entityId);

		if (records.isEmpty()) {
			throw new ComplianceNotFoundException(messageUtil.getMessage(NOT_FOUND_MESSAGE, COMPLIANCE, entityId));
		}

		return records.stream().map(r -> modelMapper.map(r, ComplianceResponse.class)).toList();
	}

	/*
	 * ================= SUMMARY =================
	 */
	@Override
	public Map<String, Integer> getSummary() {

		Map<String, Integer> summary = new LinkedHashMap<>();
		int total = 0;

		for (ComplianceRecordResult status : ComplianceRecordResult.values()) {

			int count = repository.countByResult(status);
			summary.put(status.name(), count);
			total += count;
		}

		summary.put("All", total);
		return summary;
	}
}