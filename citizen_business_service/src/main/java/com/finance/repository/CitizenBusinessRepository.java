package com.finance.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finance.model.CitizenBusiness;

@Repository
public interface CitizenBusinessRepository extends JpaRepository<CitizenBusiness, Long> {
	Optional<CitizenBusiness> findByUserId(Long userId);
}
