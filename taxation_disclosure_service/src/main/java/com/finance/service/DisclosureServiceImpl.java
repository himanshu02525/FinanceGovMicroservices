package com.finance.service;

import java.time.LocalDateTime;
import java.util.List;
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
		// 1. Verify Entity
		boolean exists;
		try {
			exists = citizenClient.validateCitizen(request.getEntityId());
		} catch (Exception e) {
			throw new EntityNotFoundException("External validation service unreachable.");
		}

		if (!exists) {
			throw new EntityNotFoundException("Entity ID " + request.getEntityId() + " not found.");
		}

		// 2. Save Disclosure
		Disclosure disclosure = new Disclosure();
		disclosure.setEntityId(request.getEntityId());
		disclosure.setType(request.getType());
		disclosure.setStatus(DisclosureStatus.SUBMITTED);
		disclosure.setSubmissionDate(LocalDateTime.now());

		Disclosure saved = disclosureRepository.save(disclosure);

		// 3. INLINE NOTIFICATION: Submission
		try {
			UserDto user = userFeignClient.getUserById(saved.getEntityId());
			if (user != null && user.getEmail() != null) {
				NotificationRequestDto notification = NotificationRequestDto.builder().userId(user.getUserId())
						.entityId(saved.getEntityId()).category(NotificationCategory.TAX)
						.message("Your disclosure has been submitted successfully.").build();
				notificationFeignClient.sendNotification(notification, user.getEmail());
			}
		} catch (Exception e) {
			log.error("Notification failed for entity {}: {}", saved.getEntityId(), e.getMessage());
		}

		return mapToResponseDTO(saved);
	}

	@Override
	@Transactional
	public List<DisclosureResponseDTO> getAllDisclosuresByEntityId(Long entityId) {
		// Fetch records from the database
		List<Disclosure> list = disclosureRepository.findByEntityId(entityId);

		// Check if the list is empty and throw the custom exception
		if (list.isEmpty()) {
			log.error("Fetch failed: No disclosure records found for Entity ID: {}", entityId);
			throw new EntityNotFoundException("No disclosure records found for Entity ID: " + entityId);
		}

		// Map and return the list of DTOs
		return list.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public DisclosureResponseDTO validateSingleDisclosure(Long disclosureId, DisclosureStatus newStatus) {
		Disclosure disclosure = disclosureRepository.findById(disclosureId)
				.orElseThrow(() -> new ResourceNotFoundException("Disclosure record not found."));

		disclosure.setStatus(newStatus);
		Disclosure saved = disclosureRepository.save(disclosure);

		// ✅ INLINE NOTIFICATION: Single Update
		try {
			UserDto user = userFeignClient.getUserById(saved.getEntityId());
			if (user != null && user.getEmail() != null) {
				String msg = (newStatus == DisclosureStatus.VALIDATED)
						? "Disclosure Validated: Your record has been approved."
						: "Disclosure Rejected: Please check the details and submit again.";

				NotificationRequestDto notification = NotificationRequestDto.builder().userId(user.getUserId())
						.entityId(saved.getEntityId()).category(NotificationCategory.TAX).message(msg).build();
				notificationFeignClient.sendNotification(notification, user.getEmail());
			}
		} catch (Exception e) {
			log.error("Single notification failed for disclosure {}: {}", disclosureId, e.getMessage());
		}

		return mapToResponseDTO(saved);
	}

	
	
	@Override
	public List<DisclosureResponseDTO> getAllDisclosures() {
		return disclosureRepository.findAll().stream().map(this::mapToResponseDTO).collect(Collectors.toList());
	}

	@Override
	public DisclosureResponseDTO getDisclosureByDisclosureId(Long id) {
		Disclosure d = disclosureRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Disclosure " + id + " not found"));
		return mapToResponseDTO(d);
	}

	private DisclosureResponseDTO mapToResponseDTO(Disclosure d) {
		return DisclosureResponseDTO.builder().disclosureId(d.getDisclosureId()).entityId(d.getEntityId())
				.type(d.getType()).status(d.getStatus()).submissionDate(d.getSubmissionDate()).build();
	}
}