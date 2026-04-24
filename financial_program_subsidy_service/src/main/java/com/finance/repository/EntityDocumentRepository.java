package com.finance.repository;
 
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.finance.enums.DocType;
import com.finance.model.CitizenBusiness;
import com.finance.model.EntityDocument;
                                                                                                                                                                                                                                                                                                                                              
public interface EntityDocumentRepository extends JpaRepository<EntityDocument, Long> {
	  
	   Optional<EntityDocument> findByCitizenBusinessAndDocType(CitizenBusiness citizenBusiness,DocType docType);
	}