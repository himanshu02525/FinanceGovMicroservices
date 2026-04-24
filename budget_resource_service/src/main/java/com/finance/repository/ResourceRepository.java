package com.finance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.finance.enums.ResourceStatus;
import com.finance.model.Resource;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    // ✅ CHANGED: microservice-safe (no JPA relationship)
    List<Resource> findByProgramId(Long programId);

    List<Resource> findByStatus(ResourceStatus status);
}