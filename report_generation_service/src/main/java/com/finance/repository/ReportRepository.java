package com.finance.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.finance.enums.ReportScope;
import com.finance.model.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {

	List<Report> findByScope(ReportScope scope);

	Optional<Report> findTopByScopeOrderByCreatedAtDesc(ReportScope scope);
}