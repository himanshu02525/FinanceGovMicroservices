package com.finance.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.finance.client.fallback.ProgramSubsidyServiceClient;
import com.finance.client.fallback.TaxServiceClient;
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
import com.finance.exceptions.ProgramNotFoundException;
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

	/* ================= FETCH EXTERNAL DETAILS ================= */
	private void fetchExternalDetails(ComplianceRecord complianceRecord, ComplianceResponse response) {

		if (complianceRecord == null || response == null) {
			return;
		}
		fetchExternalDetails(complianceRecord.getType(), complianceRecord.getReferenceID(), response);
	}

	private void fetchExternalDetails(ComplianceRecordType type, Long refId, ComplianceResponse response) {

		if (type == null || refId == null || response == null) {
			return;
		}

		switch (type) {

		case TAX -> {
			try {
				ResponseEntity<TaxResponseDTO> entity = taxServiceClient.getTaxById(refId);

				response.setTaxResponseDTO(entity.getBody());

			} catch (TaxRecordNotFoundException | ServiceUnavailableException ex) {
			}
		}

		case SUBSIDY -> {
			try {
				ResponseEntity<SubsidyResponse> entity = programSubsidyFeignClient.getSubsidyById(refId);

				response.setSubsidyResponse(entity.getBody());

			} catch (SubsidyNotFoundException | ServiceUnavailableException ex) {
			}
		}

		case PROGRAM -> {
			try {
				ResponseEntity<FinancialProgramResponse> entity = programSubsidyFeignClient.getProgramById(refId);

				response.setFinancialProgramResponse(entity.getBody());

			} catch (ProgramNotFoundException | ServiceUnavailableException ex) {
			}
		}

		default -> throw new IllegalArgumentException("Unsupported ComplianceRecordType: " + type);
		}
	}

	/* ================= READ ================= */
	@Override
	public List<ComplianceResponse> findAll() {

		List<ComplianceResponse> responses = repository.findAll().stream().map(entity -> {
			return modelMapper.map(entity, ComplianceResponse.class);
		}).toList();

		if (responses.isEmpty()) {
			throw new ComplianceNotFoundException(messageUtil.getMessage("error.no.records.present", COMPLIANCE));
		}

		return responses;
	}

	@Override
	public ComplianceResponse findById(long complianceId) {
		ComplianceRecord complianceRecord = repository.findById(complianceId)
				.orElseThrow(() -> new ComplianceNotFoundException(
						messageUtil.getMessage(NOT_FOUND_MESSAGE, COMPLIANCE, complianceId)));

		ComplianceResponse response = modelMapper.map(complianceRecord, ComplianceResponse.class);

//		ResponseEntity<CitizenBusinessResponseDTO> entityDetails = entityFeignClient
//				.validateEntity(complianceRecord.getEntityId());
		fetchExternalDetails(complianceRecord, response);

		return response;
	}

	/* ================= CREATE ================= */
	@Override
	public ComplianceResponse create(ComplianceCreateRequest request) {
		ComplianceRecord saved = repository.save(modelMapper.map(request, ComplianceRecord.class));
		return modelMapper.map(saved, ComplianceResponse.class);
	}

	/* ================= UPDATE ================= */
	@Override
	public ComplianceResponse update(long complianceId, ComplianceUpdateRequest body) {

		ComplianceRecord complianceRecord = repository.findById(complianceId)
				.orElseThrow(() -> new ComplianceNotFoundException(
						messageUtil.getMessage(NOT_FOUND_MESSAGE, COMPLIANCE, complianceId)));

		if (complianceRecord.getResult() == ComplianceRecordResult.PASS
				|| complianceRecord.getResult() == ComplianceRecordResult.FAIL) {

			throw new ComplianceStatusConflictException(messageUtil.getMessage("record.update.invalid.message",
					COMPLIANCE, complianceRecord.getResult(), complianceId));
		}

		if (body.getResult() == ComplianceRecordResult.PENDING) {
			throw new AuditStatusConflictException(
					messageUtil.getMessage("record.status.pending.invalid", COMPLIANCE, "PENDING", complianceId));
		}

		if (body.getResult() == ComplianceRecordResult.PASS || body.getResult() == ComplianceRecordResult.FAIL) {

			complianceRecord.setClosedAt(LocalDateTime.now());
		}

		complianceRecord.setNotes(body.getNotes());
		complianceRecord.setResult(body.getResult());

		boolean result = updateRelaventData(complianceRecord.getType(), complianceRecord.getReferenceID(),
				body.getResult());
		return modelMapper.map(repository.save(complianceRecord), ComplianceResponse.class);

	}

	public boolean updateRelaventData(ComplianceRecordType complianceRecordType, Long refId,
			ComplianceRecordResult complianceRecordResult) {

		if (complianceRecordType == null || refId == null) {
			return false;
		}

		switch (complianceRecordType) {

		case TAX -> {
			try {
				TaxUpdateDTO taxUpdateDTO = new TaxUpdateDTO();

				LocalDate today = LocalDate.now();
				LocalDate dueDate = LocalDate.of(today.getYear(), Month.MARCH, 30);

				if (today.isAfter(dueDate)) {
					taxUpdateDTO.setStatus(TaxStatus.OVERDUE);
				} else {
					switch (complianceRecordResult) {
					case PASS -> taxUpdateDTO.setStatus(TaxStatus.PAID);
					case FAIL -> taxUpdateDTO.setStatus(TaxStatus.PENDING);
					default -> taxUpdateDTO.setStatus(TaxStatus.VERIFIED);
					}
				}

				ResponseEntity<?> response = taxServiceClient.updateStatus(refId, taxUpdateDTO);

				return response != null && response.getStatusCode().is2xxSuccessful();

			} catch (TaxRecordNotFoundException | ServiceUnavailableException ex) {
				throw new ServiceUnavailableException("Service Unaviable");
			}
		}

		case SUBSIDY -> {
			try {
				programSubsidyFeignClient.getSubsidyById(refId);

				ResponseEntity<FinancialProgramResponse> response = programSubsidyFeignClient.updateStatus(refId);

				return response != null && response.getStatusCode().is2xxSuccessful();

			} catch (SubsidyNotFoundException | ServiceUnavailableException ex) {
				return false;
			}
		}

		default -> throw new IllegalArgumentException("Unsupported ComplianceRecordType: " + complianceRecordType);
		}
	}

	/* ================= DELETE ================= */
	@Override
	public String delete(long complianceId) {

		if (!repository.existsById(complianceId)) {
			throw new ComplianceNotFoundException(messageUtil.getMessage(NOT_FOUND_MESSAGE, COMPLIANCE, complianceId));
		}

		repository.deleteById(complianceId);
		return messageUtil.getMessage("delete.message", COMPLIANCE, complianceId);
	}

	/* ================= SUMMARY ================= */
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

	@Override
	public List<ComplianceResponse> findByEntityId(long entityId) {
		List<ComplianceRecord> complianceRecords = repository.findByEntityId(entityId);
		if (complianceRecords.isEmpty()) {
			throw new ComplianceNotFoundException(messageUtil.getMessage(NOT_FOUND_MESSAGE, COMPLIANCE, entityId));
		}
		return complianceRecords.stream()
				.map(complianceRecord -> modelMapper.map(complianceRecord, ComplianceResponse.class)).toList();

	}
}