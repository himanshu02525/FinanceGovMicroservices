package com.finance.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
		log.info("Attempting to create tax record for Entity ID: {} for year: {}", request.getEntityId(),
				request.getYear());

		// 1. Verify Entity Existence
		Boolean exists;
		try {
			exists = citizenClient.validateCitizen(request.getEntityId());
		} catch (Exception e) {
			log.error("Citizen validation failed. Registration Service is unreachable for Entity ID: {}",
					request.getEntityId());
			throw new EntityNotFoundException("Registration Service is unreachable.");
		}

		if (exists == null || !exists) {
			log.warn("Tax record creation failed: Entity ID {} not found in system.", request.getEntityId());
			throw new EntityNotFoundException("Entity ID " + request.getEntityId() + " not found.");
		}

		int currentYear = Year.now().getValue();
		int requestYear = request.getYear();

		if (requestYear != currentYear && requestYear != (currentYear - 1)) {
			log.warn("Invalid tax year attempt: {} for Entity ID: {}. Current year is {}", requestYear,
					request.getEntityId(), currentYear);
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
		log.info("Successfully created tax record. Assigned Tax ID: {} with status: {}", saved.getTaxId(),
				saved.getStatus());

		return mapToResponseDTO(saved);
	}

	@Override
	public List<TaxResponseDTO> getAllTaxRecordsByEntityId(Long entityId) {
		log.debug("Fetching all tax records for Entity ID: {}", entityId);
		List<TaxRecord> records = taxRepository.findByEntityId(entityId);

		if (records.isEmpty()) {
			log.warn("No tax records found in database for Entity ID: {}", entityId);
			throw new EntityNotFoundException("No tax records found for Entity ID: " + entityId);
		}

		return records.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public TaxResponseDTO verifyTaxRecordByTaxId(Long taxId, TaxUpdateDTO taxUpdateDTO) {
		log.info("Verification process started for Tax ID: {} to status: {}", taxId, taxUpdateDTO.getStatus());

		TaxRecord record = taxRepository.findById(taxId).orElseThrow(() -> {
			log.error("Verification failed: Tax record {} not found", taxId);
			return new TaxRecordNotFoundException("Tax record " + taxId + " not found");
		});

		TaxStatus currentStatus = record.getStatus();
		TaxStatus requestedStatus = taxUpdateDTO.getStatus();

		// ----- STATE TRANSITIONS -----
		if (currentStatus == TaxStatus.PENDING) {
			if (requestedStatus != TaxStatus.VERIFIED_INITIAL) {
				log.warn("Invalid transition attempt for Tax ID: {}. PENDING to {}", taxId, requestedStatus);
				throw new InvalidTaxStatusTransitionException("Invalid transition from PENDING to " + requestedStatus);
			}

			record.setStatus(TaxStatus.VERIFIED_INITIAL);
			log.debug("Transitioning Tax ID: {} to VERIFIED_INITIAL. Triggering compliance request.", taxId);

			// ✅ Generate Compliance Record on Initial Verification
			try {
				ComplianceCreateRequest complianceCreateRequest = new ComplianceCreateRequest();
				complianceCreateRequest.setEntityId(record.getEntityId());
				complianceCreateRequest.setReferenceId(record.getTaxId());
				complianceCreateRequest.setType(ComplianceRecordType.TAX);
				complianceCreateRequest.setNotes(
						"Tax record verified initially by the Financial Officer. Please confirm whether the citizen filed the tax on time.");
				complianceFeignClient.create(complianceCreateRequest);
				log.info("Compliance record created successfully for Tax ID: {}", taxId);
			} catch (Exception e) {
				log.error("Failed to create compliance record for Tax ID: {}. Error: {}", taxId, e.getMessage());
				// Note: depending on business logic, you might want to rethrow or continue
			}

		} else if (currentStatus == TaxStatus.VERIFIED_INITIAL) {
			if (requestedStatus == TaxStatus.VERIFIED_FINAL) {
				log.info("Tax ID: {} detected as PAID (On time)", taxId);
				record.setStatus(TaxStatus.VERIFIED_FINAL);

			} else if (requestedStatus == TaxStatus.REJECTED) {
				log.info("Tax ID: {} has been REJECTED by officer.", taxId);
				record.setStatus(TaxStatus.REJECTED);
			} else {
				log.warn("Invalid transition attempt for Tax ID: {}. VERIFIED_INITIAL to {}", taxId, requestedStatus);
				throw new InvalidTaxStatusTransitionException(
						"Invalid transition from VERIFIED_INITIAL to " + requestedStatus);
			}

		} else {
			log.warn("Block verification attempt: Tax ID {} is already in final state: {}", taxId, currentStatus);
			throw new InvalidTaxStatusTransitionException(
					"Tax record is already finalized with status: " + currentStatus);
		}

		TaxRecord saved = taxRepository.save(record);

		// ----- NOTIFICATIONS (PAID / OVERDUE ONLY) -----
		if (saved.getStatus() == TaxStatus.PAID || saved.getStatus() == TaxStatus.OVERDUE) {
			log.debug("Initiating notification sequence for Tax ID: {} with status: {}", taxId, saved.getStatus());
			try {
				UserDto user = userFeignClient.getUserById(saved.getEntityId());

				if (user != null && user.getEmail() != null) {
					String message = switch (saved.getStatus()) {
					case PENDING -> "Your tax submission has been received and is currently under review.";
					case PAID -> "Your tax payment has been successfully completed.";
					case OVERDUE ->
						"Your tax payment is overdue. Please complete the payment at the earliest to avoid penalties.";
					case REJECTED ->
						"Your tax submission has been reviewed and requires corrections. Please resubmit with the necessary updates.";
					case VERIFIED_INITIAL ->
						"Your tax details have passed the initial review and are progressing to the next stage.";
					case VERIFIED_FINAL -> "Your tax details have been fully verified and approved successfully.";
					default -> "Your tax status has been updated. Please check the portal for more details.";
					};

					NotificationRequestDto notification = NotificationRequestDto.builder().userId(user.getUserId())
							.entityId(saved.getEntityId()).category(NotificationCategory.TAX).message(message).build();

					notificationFeignClient.sendNotification(notification, user.getEmail());
					log.info("Notification sent successfully to {} for Tax ID: {}", user.getEmail(), taxId);
				} else {
					log.warn("Notification skipped for Tax ID: {}. User or Email not found for Entity ID: {}", taxId,
							saved.getEntityId());
				}

			} catch (Exception e) {
				log.error("Tax notification failed for Tax ID {}: {}", taxId, e.getMessage());
			}
		}

		return mapToResponseDTO(saved);
	}

	@Override
	public List<TaxResponseDTO> getAllTaxRecords() {
		log.debug("Fetching all tax records from repository");
		return taxRepository.findAll().stream().map(this::mapToResponseDTO).collect(Collectors.toList());
	}

	@Override
	public TaxResponseDTO getTaxRecordByTaxId(Long id) {
		log.debug("Fetching tax record details for Tax ID: {}", id);
		TaxRecord record = taxRepository.findById(id).orElseThrow(() -> {
			log.warn("Tax record ID {} not found.", id);
			return new TaxRecordNotFoundException("Tax record ID " + id + " not found.");
		});
		return mapToResponseDTO(record);
	}

	@Override

	public Map<String, Object> getTaxStatistics(Integer year) {
 
		log.info("Calculating tax statistics...");
 
		Map<String, Object> response = new HashMap<>();
 
		// ---- BASIC COUNTS ----

		int totalTaxpayers = (year == null) ? Optional.ofNullable(taxRepository.countTotalTaxPayers()).orElse(0)

				: Optional.ofNullable(taxRepository.countTotalTaxPayers(year)).orElse(0);
 
		long totalRecords = (year == null) ? taxRepository.count() : taxRepository.countByYear(year);
 
		long pendingCount = getCountByStatus(TaxStatus.PENDING, year);

		long paidCount = getCountByStatus(TaxStatus.PAID, year);

		long overdueCount = getCountByStatus(TaxStatus.OVERDUE, year);

		long rejectedCount = getCountByStatus(TaxStatus.REJECTED, year);
 
		double revenueCollected = (year == null)

				? Optional.ofNullable(taxRepository.calculateTotalRevenue()).orElse(0.0)

				: Optional.ofNullable(taxRepository.calculateTotalRevenue(year)).orElse(0.0);
 
		// ---- METRICS ----

		double complianceRate = totalRecords == 0 ? 0 : (paidCount * 100.0) / totalRecords;

		double pendingPercentage = totalRecords == 0 ? 0 : (pendingCount * 100.0) / totalRecords;

		double overduePercentage = totalRecords == 0 ? 0 : (overdueCount * 100.0) / totalRecords;
 
		// ---- REVENUE DETAILS ----

		double averageTax = totalRecords == 0 ? 0 : revenueCollected / totalRecords;
 
		Double highestTax = Optional

				.ofNullable(year == null ? taxRepository.findMaxTax() : taxRepository.findMaxTax(year)).orElse(0.0);
 
		Double lowestTax = Optional

				.ofNullable(year == null ? taxRepository.findMinTax() : taxRepository.findMinTax(year)).orElse(0.0);
 
		// ---- BUILD RESPONSE ----
 
		Map<String, Object> summary = new HashMap<>();

		summary.put("totalTaxpayers", totalTaxpayers);

		summary.put("totalRecords", totalRecords);

		summary.put("revenueCollected", revenueCollected);
 
		Map<String, Object> status = new HashMap<>();

		status.put("pending", pendingCount);

		status.put("paid", paidCount);

		status.put("overdue", overdueCount);

		status.put("rejected", rejectedCount);
 
		Map<String, Object> metrics = new HashMap<>();

		metrics.put("complianceRate", round(complianceRate));

		metrics.put("pendingPercentage", round(pendingPercentage));

		metrics.put("overduePercentage", round(overduePercentage));
 
		Map<String, Object> revenue = new HashMap<>();

		revenue.put("averageTax", round(averageTax));

		revenue.put("highestTax", highestTax);

		revenue.put("lowestTax", lowestTax);
 
		response.put("summary", summary);

		response.put("status", status);

		response.put("metrics", metrics);

		response.put("revenue", revenue);
 
		return response;

	}
 

	private long getCountByStatus(TaxStatus status, Integer year) {
		return (year == null) ? taxRepository.countByStatus(status) : taxRepository.countByStatusAndYear(status, year);
	}

	private double round(double value) {
		return Math.round(value * 10.0) / 10.0;
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

	@Override
    @Transactional
    public TaxResponseDTO payTaxRecordByTaxId(Long taxId) {
        log.info("Payment process started for Tax ID: {}", taxId);

        // 1. Retrieve the record from the database
        TaxRecord record = taxRepository.findById(taxId).orElseThrow(() -> {
            log.error("Payment failed: Tax record {} not found", taxId);
            return new TaxRecordNotFoundException("Tax record " + taxId + " not found");
        });

        // 2. Validate that the record is ready for payment (VERIFIED_FINAL)
        TaxStatus currentStatus = record.getStatus();
        if (currentStatus != TaxStatus.VERIFIED_FINAL) {
            log.warn("Invalid payment attempt for Tax ID: {} with status: {}", taxId, currentStatus);
            throw new InvalidTaxStatusTransitionException(
                    "Payment can only be confirmed for VERIFIED_FINAL records. Current status: " + currentStatus);
        }

        LocalDate paymentDate = LocalDate.now();
        LocalDate dueDate = LocalDate.of(record.getCreatedAt().getYear(), 3, 30);
        boolean isLate = paymentDate.isAfter(dueDate);
        BigDecimal penalty = BigDecimal.ZERO;

        // 3. Determine Status and Calculate Penalty if late
        if (isLate) {
            BigDecimal originalAmount = record.getAmount();
            // Calculate 2% penalty: (Amount * 2) / 100
            penalty = originalAmount.multiply(BigDecimal.valueOf(2))
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            
            // Update the amount in the database to include the penalty
            record.setAmount(originalAmount.add(penalty));
            record.setStatus(TaxStatus.OVERDUE_PAID); // Updated to your new Enum value
            log.info("Late payment for Tax ID: {}. 2% penalty (₹{}) added. New Status: OVERDUE_PAID", 
                    taxId, penalty);
        } else {
            record.setStatus(TaxStatus.PAID);
            log.info("On-time payment for Tax ID: {}. Status: PAID", taxId);
        }

        // 4. Save updated amount and status to the database
        TaxRecord saved = taxRepository.save(record);

        // 5. Send Notification with specific details
        try {
            UserDto user = userFeignClient.getUserById(saved.getEntityId());
            if (user != null && user.getEmail() != null) {
                String message;
                if (isLate) {
                    message = "Your tax payment was successful but marked as OVERDUE_PAID. A 2% late fee of ₹" 
                            + penalty + " was applied for paying after the March 30 deadline.";
                } else {
                    message = "Your tax payment has been successfully completed.";
                }

                NotificationRequestDto notification = NotificationRequestDto.builder()
                        .userId(user.getUserId())
                        .entityId(saved.getEntityId())
                        .category(NotificationCategory.TAX)
                        .message(message)
                        .build();

                notificationFeignClient.sendNotification(notification, user.getEmail());
                log.info("Payment notification sent to {} for Tax ID: {}", user.getEmail(), taxId);
            }
        } catch (Exception e) {
            log.error("Notification failed for Tax ID {}: {}", taxId, e.getMessage());
        }

        return mapToResponseDTO(saved);
    }
}