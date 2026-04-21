package com.finance.service;
import java.util.List;

import com.finance.dto.EntityDocumentRequestDTO;
import com.finance.dto.EntityDocumentResponseDTO;
import com.finance.enums.DocType;
import com.finance.model.EntityDocument;


public interface EntityDocumentService {
	
	EntityDocumentResponseDTO uploadDocument(Long entityId, EntityDocumentRequestDTO request);
   
   List<EntityDocument> getAllDocuments();
   
   void verifyDocument(Long entityId, DocType docType );
   
   void rejectDocument(Long entityId, DocType docType);
   
   EntityDocumentResponseDTO updateDocument(Long entityId, DocType docType, EntityDocumentRequestDTO request);
   
 
	   
	   
	   
	
}