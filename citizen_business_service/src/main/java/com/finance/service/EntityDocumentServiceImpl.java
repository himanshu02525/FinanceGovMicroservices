package com.finance.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import com.finance.enums.Type;
import com.finance.enums.VerificationStatus;
import com.finance.exceptions.EntityNotFoundException;
import com.finance.model.CitizenBusiness;
import com.finance.model.EntityDocument;
import com.finance.repository.CitizenBusinessRepository;
import com.finance.repository.EntityDocumentRepository;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

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

    // Handles document upload for an entity (multipart)
    @Override
    public EntityDocumentResponseDTO uploadDocument(Long entityId, DocType docType, MultipartFile file, String uploadedDate) throws IOException {

        CitizenBusiness entity = citizenRepository.findById(entityId)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found"));

        // ✅ Validate based on entity type
        validateDocumentForEntityType(entity, docType);

        // Ensure uploads directory exists and save file
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf('.')) : ".pdf";
        String uniqueName = entityId + "-" + docType.name() + "-" + UUID.randomUUID().toString() + extension;

        saveFileToDisk(uniqueName, file.getBytes());

        EntityDocument doc = new EntityDocument();
        doc.setCitizenBusiness(entity);
        doc.setDocType(docType);
        doc.setFileURI(uniqueName);
        doc.setUploadedDate(uploadedDate);
        doc.setVerificationStatus(VerificationStatus.PENDING);

        EntityDocument saved = repository.save(doc);
        log.info("Document uploaded for entityId={}, docType={}", entityId, docType);

        notifyCitizen(entity, "Your document has been uploaded and is pending verification");

        return new EntityDocumentResponseDTO(
                saved.getDocumentId(),
                entity.getEntityId(),
                saved.getDocType(),
                saved.getFileURI(),
                saved.getUploadedDate(),
                saved.getVerificationStatus()
        );
    }

    // Marks a document as verified
    @Override
    public void verifyDocument(Long entityId, DocType docType) {

        CitizenBusiness entity = citizenRepository.findById(entityId)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found"));

        List<EntityDocument> docs = repository.findAllByCitizenBusinessAndDocType(entity, docType);
        if (docs == null || docs.isEmpty()) {
            throw new EntityNotFoundException("Document not found");
        }
        
        // Get the most recent document
        EntityDocument doc = docs.stream()
                .max((d1, d2) -> {
                    if (d1.getUploadedDate() == null) return -1;
                    if (d2.getUploadedDate() == null) return 1;
                    return d1.getUploadedDate().compareTo(d2.getUploadedDate());
                })
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));

        doc.setVerificationStatus(VerificationStatus.VERIFIED);
        repository.save(doc);

        notifyCitizen(entity, "Your document has been verified successfully");
    }

   
    // Marks a document as rejected
    @Override
    public void rejectDocument(Long entityId, DocType docType) {

        CitizenBusiness entity = citizenRepository.findById(entityId)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found"));

        EntityDocument doc = repository.findByCitizenBusinessAndDocType(entity, docType)
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));

        doc.setVerificationStatus(VerificationStatus.REJECTED);
        repository.save(doc);

        notifyCitizen(entity, "Your document has been rejected. Please upload a valid document");
    }

    // Get all uploaded documents
    @Override
    public List<EntityDocumentResponseDTO> getAllDocuments() {

        return repository.findAll().stream()
                .map(doc -> new EntityDocumentResponseDTO(
                        doc.getDocumentId(),
                        doc.getCitizenBusiness().getEntityId(),
                        doc.getDocType(),
                        doc.getFileURI(),
                        doc.getUploadedDate(),
                        doc.getVerificationStatus()
                ))
                .collect(java.util.stream.Collectors.toList());
    }

    // Update document (re-upload) using multipart file
    @Override
    public EntityDocumentResponseDTO updateDocument(Long entityId, DocType docType, MultipartFile file, String uploadedDate) throws IOException {

        CitizenBusiness entity = citizenRepository.findById(entityId)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found"));

        EntityDocument doc = repository.findByCitizenBusinessAndDocType(entity, docType)
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));

        // Save new file to disk
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf('.')) : ".pdf";
        String uniqueName = entityId + "-" + docType.name() + "-" + UUID.randomUUID().toString() + extension;

        saveFileToDisk(uniqueName, file.getBytes());

        doc.setFileURI(uniqueName);
        doc.setUploadedDate(uploadedDate);
        doc.setVerificationStatus(VerificationStatus.PENDING);

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

    // Download raw file bytes for preview
    @Override
    public byte[] downloadDocument(Long entityId, DocType docType) {
        CitizenBusiness entity = citizenRepository.findById(entityId)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found"));

        EntityDocument doc = repository.findByCitizenBusinessAndDocType(entity, docType)
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));

        Path filePath = Paths.get("C:", "uploads", "documents", doc.getFileURI());
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Error reading file {}", filePath, e);
            throw new RuntimeException("File not found: " + doc.getFileURI(), e);
        }
    }

    // Helper to save bytes to disk
    private void saveFileToDisk(String fileName, byte[] fileContent) throws IOException {
        Path uploadsDir = Paths.get("C:", "uploads", "documents");
        Files.createDirectories(uploadsDir);
        Path filePath = uploadsDir.resolve(fileName);
        Files.write(filePath, fileContent);
        log.info("Saved file to {}", filePath.toAbsolutePath());
    }

    private void validateDocumentForEntityType(CitizenBusiness entity, DocType docType) {
        System.out.println("DEBUG: entity.getType() = " + entity.getType());
        System.out.println("DEBUG: docType = " + docType);
        System.out.println("DEBUG: Type.BUSINESS = " + Type.BUSINESS);
        System.out.println("DEBUG: DocType.FINANCIAL_STATEMENT = " + DocType.FINANCIAL_STATEMENT);
        
        if (entity.getType() == Type.CITIZEN) {
            if (docType == DocType.FINANCIAL_STATEMENT) {
                throw new IllegalArgumentException(
                        "Citizens cannot upload Financial Statement"
                );
            }
        }

        if (entity.getType() == Type.BUSINESS) {
            if (docType != DocType.FINANCIAL_STATEMENT) {
                throw new IllegalArgumentException(
                        "Business must upload Financial Statement"
                );
            }
        }
    }

    // Send notification
    private void notifyCitizen(CitizenBusiness entity, String message) {

        UserDto user = userFeignClient.getUserById(entity.getUserId());

        NotificationRequestDto notification =
                NotificationRequestDto.builder()
                        .userId(user.getUserId())
                        .entityId(entity.getEntityId())
                        .category(NotificationCategory.GENERAL)
                        .message(message)
                        .build();

        notificationFeignClient.sendNotification(notification, user.getEmail());
    }
    
    
}