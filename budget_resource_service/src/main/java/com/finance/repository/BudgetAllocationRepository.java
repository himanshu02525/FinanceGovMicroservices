package com.finance.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.finance.model.BudgetAllocation;

public interface BudgetAllocationRepository extends JpaRepository<BudgetAllocation, Long> {

    // ✅ CHANGED: use programId column instead of JPA relationship
    List<BudgetAllocation> findByProgramId(Long programId);

    // ✅ CHANGED: query updated for microservice-safe design
    @Query("""
            SELECT COALESCE(SUM(b.amount), 0)
            FROM BudgetAllocation b
            WHERE b.programId = :programId
           """)
    BigDecimal getTotalAllocatedByProgramId(@Param("programId") Long programId);

}