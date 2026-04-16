
package com.financegov.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.financegov.dto.ResourceRequestDTO;
import com.financegov.dto.ResourceResponseDTO;
import com.financegov.service.ResourceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

	private final ResourceService resourceService;

	@PostMapping("/createResource")
	public ResponseEntity<ResourceResponseDTO> createResource(@Valid @RequestBody ResourceRequestDTO dto) {
		return new ResponseEntity<>(resourceService.createResource(dto), HttpStatus.CREATED);
	}

	@GetMapping("/program/{programId}")
	public ResponseEntity<List<ResourceResponseDTO>> getResourcesByProgram(@PathVariable Long programId) {
		return ResponseEntity.ok(resourceService.getResourcesByProgramId(programId));
	}

	@GetMapping("/getAllallocated")
	public ResponseEntity<List<ResourceResponseDTO>> getAllocatedResources() {
		return ResponseEntity.ok(resourceService.getAllocatedResources());
	}

	@DeleteMapping("deleteResource/{resourceId}")
	public ResponseEntity<Map<String, String>> deleteResource(@PathVariable Long resourceId) {
		Map<String, String> response = new HashMap<>();
		response.put("message", resourceService.deleteResource(resourceId));
		return ResponseEntity.ok(response);
	}
}
