package com.finance.service;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.finance.dto.EntityDocumentRequestDTO;
import com.finance.dto.EntityDocumentResponseDTO;
import com.finance.enums.DocType;


public interface EntityDocumentService {

    List<EntityDocumentResponseDTO> getAllDocuments();

    EntityDocumentResponseDTO uploadDocument(Long entityId, EntityDocumentRequestDTO request);

    EntityDocumentResponseDTO updateDocument(
            Long entityId,
            DocType docType,
            EntityDocumentRequestDTO request
    );

    void verifyDocument(Long entityId, DocType docType);

    void rejectDocument(Long entityId, DocType docType);

	
}

 
	   
	   
	   
	