package com.finance.service;

import java.util.List;
import com.finance.dto.DisclosureCreateRequestDTO;
import com.finance.dto.DisclosureResponseDTO;
import com.finance.enums.DisclosureStatus;

public interface DisclosureService {
    // Process filing with notification context
	DisclosureResponseDTO processDisclosure(DisclosureCreateRequestDTO request);
    
    // Admin: Retrieve all disclosure records
    List<DisclosureResponseDTO> getAllDisclosures();
    

    // Retrieve single disclosure by ID
    DisclosureResponseDTO getDisclosureByDisclosureId(Long disclosureId);
    
    
//    List<DisclosureResponseDTO> getAllDisclosuresByEntityId(Long entityId);
    List<DisclosureResponseDTO> getAllDisclosuresByEntityId(Long entityId);
    
    // Officer: Validate single record with notification feedback
    DisclosureResponseDTO validateSingleDisclosure(Long disclosureId, DisclosureStatus newStatus);

}