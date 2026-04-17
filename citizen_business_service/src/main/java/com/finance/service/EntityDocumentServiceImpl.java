package com.finance.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.finance.dto.EntityDocumentRequestDTO;
import com.finance.dto.EntityDocumentResponseDTO;
import com.finance.enums.DocType;
import com.finance.enums.VerificationStatus;
import com.finance.exceptions.EntityNotFoundException;
import com.finance.model.CitizenBusiness;
import com.finance.model.EntityDocument;
import com.finance.repository.CitizenBusinessRepository;
import com.finance.repository.EntityDocumentRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EntityDocumentServiceImpl implements EntityDocumentService {
	@Autowired
	private EntityDocumentRepository repository;
	@Autowired
	private CitizenBusinessRepository citizenRepository;

	@Override
	public EntityDocumentResponseDTO uploadDocument(Long entityId, EntityDocumentRequestDTO request) {
		CitizenBusiness entity = citizenRepository.findById(entityId)
				.orElseThrow(() -> new EntityNotFoundException("Entity not found"));
		EntityDocument doc = new EntityDocument();
		doc.setCitizenBusiness(entity);
		doc.setDocType(request.getDocType());
		doc.setFileURI(request.getFileURI());
		doc.setUploadedDate(request.getUploadedDate());
		doc.setVerificationStatus(VerificationStatus.PENDING);
		EntityDocument saved = repository.save(doc);
		return new EntityDocumentResponseDTO(saved.getDocumentId(), entity.getEntityId(), saved.getDocType(),
				saved.getFileURI(), saved.getUploadedDate(), saved.getVerificationStatus());
	}

	@Override
	public List<EntityDocument> getAllDocuments() {
		return repository.findAll();
	}

	@Override
	public EntityDocumentResponseDTO updateDocument(Long entityId, DocType docType, EntityDocumentRequestDTO request) {
		CitizenBusiness entity = citizenRepository.findById(entityId)
				.orElseThrow(() -> new EntityNotFoundException("Entity not found"));
		EntityDocument doc = repository.findByCitizenBusinessAndDocType(entity, docType)
				.orElseThrow(() -> new EntityNotFoundException("Document not found"));
		doc.setFileURI(request.getFileURI());
		doc.setUploadedDate(request.getUploadedDate());
		EntityDocument updated = repository.save(doc);
		return new EntityDocumentResponseDTO(updated.getDocumentId(), entity.getEntityId(), updated.getDocType(),
				updated.getFileURI(), updated.getUploadedDate(), updated.getVerificationStatus());
	}

	@Override
	public void verifyDocument(Long entityId, DocType docType) {

		CitizenBusiness entity = citizenRepository.findById(entityId)

				.orElseThrow(() -> new EntityNotFoundException("Entity not found"));

		EntityDocument doc = repository

				.findByCitizenBusinessAndDocType(entity, docType)

				.orElseThrow(() -> new EntityNotFoundException("Document not found"));

		doc.setVerificationStatus(VerificationStatus.VERIFIED);

		repository.save(doc);

	}

	@Override
	public void rejectDocument(Long entityId, DocType docType) {

		CitizenBusiness entity = citizenRepository.findById(entityId)

				.orElseThrow(() -> new EntityNotFoundException("Entity not found"));

		EntityDocument doc = repository

				.findByCitizenBusinessAndDocType(entity, docType)

				.orElseThrow(() -> new EntityNotFoundException("Document not found"));

		doc.setVerificationStatus(VerificationStatus.REJECTED);

		repository.save(doc);

	}

}
