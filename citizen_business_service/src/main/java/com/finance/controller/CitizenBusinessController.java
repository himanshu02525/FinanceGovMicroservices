package com.finance.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finance.dto.CitizenBusinessRequestDTO;
import com.finance.dto.CitizenBusinessResponseDTO;
import com.finance.model.CitizenBusiness;
import com.finance.service.CitizenBusinessService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/entities")
public class CitizenBusinessController {

	private static final Logger logger = LoggerFactory.getLogger(CitizenBusinessController.class);

	@Autowired
	private CitizenBusinessService service;

	// CREATE ENTITY
	@PostMapping("/createCitizen")
	public ResponseEntity<CitizenBusinessResponseDTO> createCitizen(@RequestBody @Valid CitizenBusinessRequestDTO request) {
		return new ResponseEntity<>(service.createCitizen(request), HttpStatus.CREATED);
	}
	
	
	
	// ENTITYID FOR THE OTHER MODULES

	@GetMapping("/validate/{entityId}")
	public ResponseEntity<Boolean> validateEntity(@PathVariable Long entityId) {
		service.getCitizenById(entityId);
		return ResponseEntity.ok(true);
	}

	
	
	// GET ALL THE ENTITES
	@GetMapping("/getAllEntity")
	public List<CitizenBusiness> getAllEntities() {

		logger.info("Fetching all citizens");

		return service.getAllCitizens();
	}

	// GET CITIZEN BY ID
	@GetMapping("/getCitizenById/{id}")
	public CitizenBusiness getCitizenById(@PathVariable Long id) {

		logger.info("Fetching citizen with ID: {}", id);

		return service.getCitizenById(id);
	}

	// DELETE CITIZEN BY ID
	@DeleteMapping("/deleteById/{id}")
	public ResponseEntity<String> deleteCitizen(@PathVariable Long id) {

		logger.info("Deleting citizen with ID: {}", id);

		service.deleteCitizen(id);

		return new ResponseEntity<>("Entity deleted successfully", HttpStatus.OK);
	}

	// UPDATE CITIZEN DETAILS
	@PutMapping("/updateCitizenById/{id}")
	public ResponseEntity<String> updateCitizen(@PathVariable Long id, @Valid @RequestBody CitizenBusiness citizen) {

		logger.info("Updating citizen with ID: {}", id);

		service.updateCitizen(id, citizen);

		return new ResponseEntity<>("Entity updated successfully", HttpStatus.OK);
	}

	// ADMIN APPROVE THE CITIZEN
	@PutMapping("/approveCitizen/{id}")
	public ResponseEntity<String> approveCitizen(@PathVariable Long id) {

		logger.info("Admin approving citizen with ID: {}", id);

		service.approveCitizen(id);

		return new ResponseEntity<>("Entity approved successfully", HttpStatus.OK);
	}
}
