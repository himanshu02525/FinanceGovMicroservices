package com.financegov.service;

import java.util.*;
import com.financegov.dto.*;

public interface ResourceService {
	ResourceResponseDTO createResource(ResourceRequestDTO dto);

	List<ResourceResponseDTO> getResourcesByProgramId(Long programId);

	List<ResourceResponseDTO> getAllocatedResources();

	String deleteResource(Long resourceId);
}