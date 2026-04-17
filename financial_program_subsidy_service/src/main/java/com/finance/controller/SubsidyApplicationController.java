package com.finance.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finance.dto.SubsidyApplicationRequest;
import com.finance.dto.SubsidyApplicationResponse;
import com.finance.service.SubsidyApplicationService;

@RestController
@RequestMapping("/applications")
public class SubsidyApplicationController {

	private  SubsidyApplicationService service;
	SubsidyApplicationController(SubsidyApplicationService service){
		this.service=service;
	}
	@PostMapping("/save")
	public ResponseEntity<SubsidyApplicationResponse> createApplication(
			@RequestBody SubsidyApplicationRequest request) {
		SubsidyApplicationResponse response = service.saveApplication(request);
		return ResponseEntity.ok(response);
	}

	
	@GetMapping("/fetchByEntity/{entityId}")
	public ResponseEntity<List<SubsidyApplicationResponse>> getApplicationsByEntity(@PathVariable Long entityId) {
		List<SubsidyApplicationResponse> responses = service.getApplicationsByEntity(entityId);
		return ResponseEntity.ok(responses);
	}

	@PutMapping("/approve/{id}")
	public ResponseEntity<SubsidyApplicationResponse> approveApplication(@PathVariable Long id) {
		SubsidyApplicationResponse response = service.approveApplication(id);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/reject/{id}")
	public ResponseEntity<SubsidyApplicationResponse> rejectApplication(@PathVariable Long id) {
		SubsidyApplicationResponse response = service.rejectApplication(id);
		return ResponseEntity.ok(response);
	}
}
