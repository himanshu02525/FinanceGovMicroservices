package com.finance.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.finance.model.TaxRecord;

@Repository
public interface TaxRepository extends JpaRepository<TaxRecord, Long> {

    // Finds all tax records associated with a specific entity ID (Citizen or Business)
    List<TaxRecord> findByEntityId(Long entityId);

    // Optimized: Primary Key lookup should return a single Optional record
    Optional<TaxRecord> findByTaxId(Long taxId);

    // Finds all tax records for an entity in a specific fiscal year
    List<TaxRecord> findByEntityIdAndYear(Long entityId, Integer year);
    
    // Checks if a record already exists for an entity and year to prevent duplicate filing
    boolean existsByEntityIdAndYear(Long entityId, Integer year);

    // --- AGGREGATION QUERIES FOR STATISTICS ---

    // Counts unique entity IDs to determine the total number of distinct taxpayers
    @Query("SELECT COUNT(DISTINCT t.entityId) FROM TaxRecord t")
    long countTotalTaxPayers();

    // Sums the 'amount' column across all records to calculate total revenue
    // Returns Double to handle cases where the table might be empty (returns null)
    @Query("SELECT SUM(t.amount) FROM TaxRecord t")
    Double calculateTotalRevenue();
}