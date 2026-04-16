package com.financegov.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.financegov.dto.ResourceRequestDTO;
import com.financegov.dto.ResourceResponseDTO;
import com.financegov.enums.ResourceStatus;
import com.financegov.exceptions.InvalidResourceStatusException;
import com.financegov.model.FinancialProgram; // ✅ ADDED
import com.financegov.model.Resource;
import com.financegov.repository.FinancialProgramRepository; // ✅ ADDED
import com.financegov.repository.ResourceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

	private final ResourceRepository resourceRepository;

	// ✅ ADDED – required to fetch FinancialProgram entity
	private final FinancialProgramRepository financialProgramRepository;

	private ResourceStatus mapStatus(String status) {
		try {
			return ResourceStatus.valueOf(status.toUpperCase());
		} catch (Exception ex) {
			throw new InvalidResourceStatusException("Resource status must be AVAILABLE or UTILIZED");
		}
	}

	@Override
	public ResourceResponseDTO createResource(ResourceRequestDTO dto) {

		// ✅ CHANGED – fetch FinancialProgram using programId
		FinancialProgram program = financialProgramRepository.findById(dto.getProgramId())
				.orElseThrow(() -> new RuntimeException("Program not found"));

		// ✅ CHANGED – set program ENTITY instead of programId
		Resource resource = Resource.builder().program(program).type(dto.getType()).quantity(dto.getQuantity())
				.status(mapStatus(dto.getStatus())).build();

		Resource saved = resourceRepository.save(resource);

		// ✅ CHANGED – access programId through program object
		return ResourceResponseDTO.builder().resourceId(saved.getResourceId())
				.programId(saved.getProgram().getProgramId()).type(saved.getType()).quantity(saved.getQuantity())
				.status(saved.getStatus().name()).build();
	}

	@Override
	public List<ResourceResponseDTO> getResourcesByProgramId(Long programId) {

		List<ResourceResponseDTO> responseList = new ArrayList<>();

		// ✅ CHANGED – repository method uses relationship path
		for (Resource resource : resourceRepository.findByProgramProgramId(programId)) {
			responseList.add(ResourceResponseDTO.builder().resourceId(resource.getResourceId())
					.programId(resource.getProgram().getProgramId()) // ✅ FIXED
					.type(resource.getType()).quantity(resource.getQuantity()).status(resource.getStatus().name())
					.build());
		}
		return responseList;
	}

	@Override
	public List<ResourceResponseDTO> getAllocatedResources() {

		List<ResourceResponseDTO> responseList = new ArrayList<>();

		for (Resource resource : resourceRepository.findByStatus(ResourceStatus.UTILIZED)) {
			responseList.add(ResourceResponseDTO.builder().resourceId(resource.getResourceId())
					.programId(resource.getProgram().getProgramId()) // ✅ FIXED
					.type(resource.getType()).quantity(resource.getQuantity()).status(resource.getStatus().name())
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
