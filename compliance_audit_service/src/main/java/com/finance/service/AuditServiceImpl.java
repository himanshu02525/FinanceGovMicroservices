package com.finance.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.finance.client.UserFeignClient;
import com.finance.dto.AuditCreateRequest;
import com.finance.dto.AuditResponse;
import com.finance.dto.AuditUpdateRequest;
import com.finance.dto.UserResponseDto;
import com.finance.enums.AuditStatus;
import com.finance.enums.RoleType;
import com.finance.exceptions.AuditRecordNotFoundException;
import com.finance.exceptions.AuditStatusConflictException;
import com.finance.exceptions.UserNotFoundException;
import com.finance.model.Audit;
import com.finance.repository.AuditRepository;
import com.finance.util.MessageUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

	private static final String AUDIT = "Audit";
	private static final String NOT_FOUND_MESSAGE = "not.found.message";

	private final AuditRepository repository;
	private final ModelMapper modelMapper;
	private final MessageUtil messageUtil;

	private final UserFeignClient userFeignClient;

	@Override
	public List<AuditResponse> findAll() {
		log.info("Fetching all audit records from database");

		List<AuditResponse> result = repository.findAll().stream()
				.map(auditRecord -> modelMapper.map(auditRecord, AuditResponse.class)).toList();

		if (result.isEmpty()) {
			log.warn("No audit records found in the repository");
			throw new AuditRecordNotFoundException(messageUtil.getMessage("error.no.records.present", AUDIT));
		}
		log.info("Successfully fetched {} audit records", result.size());
		return result;
	}

	@Override
	public Map<String, Integer> getSummary() {
		log.info("Generating audit status summary report");

		Map<String, Integer> summary = new LinkedHashMap<>();
		int allCount = 0;
		for (AuditStatus status : AuditStatus.values()) {
			int countByStatus = repository.countByStatus(status);
			allCount += countByStatus;
			summary.put(status.toString(), countByStatus);
			log.debug("Status: {}, Count: {}", status, countByStatus);
		}
		summary.put("All", allCount);
		log.info("Summary generation complete. Total records: {}", allCount);
		return summary;
	}

	@Override
	public AuditResponse findById(long auditId) {
		log.info("Searching for audit record ID: {}", auditId);

		Audit existingAudit = repository.findById(auditId).orElseThrow(() -> {
			log.error("Audit record NOT FOUND for ID: {}", auditId);
			return new AuditRecordNotFoundException(messageUtil.getMessage(NOT_FOUND_MESSAGE, AUDIT, auditId));
		});

		log.info("Successfully retrieved audit record for ID: {}", auditId);
		return modelMapper.map(existingAudit, AuditResponse.class);
	}

	@Override
	public List<AuditResponse> findByOfficerId(long officerId) {
		log.info("Fetching audit records assigned to Officer ID: {}", officerId);

		List<Audit> auditRecords = repository.findByOfficerId(officerId);
		if (auditRecords.isEmpty()) {
			log.warn("No audit records found for Officer ID: {}", officerId);
			throw new AuditRecordNotFoundException(
					messageUtil.getMessage("error.no.records.found", AUDIT, "Officer ID", officerId));
		}

		log.info("Found {} records for Officer ID: {}", auditRecords.size(), officerId);
		return auditRecords.stream().map(auditRecord -> modelMapper.map(auditRecord, AuditResponse.class)).toList();
	}

	@Override
	public AuditResponse create(AuditCreateRequest auditBody) {
		log.info("Creating new audit by Officer ID: {}", auditBody.getOfficerId(), auditBody.getOfficerId());

		log.debug("[UserClient] Validating Officer ID: {} via User Service", auditBody.getOfficerId());
		ResponseEntity<UserResponseDto> response = userFeignClient.getOfficerById(auditBody.getOfficerId());

		if (response == null || !response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
			log.error("[UserClient] Validation Failed: Officer ID {} not found", auditBody.getOfficerId());
			throw new UserNotFoundException(
					messageUtil.getMessage(NOT_FOUND_MESSAGE, "Officer", auditBody.getOfficerId()));
		}

		if (!(RoleType.ROLE_GOVERNMENT_AUDITOR.toString().equals(response.getBody().getRole()))) {
			log.warn("Security Alert: User {} is not an authorized auditor", auditBody.getOfficerId());
			throw new UserNotFoundException(messageUtil.getMessage("error.unauthorized.auditor"));
		}

		Audit savedAudit = repository.save(modelMapper.map(auditBody, Audit.class));
		log.info("New audit created successfully with Generated ID: {}", savedAudit.getAuditId());
		return modelMapper.map(savedAudit, AuditResponse.class);
	}

	@Override
	public AuditResponse update(long auditId, AuditUpdateRequest auditBody) {
		log.info("Update request for Audit ID: {} -> New Status: {}", auditId, auditBody.getStatus());

		Audit existingAudit = repository.findById(auditId).orElseThrow(() -> {
			log.error("Update failed: Audit ID {} not found", auditId);
			return new AuditRecordNotFoundException(messageUtil.getMessage(NOT_FOUND_MESSAGE, AUDIT, auditId));
		});

		if (existingAudit.getStatus() == AuditStatus.COMPLETED) {
			log.warn("Update rejected: Audit ID {} is already COMPLETED", auditId);
			throw new AuditStatusConflictException(messageUtil.getMessage("record.update.invalid.message", AUDIT,
					AuditStatus.COMPLETED.toString(), auditId));
		}

		if (auditBody.getStatus() == AuditStatus.PENDING) {
			log.warn("Invalid status transition: Cannot revert Audit ID {} to PENDING", auditId);
			throw new AuditStatusConflictException(messageUtil.getMessage("record.status.pending.invalid", AUDIT,
					AuditStatus.PENDING.toString(), auditId));
		}

		existingAudit.setFindings(auditBody.getFindings());
		existingAudit.setStatus(auditBody.getStatus());

		if (auditBody.getStatus() == AuditStatus.COMPLETED) {
			existingAudit.setClosedAt(LocalDateTime.now());
			log.info("Closing audit record ID: {} at {}", auditId, existingAudit.getClosedAt());
		}

		Audit updated = repository.save(existingAudit);
		log.info("Audit record ID: {} updated successfully", auditId);

		return modelMapper.map(updated, AuditResponse.class);
	}

	@Override
	public String delete(long auditId) {
		log.info("Attempting to delete audit record ID: {}", auditId);

		// Reusing findById to ensure record exists and leverage its logging
		findById(auditId);

		repository.deleteById(auditId);

		log.info("Audit record ID: {} deleted successfully from database", auditId);
		return messageUtil.getMessage("delete.message", AUDIT, auditId);
	}
}