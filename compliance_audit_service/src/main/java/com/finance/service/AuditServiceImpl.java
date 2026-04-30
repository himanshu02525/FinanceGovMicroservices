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
		log.info("Fetching all audit records");

		List<AuditResponse> result = repository.findAll().stream()
				.map(auditRecord -> modelMapper.map(auditRecord, AuditResponse.class)).toList();

		if (result.isEmpty()) {
			throw new AuditRecordNotFoundException(messageUtil.getMessage("error.no.records.present", AUDIT));
		}
		log.info("Total audit records fetched: {}", result.size());
		return result;
	}

	@Override
	public Map<String, Integer> getSummary() {
		log.info("Generating audit summary by status");

		Map<String, Integer> summary = new LinkedHashMap<>();
		int allCount = 0;
		for (AuditStatus status : AuditStatus.values()) {
			int countByStatus = repository.countByStatus(status);
			allCount += countByStatus;
			summary.put(status.toString(), countByStatus);
		}
		summary.put("All", allCount);
		log.info("Audit summary generated successfully");
		return summary;
	}

	@Override
	public AuditResponse findById(long auditId) {
		log.info("Fetching audit record with ID: {}", auditId);

		Audit existingAudit = repository.findById(auditId).orElseThrow(() -> {
			log.warn("Audit record not found for ID: {}", auditId);
			return new AuditRecordNotFoundException(messageUtil.getMessage(NOT_FOUND_MESSAGE, AUDIT, auditId));
		});

		log.info("Audit record found for ID: {}", auditId);
		return modelMapper.map(existingAudit, AuditResponse.class);
	}

	@Override
	public List<AuditResponse> findByOfficerId(long officerId) {
		List<Audit> auditRecords = repository.findByOfficerId(officerId);
		if (auditRecords.isEmpty()) {
			throw new AuditRecordNotFoundException(
					messageUtil.getMessage("error.no.records.found", AUDIT, "Officer ID", officerId));
		}
		return auditRecords.stream().map(auditRecord -> modelMapper.map(auditRecord, AuditResponse.class)).toList();
	}

	@Override
	public AuditResponse create(AuditCreateRequest auditBody) {
		log.info("Creating new audit record");

		ResponseEntity<UserResponseDto> response = userFeignClient.getOfficerById(auditBody.getOfficerId());

		if (response == null || !response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
			throw new UserNotFoundException(
					messageUtil.getMessage(NOT_FOUND_MESSAGE, "Officer", auditBody.getOfficerId()));
		}

		if (!(RoleType.ROLE_GOVERNMENT_AUDITOR.toString().equals(response.getBody().getRole()))) {
			throw new UserNotFoundException(messageUtil.getMessage("error.unauthorized.auditor"));
		}

		Audit savedAudit = repository.save(modelMapper.map(auditBody, Audit.class));
		return modelMapper.map(savedAudit, AuditResponse.class);
	}

	@Override
	public AuditResponse update(long auditId, AuditUpdateRequest auditBody) {

		log.info("Updating audit record with ID: {}", auditId);

		Audit existingAudit = repository.findById(auditId).orElseThrow(
				() -> new AuditRecordNotFoundException(messageUtil.getMessage(NOT_FOUND_MESSAGE, AUDIT, auditId)));
		if (existingAudit.getStatus() == AuditStatus.COMPLETED) {
			throw new AuditStatusConflictException(messageUtil.getMessage("record.update.invalid.message", AUDIT,
					AuditStatus.COMPLETED.toString(), auditId));
		}

		if (auditBody.getStatus() == AuditStatus.PENDING) {
			throw new AuditStatusConflictException(messageUtil.getMessage("record.status.pending.invalid", AUDIT,
					AuditStatus.PENDING.toString(), auditId));
		}
		existingAudit.setFindings(auditBody.getFindings());
		existingAudit.setStatus(auditBody.getStatus());

		if (auditBody.getStatus() == AuditStatus.COMPLETED) {
			existingAudit.setClosedAt(LocalDateTime.now());
		}

		Audit updated = repository.save(existingAudit);

		log.info("Audit record updated successfully for ID: {}", auditId);

		return modelMapper.map(updated, AuditResponse.class);
	}

	@Override
	public String delete(long auditId) {
		log.info("Attempting to delete audit record with ID: {}", auditId);

		findById(auditId);

		repository.deleteById(auditId);

		log.info("Audit record deleted successfully with ID: {}", auditId);
		return messageUtil.getMessage("delete.message", AUDIT, auditId);

	}

}