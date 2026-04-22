package com.finance.service;

import java.time.Year;
import java.util.List;
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
import com.finance.dto.TaxStatsDTO;
import com.finance.dto.UserDto;
import com.finance.enums.ComplianceRecordType;
import com.finance.enums.NotificationCategory;
import com.finance.enums.TaxStatus;
import com.finance.exceptions.EntityNotFoundException;
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

	private final TaxRepository taxRepository; // Repository for tax database operations
	private final CitizenClient citizenClient; // Feign client to validate citizen existence
	private final UserFeignClient userFeignClient; // Feign client to fetch user contact details
	private final NotificationFeignClient notificationFeignClient; // Feign client to send alerts
	private final ComplianceFeignClient complianceFeignClient; // Feign client for compliance microservice

	@Override
	@Transactional
	public TaxResponseDTO createTaxRecord(TaxRequestDTO request) {
		Boolean exists; // Variable to store citizen validation result
		try {
			exists = citizenClient.validateCitizen(request.getEntityId()); // Call external service to check entity ID
		} catch (Exception e) {
			throw new EntityNotFoundException("Registration Service is unreachable."); // Handle connection failures
		}

		if (exists == null || !exists) {
			throw new EntityNotFoundException("Entity ID " + request.getEntityId() + " not found."); // Stop if entity
																										// is invalid
		}

		int currentYear = Year.now().getValue(); // Get current calendar year
		int requestYear = request.getYear(); // Extract year from user request

		if (requestYear != currentYear && requestYear != (currentYear - 1)) {
			throw new InvalidTaxYearException("Invalid Year: Use " + currentYear + " or " + (currentYear - 1)); // Enforce
																												// year
																												// rules
		}

		TaxRecord taxRecord = new TaxRecord(); // Initialize new tax record entity
		taxRecord.setEntityId(request.getEntityId()); // Map entity ID
		taxRecord.setYear(request.getYear()); // Map filing year
		taxRecord.setAmount(request.getAmount()); // Map tax amount
		taxRecord.setStatus(TaxStatus.PENDING); // Set default status as pending

		TaxRecord saved = taxRepository.save(taxRecord); // Persist the tax record in MySQL

		try {
			ComplianceCreateRequest compReq = new ComplianceCreateRequest(); // Prepare request for compliance service
			compReq.setEntityId(saved.getEntityId()); // Pass the citizen ID
			compReq.setReferenceId(saved.getTaxId()); // Link the tax ID as reference
			compReq.setType(ComplianceRecordType.TAX); // Specify tax-type audit
			compReq.setNotes("Tax record submitted successfully;Initial compliance has been generated."); // Set
																												// submission
																												// note
			complianceFeignClient.create(compReq); // Send call to compliance microservice
		} catch (Exception e) {
			log.error("Initial compliance trigger failed: {}", e.getMessage()); // Log error but keep record
		}

		return mapToResponseDTO(saved); // Return result DTO
	}

	

	@Override
	@Transactional
	public TaxResponseDTO verifyTaxRecordByTaxId(Long taxId, TaxStatus status) {
		TaxRecord record = taxRepository.findById(taxId)
				.orElseThrow(() -> new TaxRecordNotFoundException("Tax record not found.")); // Fetch record or fail

		record.setStatus(status); // Apply status (Paid/Overdue)
		TaxRecord saved = taxRepository.save(record); // Update database

		// Generate compliance record if status is PAID or OVERDUE
		if (status == TaxStatus.PAID || status == TaxStatus.OVERDUE) {
			try {
				ComplianceCreateRequest compReq = new ComplianceCreateRequest(); // Prepare audit record
				compReq.setEntityId(saved.getEntityId()); // Map entity ID
				compReq.setReferenceId(saved.getTaxId()); // Map tax ID reference
				compReq.setType(ComplianceRecordType.TAX); // Set type
				String note = (status == TaxStatus.PAID) ? "Tax Payment Successful, compliance generated"
						: "Tax Payment Overdue, compliance generated"; // Set note
				compReq.setNotes(note); // Set note
				complianceFeignClient.create(compReq); // Update compliance service
			} catch (Exception e) {
				log.error("Single compliance update failed: {}", e.getMessage()); // Log error
			}
		}

		if (status == TaxStatus.PAID || status == TaxStatus.OVERDUE) {
			try {
				UserDto user = userFeignClient.getUserById(saved.getEntityId()); // Fetch user by entity ID
				if (user != null && user.getEmail() != null) {
					String message = (status == TaxStatus.PAID) ? "Tax Payment Successful"
							: "Your tax payment is overdue, pay it"; // Set msg
					NotificationRequestDto note = NotificationRequestDto.builder().userId(user.getUserId())
							.entityId(saved.getEntityId()).category(NotificationCategory.TAX).message(message).build(); // Prepare
																														// alert
					notificationFeignClient.sendNotification(note, user.getEmail()); // Dispatch alert
				}
			} catch (Exception e) {
				log.error("Notification failed for Tax ID {}: {}", taxId, e.getMessage()); // Log alert failure
			}
		}

		return mapToResponseDTO(saved); // Return response
	}

	@Override
	public List<TaxResponseDTO> getAllTaxRecords() {
		return taxRepository.findAll().stream().map(this::mapToResponseDTO).collect(Collectors.toList()); // List all
	}

	@Override
	public TaxResponseDTO getTaxRecordByTaxId(Long taxId) {
		TaxRecord record = taxRepository.findById(taxId)
				.orElseThrow(() -> new TaxRecordNotFoundException("ID not found.")); // Get specific
		return mapToResponseDTO(record); // Return DTO
	}
	
	@Override
	public List<TaxResponseDTO> getAllTaxRecordsByEntityId(Long entityId) {
		// Fetch the list of records from the repository
		List<TaxRecord> records = taxRepository.findByEntityId(entityId);

		// Throw custom exception if no records exist for the given entityId
		if (records.isEmpty()) {
			throw new EntityNotFoundException("No tax records found for Entity ID: " + entityId);
		}

		// Map the entities to DTOs and return the list
		return records.stream()
				.map(this::mapToResponseDTO)
				.collect(Collectors.toList());
	}

	@Override
	public TaxStatsDTO getTaxStatistics() {
		return new TaxStatsDTO(taxRepository.countTotalTaxPayers(), taxRepository.calculateTotalRevenue()); // Aggregated
																											// data
	}

	private TaxResponseDTO mapToResponseDTO(TaxRecord record) {
		TaxResponseDTO dto = new TaxResponseDTO(); // Create DTO
		dto.setTaxId(record.getTaxId()); // Set PK
		dto.setEntityId(record.getEntityId()); // Set citizen ID
		dto.setYear(record.getYear()); // Set year
		dto.setAmount(record.getAmount()); // Set amount
		dto.setStatus(record.getStatus()); // Set status
		return dto; // Return mapped DTO
	}

}