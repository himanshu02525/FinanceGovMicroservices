package com.finance.service;

import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.finance.dto.UserCreationRequest;
import com.finance.enums.RoleType;
import com.finance.exceptions.UserNotFoundException;
import com.finance.model.Role;
import com.finance.model.User;
import com.finance.repository.RoleRepository;
import com.finance.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional 
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
    }

    @Override
    public User getUserById(Long id) {
        return findUserById(id);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("No account found with email: " + email));
    }

    @Override
    public User getUserByUserName(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Username not found: " + username));
    }

    @Override
    public User updateUser(Long id, User updatedUser) {
        User existingUser = findUserById(id);

        // Standard profile updates
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());

        // Only update password if a new one is actually provided
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            log.info("Password updated for User ID: {}", id);
        }

        return userRepository.save(existingUser);
    }

    @Override
    public User updateUserStatus(Long id, String status) {
        User user = findUserById(id);
        user.setStatus(status.toUpperCase()); // Ensure status is stored consistently
        log.info("Security Status Update: User {} is now {}", user.getEmail(), status);
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("Cannot delete: User not found with ID: " + id);
        }
        userRepository.deleteById(id);
        log.warn("PERMANENT DELETE: User ID {} removed from the system.", id);
    }

    @Override
    public String createOfficer(UserCreationRequest request) {
        log.info("Admin attempt to create internal officer: {}", request.getEmail());

        // 1. Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Creation failed: Email already registered.");
        }

        // 2. Map and Verify the Role
        RoleType requestedRoleType;
        try {
            requestedRoleType = RoleType.valueOf(request.getRole());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role provided: " + request.getRole());
        }

        Role officialRole = roleRepository.findByRoleName(requestedRoleType)
                .orElseThrow(() -> new RuntimeException("Database Error: Role " + requestedRoleType + " does not exist."));

        // 3. Assemble the internal user
        User internalUser = new User();
        internalUser.setUsername(request.getUsername());
        internalUser.setEmail(request.getEmail());
        internalUser.setPassword(passwordEncoder.encode(request.getPassword()));
        internalUser.setRole(officialRole);
        
        // Internal staff are pre-verified and active by default
        internalUser.setVerified(true);
        internalUser.setStatus("ACTIVE");

        userRepository.save(internalUser);
        log.info("Success: {} account created for {}", requestedRoleType, request.getEmail());
        
        return "Internal account (" + requestedRoleType + ") created successfully for " + request.getUsername();
    }
    
    @Override
    public void deleteOfficer(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Officer not found with ID: " + id));

        // Prevent deletion of Admin accounts
        if (user.getRole().getRoleName() == RoleType.ROLE_ADMIN) {
            throw new RuntimeException("Admin accounts cannot be deleted.");
        }

        userRepository.delete(user);
        log.warn("Officer with ID {} deleted successfully.", id);
    }

}