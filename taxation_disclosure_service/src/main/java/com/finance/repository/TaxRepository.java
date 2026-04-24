package com.finance.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.finance.model.TaxRecord;

@Repository
public interface TaxRepository extends JpaRepository<TaxRecord, Long> {

   
    // Optimized: Primary Key lookup should return a single Optional record
    Optional<TaxRecord> findByTaxId(Long taxId);

    // Finds all tax records for an entity in a specific fiscal year
    List<TaxRecord> findByEntityIdAndYear(Long entityId, Integer year);
    
    // Checks if a record already exists for an entity and year to prevent duplicate filing
    boolean existsByEntityIdAndYear(Long entityId, Integer year);

    // --- AGGREGATION QUERIES FOR STATISTICS ---

    @Query("SELECT COUNT(DISTINCT t.entityId) FROM TaxRecord t")
    Integer countTotalTaxPayers();
 
    @Query("SELECT COALESCE(SUM(t.amount), 0.0) FROM TaxRecord t WHERE t.status = com.finance.enums.TaxStatus.PAID")
    Double calculateTotalRevenue();
    List<TaxRecord> findByEntityId(Long entityId);
}