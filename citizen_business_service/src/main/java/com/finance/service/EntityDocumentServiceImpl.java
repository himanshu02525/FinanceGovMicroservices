package com.finance.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.finance.client.NotificationFeignClient;
import com.finance.client.UserFeignClient;
import com.finance.dto.EntityDocumentRequestDTO;
import com.finance.dto.EntityDocumentResponseDTO;
import com.finance.dto.NotificationRequestDto;
import com.finance.dto.UserDto;
import com.finance.enums.DocType;
import com.finance.enums.NotificationCategory;
import com.finance.enums.VerificationStatus;
import com.finance.exceptions.EntityNotFoundException;
import com.finance.model.CitizenBusiness;
import com.finance.model.EntityDocument;
import com.finance.repository.CitizenBusinessRepository;
import com.finance.repository.EntityDocumentRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@Service
public class EntityDocumentServiceImpl implements EntityDocumentService {

    @Autowired
    private EntityDocumentRepository repository;

    @Autowired
    private CitizenBusinessRepository citizenRepository;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private NotificationFeignClient notificationFeignClient;

    // =====================================================
    // 1️⃣ Document Uploaded
    // =====================================================
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

        log.info("Document uploaded for entityId={}, docType={}", entityId, request.getDocType());

        // 🔔 Notification: Document Uploaded
        notifyCitizen(
            entity,
            "Your document has been uploaded and is pending verification"
        );

        return new EntityDocumentResponseDTO(
                saved.getDocumentId(),
                entity.getEntityId(),
                saved.getDocType(),
                saved.getFileURI(),
                saved.getUploadedDate(),
                saved.getVerificationStatus()
        );
    }

    // =====================================================
    // 2️⃣ Document Verified
    // =====================================================
    @Override
    public void verifyDocument(Long entityId, DocType docType) {

        CitizenBusiness entity = citizenRepository.findById(entityId)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found"));

        EntityDocument doc = repository.findByCitizenBusinessAndDocType(entity, docType)
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));

        doc.setVerificationStatus(VerificationStatus.VERIFIED);
        repository.save(doc);

        log.info("Document verified for entityId={}, docType={}", entityId, docType);

        // 🔔 Notification: Document Verified
        notifyCitizen(
            entity,
            "Your document has been verified successfully"
        );
    }

    // =====================================================
    // 3️⃣ Document Rejected
    // =====================================================
    @Override
    public void rejectDocument(Long entityId, DocType docType) {

        CitizenBusiness entity = citizenRepository.findById(entityId)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found"));

        EntityDocument doc = repository.findByCitizenBusinessAndDocType(entity, docType)
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));

        doc.setVerificationStatus(VerificationStatus.REJECTED);
        repository.save(doc);

        log.info("Document rejected for entityId={}, docType={}", entityId, docType);

        // 🔔 Notification: Document Rejected
        notifyCitizen(
            entity,
            "Your document has been rejected. Please upload a valid document"
        );
    }
    //
    // =====================================================
    // Common Notification Method
    // =====================================================
    private void notifyCitizen(CitizenBusiness entity, String message) {

        Long userId = entity.getUserId();
        UserDto user = userFeignClient.getUserById(userId);

        NotificationRequestDto notification =
                NotificationRequestDto.builder()
                    .userId(user.getUserId())
                    .entityId(entity.getEntityId())
                    .category(NotificationCategory.GENERAL)
                    .message(message)
                    .build();

        notificationFeignClient.sendNotification(
            notification,
            user.getEmail()
        );

        log.info("Notification sent to {}", user.getEmail());
    }

    // =====================================================
    // Fetch APIs (unchanged)
    // =====================================================
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

        return new EntityDocumentResponseDTO(
                updated.getDocumentId(),
                entity.getEntityId(),
                updated.getDocType(),
                updated.getFileURI(),
                updated.getUploadedDate(),
                updated.getVerificationStatus()
        );
    }
}
