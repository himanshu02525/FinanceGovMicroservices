package com.finance.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.finance.client.EntityFeignClient;
import com.finance.client.fallback.ProgramSubsidyServiceClient;
import com.finance.client.fallback.TaxServiceClient;
import com.finance.dto.CitizenBusinessResponseDTO;
import com.finance.dto.ComplianceCreateRequest;
import com.finance.dto.ComplianceResponse;
import com.finance.dto.ComplianceUpdateRequest;
import com.finance.dto.FinancialProgramResponse;
import com.finance.dto.SubsidyResponse;
import com.finance.dto.TaxResponseDTO;
import com.finance.dto.TaxUpdateDTO;
import com.finance.enums.ComplianceRecordResult;
import com.finance.enums.ComplianceRecordType;
import com.finance.enums.TaxStatus;
import com.finance.exceptions.AuditStatusConflictException;
import com.finance.exceptions.ComplianceNotFoundException;
import com.finance.exceptions.ComplianceStatusConflictException;
import com.finance.exceptions.ServiceUnavailableException;
import com.finance.exceptions.SubsidyNotFoundException;
import com.finance.exceptions.TaxRecordNotFoundException;
import com.finance.model.ComplianceRecord;
import com.finance.repository.ComplianceRecordRepository;
import com.finance.util.MessageUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ComplianceRecordServiceImpl implements ComplianceRecordService {

	private static final String COMPLIANCE = "Compliance";
	private static final String NOT_FOUND_MESSAGE = "not.found.message";

	private final ComplianceRecordRepository repository;
	private final ModelMapper modelMapper;
	private final MessageUtil messageUtil;
	private final ProgramSubsidyServiceClient programSubsidyFeignClient;
	private final TaxServiceClient taxServiceClient;
	private final EntityFeignClient entityFeignClient;

	/* ================= FETCH EXTERNAL DETAILS ================= */
	private void fetchExternalDetails(ComplianceResponse response) {
		Long lookupId = response.getReferenceId();
		ComplianceRecordType type = response.getType();

		if (type == null || lookupId == null) {
			log.warn(" Skipping external fetch: Type or Reference ID is missing for Response ID: {}",
					response.getComplianceId());
			return;
		}

		log.info(" Initiating external fetch for Type: {} and ID: {}", type, lookupId);

		try {
			switch (type) {
			case TAX -> {
				log.debug("[TaxClient] Requesting tax details for ID: {}", lookupId);
				TaxResponseDTO tax = taxServiceClient.getTaxById(lookupId).getBody();
				response.setTaxResponseDTO(tax);
				if (tax != null) {
					log.info("[TaxClient] Successfully retrieved tax data: {}", tax);
				}
			}
			case SUBSIDY -> {
				log.debug("[SubsidyClient] Requesting subsidy details for ID: {}", lookupId);
				SubsidyResponse subsidy = programSubsidyFeignClient.getSubsidyById(lookupId).getBody();
				response.setSubsidyResponse(subsidy);
				log.info("[SubsidyClient] Successfully retrieved subsidy data");
			}
			case PROGRAM -> {
				log.debug("[ProgramClient] Requesting program details for ID: {}", lookupId);
				FinancialProgramResponse program = programSubsidyFeignClient.getProgramById(lookupId).getBody();
				response.setFinancialProgramResponse(program);
				log.info("[ProgramClient] Successfully retrieved program data");
			}
			}
		} catch (Exception ex) {
			log.error(" CRITICAL ERROR: Failed to fetch external details for type {} and ID {}. Reason: {}", type,
					lookupId, ex.getMessage(), ex);
		}
	}

	/* ================= READ ================= */
	@Override
	public List<ComplianceResponse> findAll() {
		log.info(" Fetching all compliance records");
		List<ComplianceResponse> responses = repository.findAll().stream().map(entity -> {
			return modelMapper.map(entity, ComplianceResponse.class);
		}).toList();

		if (responses.isEmpty()) {
			log.warn(" No compliance records found in the system");
			throw new ComplianceNotFoundException(messageUtil.getMessage("error.no.records.present", COMPLIANCE));
		}

		return responses;
	}

	@Override
	public ComplianceResponse findById(long complianceId) {
		log.info(" Searching for compliance record with ID: {}", complianceId);
		ComplianceRecord complianceRecord = repository.findById(complianceId).orElseThrow(() -> {
			log.error(" Record not found for ID: {}", complianceId);
			return new ComplianceNotFoundException(messageUtil.getMessage(NOT_FOUND_MESSAGE, COMPLIANCE, complianceId));
		});

		ComplianceResponse response = modelMapper.map(complianceRecord, ComplianceResponse.class);

		log.info(" Found record. Fetching associated Citizen/Business details for Entity ID: {}",
				complianceRecord.getEntityId());
		ResponseEntity<CitizenBusinessResponseDTO> entityDetails = entityFeignClient
				.getCitizenById(complianceRecord.getEntityId());

		fetchExternalDetails(response);
		return response;
	}

	/* ================= CREATE ================= */
	@Override
	public ComplianceResponse create(ComplianceCreateRequest request) {
		log.info(" Creating new record for Entity ID: {} of Type: {}", request.getEntityId(), request.getType());
		ComplianceRecord saved = repository.save(modelMapper.map(request, ComplianceRecord.class));
		log.info(" Successfully saved new compliance record with Generated ID: {}", saved.getComplianceId());
		return modelMapper.map(saved, ComplianceResponse.class);
	}

	/* ================= UPDATE ================= */
	@Override
	public ComplianceResponse update(long complianceId, ComplianceUpdateRequest body) {
		log.info(" Update initiated for ID: {}. Target Result: {}", complianceId, body.getResult());

		ComplianceRecord complianceRecord = repository.findById(complianceId)
				.orElseThrow(() -> new ComplianceNotFoundException(
						messageUtil.getMessage(NOT_FOUND_MESSAGE, COMPLIANCE, complianceId)));

		if (complianceRecord.getResult() == ComplianceRecordResult.PASS
				|| complianceRecord.getResult() == ComplianceRecordResult.FAIL) {

			log.warn(" Update rejected: Record {} is already in terminal state {}", complianceId,
					complianceRecord.getResult());
			throw new ComplianceStatusConflictException(messageUtil.getMessage("record.update.invalid.message",
					COMPLIANCE, complianceRecord.getResult(), complianceId));
		}

		if (body.getResult() == ComplianceRecordResult.PENDING) {
			log.warn(" Update rejected: Cannot set status back to PENDING for ID: {}", complianceId);
			throw new AuditStatusConflictException(
					messageUtil.getMessage("record.status.pending.invalid", COMPLIANCE, "PENDING", complianceId));
		}

		if (body.getResult() == ComplianceRecordResult.PASS || body.getResult() == ComplianceRecordResult.FAIL) {
			complianceRecord.setClosedAt(LocalDateTime.now());
			log.info(" Record {} finalized. Closing at {}", complianceId, complianceRecord.getClosedAt());
		}

		complianceRecord.setNotes(body.getNotes());
		complianceRecord.setResult(body.getResult());

		log.info(" Triggering external data sync for RefID: {}", complianceRecord.getReferenceID());
		updateRelaventData(complianceRecord.getType(), complianceRecord.getReferenceID(), body.getResult());

		ComplianceRecord updated = repository.save(complianceRecord);
		log.info(" Compliance record {} successfully updated in database", complianceId);
		return modelMapper.map(updated, ComplianceResponse.class);
	}

	public void updateRelaventData(ComplianceRecordType complianceRecordType, Long refId,
			ComplianceRecordResult complianceRecordResult) {

		if (complianceRecordType == null || refId == null) {
			log.warn("[ExternalSync] Sync skipped: Type or RefId is null");
			return;
		}

		log.info("[ExternalSync] Syncing {} record (RefID: {}) with result {}", complianceRecordType, refId,
				complianceRecordResult);

		switch (complianceRecordType) {

		case TAX -> {
			try {
				ResponseEntity<TaxResponseDTO> taxRecord = taxServiceClient.getTaxById(refId);

				if (taxRecord == null || taxRecord.getBody() == null) {
					log.error("[TaxSync] Failed: No Tax record found for RefID: {}", refId);
					return;
				}

				TaxUpdateDTO taxUpdateDTO = new TaxUpdateDTO();

				TaxStatus status = TaxStatus.VERIFIED_INITIAL;

				if (complianceRecordResult == ComplianceRecordResult.PASS) {
					status = TaxStatus.VERIFIED_FINAL;
				} else if (complianceRecordResult == ComplianceRecordResult.FAIL) {
					status = TaxStatus.REJECTED;
				}
				taxUpdateDTO.setStatus(status);
				log.debug("[TaxSync] Sending status {} to Tax Service for RefID: {}", status, refId);
				taxServiceClient.verifySingleTax(refId, taxUpdateDTO);
				log.info("[TaxSync] Successfully updated Tax record status");

			} catch (TaxRecordNotFoundException | ServiceUnavailableException ex) {
				log.error("[TaxSync] External service error during sync: {}", ex.getMessage());
			}
		}
		case SUBSIDY -> {
			try {

				programSubsidyFeignClient.getSubsidyById(refId);
				programSubsidyFeignClient.updateStatus(refId);
				log.info("[SubsidySync] Successfully updated Subsidy status for RefID: {}", refId);
			} catch (SubsidyNotFoundException | ServiceUnavailableException ex) {
				log.error("[SubsidySync] External service error during sync: {}", ex.getMessage());
			}
		}

		default -> {
			log.error("[ExternalSync] Unsupported Record Type: {}", complianceRecordType);
			throw new IllegalArgumentException("Unsupported ComplianceRecordType: " + complianceRecordType);
		}
		}
	}

	/* ================= DELETE ================= */
	@Override
	public String delete(long complianceId) {
		log.info(" Deleting record ID: {}", complianceId);
		if (!repository.existsById(complianceId)) {
			log.error(" Delete failed: Record {} not found", complianceId);
			throw new ComplianceNotFoundException(messageUtil.getMessage(NOT_FOUND_MESSAGE, COMPLIANCE, complianceId));
		}

		repository.deleteById(complianceId);
		log.info(" Record {} deleted successfully", complianceId);
		return messageUtil.getMessage("delete.message", COMPLIANCE, complianceId);
	}

	/* ================= SUMMARY ================= */
	@Override
	public Map<String, Integer> getSummary() {
		log.info(" Generating compliance summary report");
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

	@Override
	public List<ComplianceResponse> findByEntityId(long entityId) {
		log.info(" Fetching records for Entity ID: {}", entityId);
		List<ComplianceRecord> complianceRecords = repository.findByEntityId(entityId);
		if (complianceRecords.isEmpty()) {
			log.warn(" No records found for Entity ID: {}", entityId);
			throw new ComplianceNotFoundException(messageUtil.getMessage(NOT_FOUND_MESSAGE, COMPLIANCE, entityId));
		}
		return complianceRecords.stream()
				.map(complianceRecord -> modelMapper.map(complianceRecord, ComplianceResponse.class)).toList();

	}
}