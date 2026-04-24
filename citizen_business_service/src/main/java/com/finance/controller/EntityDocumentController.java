package com.finance.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finance.dto.EntityDocumentRequestDTO;
import com.finance.dto.EntityDocumentResponseDTO;
import com.finance.enums.DocType;
import com.finance.model.EntityDocument;
import com.finance.service.EntityDocumentService;

@RestController

@RequestMapping("/documents")

public class EntityDocumentController {

	@Autowired
	private EntityDocumentService service;

	// UPLOAD THE DOCUMENTS 
	@PostMapping("/uploadDoc/{entityId}")
	public ResponseEntity<EntityDocumentResponseDTO> uploadDocument(@PathVariable Long entityId,
			@RequestBody EntityDocumentRequestDTO request) {
		return new ResponseEntity<>(service.uploadDocument(entityId, request), HttpStatus.CREATED);
	}
	// UPDATE THE DOCUMENTS
	@PutMapping("/updateDoc/{entityId}/{docType}")
	public ResponseEntity<EntityDocumentResponseDTO> updateDocument(@PathVariable Long entityId,
			@PathVariable DocType docType, @RequestBody EntityDocumentRequestDTO request) {
		return ResponseEntity.ok(service.updateDocument(entityId, docType, request));
	}
	// GET ALL THE DOCUMENTS
	@GetMapping("/getAllDocument")
	public List<EntityDocument> getAllDocuments() {

		return service.getAllDocuments();

	}
	// ADMIN WILL VERIFY THE DOCUMENT
	@PutMapping("/verify/{entityId}/{docType}")
	public ResponseEntity<String> verifyDocument(@PathVariable Long entityId, @PathVariable DocType docType) {
		service.verifyDocument(entityId, docType);
		return ResponseEntity.ok("Document verified successfully");
	}
	// ADMIN WILL REJECT THE DOCUMENT
	@PutMapping("/reject/{entityId}/{docType}")
	public ResponseEntity<String> rejectDocument(@PathVariable Long entityId, @PathVariable DocType docType) {
		service.rejectDocument(entityId, docType);
		return ResponseEntity.ok("Document rejected successfully");
	}

}
