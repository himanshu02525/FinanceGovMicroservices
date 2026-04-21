package com.finance.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finance.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // This is the "Keyhole." Spring Security uses this during login 
    // to find the user's account by their unique email.
    Optional<User> findByEmail(String email);

    // Useful for profile pages or when users want to log in via a unique handle.
    Optional<User> findByUsername(String username);

    // These 'Exists' checks are vital for the Registration flow. 
    // They prevent two people from signing up with the same email.
    Boolean existsByEmail(String email);

    Boolean existsByUsername(String username);

    // Used by the Admin Dashboard to quickly see who is 'SUSPENDED' 
    // or who hasn't 'ACTIVE'ated their account yet.
    List<User> findByStatus(String status);
}