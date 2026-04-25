package com.finance.service;

import java.util.List;

import com.finance.dto.CitizenBusinessRequestDTO;
import com.finance.dto.CitizenBusinessResponseDTO;
import com.finance.model.CitizenBusiness;

public interface CitizenBusinessService {

    CitizenBusinessResponseDTO createCitizen(CitizenBusinessRequestDTO request);

    CitizenBusiness getCitizenById(Long id);

    List<CitizenBusiness> getAllCitizens();

    void deleteCitizen(Long id);

    CitizenBusiness updateCitizen(Long id, CitizenBusiness citizen);

    CitizenBusiness approveCitizen(Long id);
}
