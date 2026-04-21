package com.finance.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.finance.model.Disclosure;
import com.finance.enums.DisclosureType;

@Repository
public interface DisclosureRepository extends JpaRepository<Disclosure, Long> {

    // Retrieves all disclosures submitted by a specific citizen or business
    List<Disclosure> findByEntityId(Long entityId);

    // Filters disclosures for an entity based on the type (INCOME or EXPENSE)
    List<Disclosure> findByEntityIdAndType(Long entityId, DisclosureType type);
    
    // Counts the total number of specific disclosure types for an entity using JPQL
    @Query("SELECT COUNT(d) FROM Disclosure d WHERE d.entityId = :entityId AND d.type = :type")
    Long countByEntityIdAndType(@Param("entityId") Long entityId, @Param("type") DisclosureType type);
}