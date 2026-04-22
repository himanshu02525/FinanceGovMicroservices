package com.finance.controller;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.finance.dto.DisclosureCreateRequestDTO;
import com.finance.dto.DisclosureResponseDTO; 
import com.finance.enums.DisclosureStatus;
import com.finance.service.DisclosureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/disclosure") 
public class DisclosureController {

    private static final Logger logger = LoggerFactory.getLogger(DisclosureController.class); 
    private final DisclosureService disclosureService; 

    @PostMapping("/enter_disclosure")
    public ResponseEntity<DisclosureResponseDTO> createDisclosure(@Valid @RequestBody DisclosureCreateRequestDTO request) {
        // Handles the submission of financial data from a citizen or business entity
        logger.info("REST request to submit disclosure for Entity ID: {}", request.getEntityId()); 
        return ResponseEntity.ok(disclosureService.createDisclosure(request)); 
    }

    @GetMapping("/all_disclosures")
    public ResponseEntity<List<DisclosureResponseDTO>> getAllDisclosures() {
        // Retrieves a complete history of all financial disclosures for administrative review
        logger.info("REST request to fetch all disclosures"); 
        return ResponseEntity.ok(disclosureService.getAllDisclosures()); 
    }

    @GetMapping("/{disclosureId}")
    public ResponseEntity<DisclosureResponseDTO> getDisclosureById(@PathVariable("disclosureId") Long disclosureId) {
        // Fetches the specific details of a single disclosure using its unique ID
        logger.info("REST request to fetch Disclosure ID: {}", disclosureId); 
        return ResponseEntity.ok(disclosureService.getDisclosureByDisclosureId(disclosureId)); 
    }
    
    @GetMapping("/entity/{entityId}")
    public ResponseEntity<List<DisclosureResponseDTO>> getDisclosureByEntityId(@PathVariable("entityId") Long entityId){
    	  // Fetches the specific details of  disclosures using its entity ID
        logger.info("REST request to fetch entity ID: {}", entityId); 
        return ResponseEntity.ok(disclosureService.getAllDisclosuresByEntityId(entityId)); 
    }



    @PatchMapping("/{disclosureId}/validate")
    public ResponseEntity<DisclosureResponseDTO> validateSingleDisclosure(
            @PathVariable("disclosureId") Long disclosureId,
            @RequestParam("status") DisclosureStatus status) {
        // Enables a financial officer to approve or reject one specific disclosure item
        logger.info("REST request: Validating single Disclosure ID: {} with Status: {}", disclosureId, status); 
        return ResponseEntity.ok(disclosureService.validateSingleDisclosure(disclosureId, status)); 
    }
}