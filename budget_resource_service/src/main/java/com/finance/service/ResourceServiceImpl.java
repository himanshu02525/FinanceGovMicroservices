package com.finance.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.finance.client.FinancialProgramClient;
import com.finance.dto.FinancialProgramResponseDTO;
import com.finance.dto.ResourceRequestDTO;
import com.finance.dto.ResourceResponseDTO;
import com.finance.enums.ResourceStatus;
import com.finance.exceptions.InvalidResourceStatusException;
import com.finance.model.Resource;
import com.finance.repository.ResourceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;

    // ✅ NEW – Feign client instead of FinancialProgramRepository
    private final FinancialProgramClient financialProgramClient;

    private ResourceStatus mapStatus(String status) {
        try {
            return ResourceStatus.valueOf(status.toUpperCase());
        } catch (Exception ex) {
            throw new InvalidResourceStatusException(
                    "Resource status must be AVAILABLE or UTILIZED");
        }
    }

    @Override
    public ResourceResponseDTO createResource(ResourceRequestDTO dto) {

        // ✅ MICROservice-safe: Check Program EXISTS via Feign
        FinancialProgramResponseDTO program =
                financialProgramClient.getProgramById(dto.getProgramId());

        // ✅ MICROservice-safe: Check Program ACTIVE
        if (!"ACTIVE".equalsIgnoreCase(program.getStatus())) {
            throw new IllegalStateException(
              "Resources can be allocated only when the program status is ACTIVE");
        }

        Resource resource = Resource.builder()
                .programId(dto.getProgramId())
                .type(dto.getType())
                .quantity(dto.getQuantity())
                .status(mapStatus(dto.getStatus()))
                .build();

        Resource saved = resourceRepository.save(resource);

        return ResourceResponseDTO.builder()
                .resourceId(saved.getResourceId())
                .programId(saved.getProgramId())
                .type(saved.getType())
                .quantity(saved.getQuantity())
                .status(saved.getStatus().name())
                .build();
    }

    @Override
    public List<ResourceResponseDTO> getResourcesByProgramId(Long programId) {

        List<ResourceResponseDTO> responseList = new ArrayList<>();

        for (Resource resource :
                resourceRepository.findByProgramId(programId)) {

            responseList.add(
                    ResourceResponseDTO.builder()
                            .resourceId(resource.getResourceId())
                            .programId(resource.getProgramId())
                            .type(resource.getType())
                            .quantity(resource.getQuantity())
                            .status(resource.getStatus().name())
                            .build());
        }

        return responseList;
    }

    @Override
    public List<ResourceResponseDTO> getAllocatedResources() {

        List<ResourceResponseDTO> responseList = new ArrayList<>();

        for (Resource resource :
                resourceRepository.findByStatus(ResourceStatus.UTILIZED)) {

            responseList.add(
                    ResourceResponseDTO.builder()
                            .resourceId(resource.getResourceId())
                            .programId(resource.getProgramId())
                            .type(resource.getType())
                            .quantity(resource.getQuantity())
                            .status(resource.getStatus().name())
                            .build());
        }

        return responseList;
    }

    @Override
    public String deleteResource(Long resourceId) {
        resourceRepository.deleteById(resourceId);
        return "Resource deleted successfully";
    }
}