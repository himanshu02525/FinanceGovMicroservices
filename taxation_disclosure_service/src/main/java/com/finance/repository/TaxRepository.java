package com.finance.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.finance.enums.TaxStatus;
import com.finance.model.TaxRecord;

@Repository
public interface TaxRepository extends JpaRepository<TaxRecord, Long> {

	// Optimized: Primary Key lookup should return a single Optional record
	Optional<TaxRecord> findByTaxId(Long taxId);

	// Finds all tax records for an entity in a specific fiscal year
	List<TaxRecord> findByEntityIdAndYear(Long entityId, Integer year);

	// Checks if a record already exists for an entity and year to prevent duplicate
	// filing
	boolean existsByEntityIdAndYear(Long entityId, Integer year);

	// AGGREGATION QUERIES FOR STATISTICS

	// ✅ Total unique taxpayers (all years)
	@Query("SELECT COUNT(DISTINCT t.entityId) FROM TaxRecord t")
	Integer countTotalTaxPayers();

	// ✅ Total unique taxpayers (year-wise)
	@Query("SELECT COUNT(DISTINCT t.entityId) FROM TaxRecord t WHERE t.year = :year")
	Integer countTotalTaxPayers(@Param("year") Integer year);

	// ✅ Total revenue (PAID only - all years)
	@Query("SELECT COALESCE(SUM(t.amount), 0.0) FROM TaxRecord t WHERE t.status = com.finance.enums.TaxStatus.PAID")
	Double calculateTotalRevenue();

	// ✅ Total revenue (year-wise)
	@Query("SELECT COALESCE(SUM(t.amount), 0.0) FROM TaxRecord t WHERE t.status = com.finance.enums.TaxStatus.PAID AND t.year = :year")
	Double calculateTotalRevenue(@Param("year") Integer year);

	// ✅ Count by status (all years)
	long countByStatus(TaxStatus status);

	// ✅ Count by status (year-wise)
	long countByStatusAndYear(TaxStatus status, Integer year);

	// ✅ Total records by year
	Long countByYear(Integer year);

	// ✅ Find records by entity
	List<TaxRecord> findByEntityId(Long entityId);

	// =========================
	// ✅ ANALYTICS ADDITIONS
	// =========================

	// ✅ Highest tax (all years)
	@Query("SELECT COALESCE(MAX(t.amount), 0.0) FROM TaxRecord t")
	Double findMaxTax();

	// ✅ Highest tax (year-wise)
	@Query("SELECT COALESCE(MAX(t.amount), 0.0) FROM TaxRecord t WHERE t.year = :year")
	Double findMaxTax(@Param("year") Integer year);

	// ✅ Lowest tax (all years)
	@Query("SELECT COALESCE(MIN(t.amount), 0.0) FROM TaxRecord t")
	Double findMinTax();

	// ✅ Lowest tax (year-wise)
	@Query("SELECT COALESCE(MIN(t.amount), 0.0) FROM TaxRecord t WHERE t.year = :year")
	Double findMinTax(@Param("year") Integer year);

}