package com.finance.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.finance.dto.EntityDocumentResponseDTO;
import com.finance.enums.DocType;
import com.finance.service.EntityDocumentService;

@RestController

@RequestMapping("/documents")
public class EntityDocumentController {

	@Autowired
	private EntityDocumentService service;

	// UPLOAD THE DOCUMENTS (multipart)
	@PostMapping("/uploadDoc/{entityId}")
	public ResponseEntity<EntityDocumentResponseDTO> uploadDocument(
			@PathVariable Long entityId,
			@RequestParam("docType") DocType docType,
			@RequestParam("uploadedDate") String uploadedDate,
			@RequestParam("file") MultipartFile file) throws Exception {

		EntityDocumentResponseDTO response = service.uploadDocument(entityId, docType, file, uploadedDate);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}
	// UPDATE THE DOCUMENTS
	@PutMapping("/updateDoc/{entityId}/{docType}")
	public ResponseEntity<EntityDocumentResponseDTO> updateDocument(
			@PathVariable Long entityId,
			@PathVariable DocType docType,
			@RequestParam("uploadedDate") String uploadedDate,
			@RequestParam(value = "file", required = false) MultipartFile file) throws Exception {

		// If file is null, caller may be sending a DTO style update; handle gracefully
		if (file != null) {
			EntityDocumentResponseDTO updated = service.updateDocument(entityId, docType, file, uploadedDate);
			return ResponseEntity.ok(updated);
		}

		// Fallback: no file provided
		throw new IllegalArgumentException("File must be provided for update");
	}

	// DOWNLOAD / PREVIEW DOCUMENT
	@GetMapping("/downloadDoc/{entityId}/{docType}")
	public ResponseEntity<ByteArrayResource> downloadDocument(@PathVariable Long entityId, @PathVariable DocType docType) {
		byte[] data = service.downloadDocument(entityId, docType);
		ByteArrayResource resource = new ByteArrayResource(data);

		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + "document.pdf" + "\"")
				.contentLength(data.length)
				.body(resource);
	}
	// GET ALL THE DOCUMENTS
	@GetMapping("/getAllDocument")
	public List<EntityDocumentResponseDTO> getAllDocuments() {
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
