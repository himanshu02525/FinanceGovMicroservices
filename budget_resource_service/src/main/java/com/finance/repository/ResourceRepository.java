package com.financegov.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import com.financegov.model.Resource;
import com.financegov.enums.ResourceStatus;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
	
	List<Resource> findByProgramProgramId(Long programId);

	List<Resource> findByStatus(ResourceStatus status);
}