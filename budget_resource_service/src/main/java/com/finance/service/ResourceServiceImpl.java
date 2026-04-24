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
import com.finance.exceptions.ProgramNotFound;
import com.finance.exceptions.ResourceNotFoundException;
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
			throw new InvalidResourceStatusException("Resource status must be AVAILABLE or UTILIZED");
		}
	}

	@Override
	public ResourceResponseDTO createResource(ResourceRequestDTO dto) {

		FinancialProgramResponseDTO program; // ✅ declare first

		try {
			program = financialProgramClient.getProgramById(dto.getProgramId());
		} catch (RuntimeException ex) {
			throw new ProgramNotFound("Program not found with id : " + dto.getProgramId() + " So can't allocate the resources ");
		}

		
		// ✅ Now program is accessible
		if (!"ACTIVE".equalsIgnoreCase(program.getStatus())) {
			throw new IllegalStateException("Resources can be allocated only when the program status is ACTIVE");
		}

		Resource resource = Resource.builder().programId(dto.getProgramId()).type(dto.getType())
				.quantity(dto.getQuantity()).status(mapStatus(dto.getStatus())).build();

		Resource saved = resourceRepository.save(resource);

		return ResourceResponseDTO.builder().resourceId(saved.getResourceId()).programId(saved.getProgramId())
				.type(saved.getType()).quantity(saved.getQuantity()).status(saved.getStatus().name()).build();
	}

	@Override
	public List<ResourceResponseDTO> getResourcesByProgramId(Long programId) {

		List<Resource> resources = resourceRepository.findByProgramId(programId);

		if (resources.isEmpty()) {
			throw new ResourceNotFoundException("No resources exist for programId: " + programId);
		}

		return resources.stream()
				.map(resource -> ResourceResponseDTO.builder().resourceId(resource.getResourceId())
						.programId(resource.getProgramId()).type(resource.getType()).quantity(resource.getQuantity())
						.status(resource.getStatus().name()).build())
				.toList();
	}

	@Override
	public List<ResourceResponseDTO> getAllocatedResources() {

		List<Resource> resources = resourceRepository.findByStatus(ResourceStatus.UTILIZED);

		if (resources.isEmpty()) {
			throw new IllegalStateException("No allocated resources exist");
		}

		List<ResourceResponseDTO> responseList = new ArrayList<>();

		for (Resource resource : resources) {
			responseList.add(ResourceResponseDTO.builder().resourceId(resource.getResourceId())
					.programId(resource.getProgramId()).type(resource.getType()).quantity(resource.getQuantity())
					.status(resource.getStatus().name()).build());
		}

		return responseList;
	}

	@Override
	public String deleteResource(Long resourceId) {

	    if (!resourceRepository.existsById(resourceId)) {
	        throw new ResourceNotFoundException(
	                "Resource allocation not found with id: " + resourceId
	        );
	    }

	    resourceRepository.deleteById(resourceId);
	    return "Resource deleted successfully";
	}
}