package com.finance.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finance.client.CitizenClient;
import com.finance.client.NotificationFeignClient;
import com.finance.client.UserFeignClient;
import com.finance.dto.DisclosureCreateRequestDTO;
import com.finance.dto.DisclosureResponseDTO;
import com.finance.dto.NotificationRequestDto;
import com.finance.dto.UserDto;
import com.finance.enums.DisclosureStatus;
import com.finance.enums.DisclosureType;
import com.finance.enums.NotificationCategory;
import com.finance.exceptions.EntityNotFoundException;
import com.finance.exceptions.ResourceNotFoundException;
import com.finance.model.Disclosure;
import com.finance.repository.DisclosureRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisclosureServiceImpl implements DisclosureService {

	private final DisclosureRepository disclosureRepository;
	private final CitizenClient citizenClient;
	private final NotificationFeignClient notificationFeignClient;
	private final UserFeignClient userFeignClient;

	@Override
	@Transactional
	public DisclosureResponseDTO processDisclosure(DisclosureCreateRequestDTO request) {
		log.info("Starting disclosure processing for Entity ID: {} with type: {}", request.getEntityId(),
				request.getType());

		boolean exists;
		try {
			exists = citizenClient.validateCitizen(request.getEntityId());
			log.debug("Citizen validation result for Entity ID {}: {}", request.getEntityId(), exists);
		} catch (Exception e) {
			log.error("Failed to connect to Citizen Service for Entity ID {}: {}", request.getEntityId(),
					e.getMessage());
			throw new EntityNotFoundException("Entity ID " + request.getEntityId() + " not found.");
		}

		if (!exists) {
			log.warn("Disclosure processing aborted: Entity ID {} does not exist.", request.getEntityId());
			throw new EntityNotFoundException("Entity ID " + request.getEntityId() + " not found.");
		}

		Disclosure disclosure = new Disclosure();
		disclosure.setEntityId(request.getEntityId());
		disclosure.setType(request.getType());
		disclosure.setStatus(DisclosureStatus.SUBMITTED);
		disclosure.setSubmissionDate(LocalDateTime.now());

		Disclosure saved = disclosureRepository.save(disclosure);
		log.info("Disclosure record saved with ID: {} and status: {}", saved.getDisclosureId(), saved.getStatus());

		try {
			log.debug("Fetching user details for notification for Entity ID: {}", saved.getEntityId());
			UserDto user = userFeignClient.getUserById(saved.getEntityId());

			if (user != null && user.getEmail() != null) {
				NotificationRequestDto notification = NotificationRequestDto.builder().userId(user.getUserId())
						.entityId(saved.getEntityId()).category(NotificationCategory.TAX)
						.message("Your disclosure has been submitted successfully.").build();

				notificationFeignClient.sendNotification(notification, user.getEmail());
				log.info("Submission notification sent to email: {}", user.getEmail());
			} else {
				log.warn("Notification skipped: User or email not found for Entity ID: {}", saved.getEntityId());
			}
		} catch (Exception e) {
			log.error("Notification failed for entity {}: {}", saved.getEntityId(), e.getMessage());
		}

		return mapToResponseDTO(saved);
	}

	@Override
	@Transactional
	public List<DisclosureResponseDTO> getAllDisclosuresByEntityId(Long entityId) {
		log.info("Fetching all disclosures for Entity ID: {}", entityId);

		List<Disclosure> list = disclosureRepository.findByEntityId(entityId);

		if (list.isEmpty()) {
			log.warn("No disclosure records found in database for Entity ID: {}", entityId);
			throw new EntityNotFoundException("No disclosure records found for Entity ID: " + entityId);
		}

		log.debug("Found {} disclosure records for Entity ID: {}", list.size(), entityId);
		return list.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public DisclosureResponseDTO validateSingleDisclosure(Long disclosureId, DisclosureStatus newStatus) {
		log.info("Attempting to update Disclosure ID: {} to status: {}", disclosureId, newStatus);

		Disclosure disclosure = disclosureRepository.findById(disclosureId).orElseThrow(() -> {
			log.error("Validation failed: Disclosure ID {} not found", disclosureId);
			return new ResourceNotFoundException("Disclosure record not found.");
		});

		disclosure.setStatus(newStatus);
		Disclosure saved = disclosureRepository.save(disclosure);
		log.info("Disclosure ID: {} updated successfully to {}", disclosureId, newStatus);

		try {
			log.debug("Fetching user details for status update notification for Entity ID: {}", saved.getEntityId());
			UserDto user = userFeignClient.getUserById(saved.getEntityId());

			if (user != null && user.getEmail() != null) {
				String msg = (newStatus == DisclosureStatus.VALIDATED)
						? "Disclosure Validated: Your record has been approved."
						: "Disclosure Rejected: Please check the details and submit again.";

				NotificationRequestDto notification = NotificationRequestDto.builder().userId(user.getUserId())
						.entityId(saved.getEntityId()).category(NotificationCategory.TAX).message(msg).build();

				notificationFeignClient.sendNotification(notification, user.getEmail());
				log.info("Status update notification sent to {} for Disclosure ID: {}", user.getEmail(), disclosureId);
			}
		} catch (Exception e) {
			log.error("Single notification failed for disclosure {}: {}", disclosureId, e.getMessage());
		}

		return mapToResponseDTO(saved);
	}

	@Override
	public List<DisclosureResponseDTO> getAllDisclosures() {
		log.debug("Fetching all disclosures across the system.");
		return disclosureRepository.findAll().stream().map(this::mapToResponseDTO).collect(Collectors.toList());
	}

	@Override
	public DisclosureResponseDTO getDisclosureByDisclosureId(Long id) {
		log.debug("Fetching details for Disclosure ID: {}", id);

		Disclosure d = disclosureRepository.findById(id).orElseThrow(() -> {
			log.warn("Disclosure ID {} not found in database.", id);
			return new ResourceNotFoundException("Disclosure " + id + " not found");
		});

		return mapToResponseDTO(d);
	}

	private long getDiscloureCountByStatus(DisclosureStatus status) {
		return disclosureRepository.countByStatus(status);
	}

	private long getDiscloureCountByType(DisclosureType type) {
		return disclosureRepository.countByType(type);
	}

	@Override
	public Map<String, Object> getSummary() {
		Map<String, Object> summary = new HashMap<>();

		// 1. Disclosure Status Metrics
		Map<String, Long> statusAnalytics = new HashMap<>();
		for (DisclosureStatus status : DisclosureStatus.values()) {
			// Formats: "SUBMITTED" -> "submittedDisclosures"
			String statusKey = status.name().toLowerCase() + "Disclosures";
			statusAnalytics.put(statusKey, getDiscloureCountByStatus(status));
		}

		// 2. Disclosure Type Metrics
		Map<String, Long> typeAnalytics = new HashMap<>();
		for (DisclosureType type : DisclosureType.values()) {
			// Formats: "INCOME" -> "incomeDisclosures"
			String typeKey = type.name().toLowerCase() + "Disclosures";
			typeAnalytics.put(typeKey, getDiscloureCountByType(type));
		}

		// 3. Final mapping with professional, descriptive top-level keys
		summary.put("disclosuresByStatus", statusAnalytics);
		summary.put("disclosuresByType", typeAnalytics);

		return summary;
	}

	private DisclosureResponseDTO mapToResponseDTO(Disclosure d) {
		return DisclosureResponseDTO.builder().disclosureId(d.getDisclosureId()).entityId(d.getEntityId())
				.type(d.getType()).status(d.getStatus()).submissionDate(d.getSubmissionDate()).build();
	}

}