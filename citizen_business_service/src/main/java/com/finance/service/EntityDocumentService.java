package com.finance.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.finance.dto.EntityDocumentResponseDTO;
import com.finance.enums.DocType;

public interface EntityDocumentService {

    List<EntityDocumentResponseDTO> getAllDocuments();

    // Upload document via multipart file
    EntityDocumentResponseDTO uploadDocument(Long entityId, DocType docType, MultipartFile file, String uploadedDate) throws Exception;

    // Update (re-upload) document
    EntityDocumentResponseDTO updateDocument(Long entityId, DocType docType, MultipartFile file, String uploadedDate) throws Exception;

    void verifyDocument(Long entityId, DocType docType);

    void rejectDocument(Long entityId, DocType docType);

    // Return raw file bytes for download/preview
    byte[] downloadDocument(Long entityId, DocType docType);

}
	

 

	   

	   

	   

