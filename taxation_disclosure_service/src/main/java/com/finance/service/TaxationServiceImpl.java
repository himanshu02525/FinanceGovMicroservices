package com.finance.service;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finance.client.CitizenClient;
import com.finance.client.ComplianceFeignClient;
import com.finance.client.NotificationFeignClient;
import com.finance.client.UserFeignClient;
import com.finance.dto.ComplianceCreateRequest;
import com.finance.dto.NotificationRequestDto;
import com.finance.dto.TaxRequestDTO;
import com.finance.dto.TaxResponseDTO;
import com.finance.dto.TaxUpdateDTO;
import com.finance.dto.UserDto;
import com.finance.enums.ComplianceRecordType;
import com.finance.enums.NotificationCategory;
import com.finance.enums.TaxStatus;
import com.finance.exceptions.EntityNotFoundException;
import com.finance.exceptions.InvalidTaxStatusTransitionException;
import com.finance.exceptions.InvalidTaxYearException;
import com.finance.exceptions.TaxRecordNotFoundException;
import com.finance.model.TaxRecord;
import com.finance.repository.TaxRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaxationServiceImpl implements TaxationService {

	private final TaxRepository taxRepository;
	private final CitizenClient citizenClient;
	private final UserFeignClient userFeignClient;
	private final NotificationFeignClient notificationFeignClient;
	private final ComplianceFeignClient complianceFeignClient;

	@Override
	@Transactional
	public TaxResponseDTO createTaxRecord(TaxRequestDTO request) {
		// 1. Verify Entity Existence
		Boolean exists;
		try {
			exists = citizenClient.validateCitizen(request.getEntityId());
		} catch (Exception e) {
			throw new EntityNotFoundException("Registration Service is unreachable.");
		}

		if (exists == null || !exists) {
			throw new EntityNotFoundException("Entity ID " + request.getEntityId() + " not found.");
		}

		int currentYear = Year.now().getValue();
		int requestYear = request.getYear();

		if (requestYear != currentYear && requestYear != (currentYear - 1)) {
			throw new InvalidTaxYearException(
					"Invalid Tax Year: " + requestYear + ". You can only file for the current year (" + currentYear
							+ ") or the previous year (" + (currentYear - 1) + ").");
		}

		TaxRecord taxRecord = new TaxRecord();
		taxRecord.setEntityId(request.getEntityId());
		taxRecord.setYear(request.getYear());
		taxRecord.setAmount(request.getAmount());
		taxRecord.setStatus(TaxStatus.PENDING);
		taxRecord.setCreatedAt(LocalDateTime.now());
		TaxRecord saved = taxRepository.save(taxRecord);
		return mapToResponseDTO(saved);
	}

	@Override
	public List<TaxResponseDTO> getAllTaxRecordsByEntityId(Long entityId) {
		List<TaxRecord> records = taxRepository.findByEntityId(entityId);

		if (records.isEmpty()) {
			throw new EntityNotFoundException("No tax records found for Entity ID: " + entityId);
		}

		return records.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public TaxResponseDTO verifyTaxRecordByTaxId(Long taxId, TaxUpdateDTO taxUpdateDTO) {

		TaxRecord record = taxRepository.findById(taxId)
				.orElseThrow(() -> new TaxRecordNotFoundException("Tax record " + taxId + " not found"));

		TaxStatus currentStatus = record.getStatus();
		TaxStatus requestedStatus = taxUpdateDTO.getStatus();

		// ---- VALID STATE TRANSITIONS ----
		if (currentStatus == TaxStatus.PENDING && requestedStatus == TaxStatus.VERIFIED
				|| requestedStatus == TaxStatus.REJECTED) {

			record.setStatus(taxUpdateDTO.getStatus());

			// ✅ Create Compliance Record on Verification
			ComplianceCreateRequest complianceCreateRequest = new ComplianceCreateRequest();
			complianceCreateRequest.setEntityId(record.getEntityId());
			complianceCreateRequest.setReferenceId(record.getTaxId());
			log.error(record.getTaxId() + "Here is id");
			complianceCreateRequest.setType(ComplianceRecordType.TAX);
			complianceCreateRequest.setNotes(
					"Tax record verified by the Financial Officer. Please confirm whether the citizen filed the tax on time.");
			complianceFeignClient.create(complianceCreateRequest);

		} else if (currentStatus == TaxStatus.VERIFIED
				&& (requestedStatus == TaxStatus.PAID || requestedStatus == TaxStatus.OVERDUE)) {

			record.setStatus(requestedStatus);

		} else {
			throw new InvalidTaxStatusTransitionException(
					"Invalid tax status transition from " + currentStatus + " to " + requestedStatus);
		}

		TaxRecord saved = taxRepository.save(record);

		// ---- NOTIFICATIONS (PAID / OVERDUE ONLY) ----
		if (saved.getStatus() == TaxStatus.PAID || saved.getStatus() == TaxStatus.OVERDUE) {
			try {
				UserDto user = userFeignClient.getUserById(saved.getEntityId());

				if (user != null && user.getEmail() != null) {

					String message = saved.getStatus() == TaxStatus.PAID ? "Tax payment successful."
							: "Your tax payment is overdue. Please pay immediately.";

					NotificationRequestDto notification = NotificationRequestDto.builder().userId(user.getUserId())
							.entityId(saved.getEntityId()).category(NotificationCategory.TAX).message(message).build();

					notificationFeignClient.sendNotification(notification, user.getEmail());
				}
			} catch (Exception e) {
				log.error("Tax notification failed for taxId {}: {}", taxId, e.getMessage());
			}
		}

		return mapToResponseDTO(saved);
	}

	@Override
	public List<TaxResponseDTO> getAllTaxRecords() {
		return taxRepository.findAll().stream().map(this::mapToResponseDTO).collect(Collectors.toList());
	}

	@Override
	public TaxResponseDTO getTaxRecordByTaxId(Long id) {
		TaxRecord record = taxRepository.findById(id)
				.orElseThrow(() -> new TaxRecordNotFoundException("Tax record ID " + id + " not found."));
		return mapToResponseDTO(record);
	}

	@Override
	public Map<String, Object> getTaxStatistics() {
		Map<String, Object> statistics = new HashMap<>();
		statistics.put("totalTaxpayers", taxRepository.countTotalTaxPayers());
		Double revenue = taxRepository.calculateTotalRevenue();
		statistics.put("revenueCollected", revenue != null ? revenue : 0.0);
		return statistics;
	}

	private TaxResponseDTO mapToResponseDTO(TaxRecord record) {
		TaxResponseDTO dto = new TaxResponseDTO();
		dto.setTaxId(record.getTaxId());
		dto.setEntityId(record.getEntityId());
		dto.setYear(record.getYear());
		dto.setAmount(record.getAmount());
		dto.setStatus(record.getStatus());
		dto.setCreatedAt(record.getCreatedAt());
		return dto;
	}
}