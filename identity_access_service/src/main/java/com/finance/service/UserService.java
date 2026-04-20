package com.finance.service;

import java.util.List;
import com.finance.dto.UserCreationRequest;
import com.finance.model.User;

/**
 * Management Layer for User Identities.
 * Handles administrative actions like Officer creation, status toggling, and profile updates.
 */
public interface UserService {

    // Used by Admins to manually onboard Officers/Auditors
    String createOfficer(UserCreationRequest request);

    // Oversight: View all registered entities
    List<User> findAllUsers();
    
    User findUserById(Long id);

    // Data Retrieval: Standard lookups
    User getUserById(Long id);

    User getUserByEmail(String email);

    User getUserByUserName(String username);

    // Profile Management: For self-service or admin corrections
    User updateUser(Long id, User updatedUser);

    // Security Control: Instantly ACTIVE, SUSPENDED, or INACTIVE status
    User updateUserStatus(Long id, String status);

    // Final Action: Permanent removal (usually restricted to Super Admins)
    void deleteUser(Long id);
    
    void deleteOfficer(Long id);
}