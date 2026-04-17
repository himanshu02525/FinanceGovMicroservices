package com.finance.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.finance.dto.CitizenBusinessRequestDTO;
import com.finance.dto.CitizenBusinessResponseDTO;
import com.finance.enums.Status;
import com.finance.exceptions.EntityNotFoundException;
import com.finance.model.CitizenBusiness;
import com.finance.repository.CitizenBusinessRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CitizenBusinessServiceImpl implements CitizenBusinessService {

    @Autowired
    private CitizenBusinessRepository repository;

    
    @Override
    public CitizenBusinessResponseDTO createCitizen(CitizenBusinessRequestDTO request) {

        log.info("Creating Citizen/Business: {}", request.getName());

        CitizenBusiness citizen = new CitizenBusiness();
        citizen.setName(request.getName());
        citizen.setType(request.getType());
        citizen.setAddress(request.getAddress());
        citizen.setContactInfo(request.getContactInfo());
        citizen.setStatus(Status.PENDING);

        CitizenBusiness saved = repository.save(citizen);

        log.info("Citizen/Business created with entityId={}", saved.getEntityId());
        return new CitizenBusinessResponseDTO(
                saved.getEntityId(),
                saved.getName(),
                saved.getType(),
                saved.getAddress(),
                saved.getContactInfo(),
                saved.getStatus()
        );
    }

    @Override
    public List<CitizenBusiness> getAllCitizens() {
        log.info("Fetching all entities");
        return repository.findAll();
    }

    @Override
    public CitizenBusiness getCitizenById(Long id) {
        log.info("Fetching entity with ID: {}", id);
        return repository.findById(id).orElseThrow(() -> {
            log.error("Entity not found with ID: {}", id);
            return new EntityNotFoundException("Entity not found");
        });
    }

    @Override
    public void deleteCitizen(Long id) {
        log.info("Deleting entity with ID: {}", id);
        CitizenBusiness citizen = getCitizenById(id);
        repository.delete(citizen);
        log.info("Entity deleted successfully with ID: {}", id);
    }

    @Override
    public CitizenBusiness updateCitizen(Long id, CitizenBusiness citizen) {
        log.info("Updating entity with ID: {}", id);
        CitizenBusiness existing = getCitizenById(id);
        existing.setName(citizen.getName());
        existing.setAddress(citizen.getAddress());
        existing.setContactInfo(citizen.getContactInfo());
        CitizenBusiness updated = repository.save(existing);
        log.info("Entity updated successfully with ID: {}", id);
        return updated;
    }

    @Override
    public CitizenBusiness approveCitizen(Long id) {
        log.info("Approving entity with ID: {}", id);
        CitizenBusiness citizen = getCitizenById(id);
        citizen.setStatus(Status.ACTIVE);
        CitizenBusiness updated = repository.save(citizen);
        log.info("Entity approved (ACTIVE) with ID: {}", id);
        return updated;
    }
}