package com.finance.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finance.client.CitizenClient;
import com.finance.dto.DisclosureCreateRequestDTO;
import com.finance.dto.DisclosureResponseDTO;
import com.finance.enums.DisclosureStatus;
import com.finance.exceptions.EntityNotFoundException;
import com.finance.exceptions.ResourceNotFoundException;
import com.finance.model.Disclosure;
import com.finance.repository.DisclosureRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DisclosureServiceImpl implements DisclosureService {
    
    private final DisclosureRepository disclosureRepository;
    private final CitizenClient citizenClient;

    @Override
    @Transactional
    public DisclosureResponseDTO processDisclosure(DisclosureCreateRequestDTO request) {
        // Try to validate the entity; if the client fails, we throw our custom exception
        boolean exists;
        try {
            exists = citizenClient.validateCitizen(request.getEntityId());
        } catch (Exception e) {
            throw new EntityNotFoundException("Entity is not found");
        }

        if (!exists) {
            throw new EntityNotFoundException("Entity ID " + request.getEntityId() + " not found.");
        }

        // Map DTO to Entity and save
        Disclosure disclosure = new Disclosure();
        disclosure.setEntityId(request.getEntityId());
        disclosure.setType(request.getType());
        disclosure.setStatus(DisclosureStatus.SUBMITTED);
        disclosure.setSubmissionDate(LocalDateTime.now());

        Disclosure saved = disclosureRepository.save(disclosure); 
        return mapToResponseDTO(saved); 
    }

    @Override
    public List<DisclosureResponseDTO> getAllDisclosures() {
        // Fetches all and maps to DTO list
        return disclosureRepository.findAll().stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Override
    public DisclosureResponseDTO getDisclosureByDisclosureId(Long id) {
        // Finds by ID or throws custom resource not found exception
        Disclosure d = disclosureRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Disclosure " + id + " not found"));
        return mapToResponseDTO(d);
    }

    @Override
    @Transactional
    public List<DisclosureResponseDTO> validateDisclosuresByEntity(Long id, DisclosureStatus s) {
        // Updates status for all records belonging to one entity
        List<Disclosure> list = disclosureRepository.findByEntityId(id);
        list.forEach(d -> d.setStatus(s));
        return disclosureRepository.saveAll(list).stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DisclosureResponseDTO validateSingleDisclosure(Long disclosureId, DisclosureStatus newStatus) {
        // Officer validation for a single record
        Disclosure disclosure = disclosureRepository.findById(disclosureId)
                .orElseThrow(() -> new ResourceNotFoundException("Disclosure record not found"));
        disclosure.setStatus(newStatus); 
        return mapToResponseDTO(disclosureRepository.save(disclosure));
    }

    private DisclosureResponseDTO mapToResponseDTO(Disclosure d) {
        // Uses Lombok Builder to create the response object
        return DisclosureResponseDTO.builder()
                .disclosureId(d.getDisclosureId())
                .entityId(d.getEntityId())
                .type(d.getType())
                .status(d.getStatus())
                .submissionDate(d.getSubmissionDate())
                .build();
    }
}