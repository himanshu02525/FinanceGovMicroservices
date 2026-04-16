package com.finance.service;

import java.util.List;

import com.finance.dto.ResourceRequestDTO;
import com.finance.dto.ResourceResponseDTO;

public interface ResourceService {

    ResourceResponseDTO createResource(ResourceRequestDTO dto);

    List<ResourceResponseDTO> getResourcesByProgramId(Long programId);

    List<ResourceResponseDTO> getAllocatedResources();

    String deleteResource(Long resourceId);
}