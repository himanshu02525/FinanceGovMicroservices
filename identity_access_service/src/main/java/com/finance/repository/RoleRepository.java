package com.finance.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finance.enums.RoleType;
import com.finance.model.Role;


@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    
    Optional<Role> findByRoleName(RoleType roleName);
}