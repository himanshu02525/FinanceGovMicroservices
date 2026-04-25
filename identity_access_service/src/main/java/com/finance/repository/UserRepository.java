package com.finance.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finance.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Used during login
    Optional<User> findByEmail(String email);

    // Used for username-based lookup
    Optional<User> findByUsername(String username);

    // ✅ Duplicate checks for registration
    Boolean existsByEmail(String email);

    Boolean existsByUsername(String username);

    // ✅ ADD THIS METHOD (FIXES YOUR ERROR)
    Boolean existsByPhone(String phone);

    // Used by admin dashboards
    List<User> findByStatus(String status);
}