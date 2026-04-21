package com.finance.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.finance.dto.UserCreationRequest;
import com.finance.dto.UserResponseDto;
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

    // ========= INTERNAL =========
    @Override
    public User findUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() ->
                new UserNotFoundException("User not found with ID: " + id));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() ->
                new UserNotFoundException("No account found with email: " + email));
    }

    @Override
    public User getUserByUserName(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() ->
                new UserNotFoundException("Username not found: " + username));
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    // ========= DTO SAFE =========
    @Override
    public UserResponseDto getUserById(Long id) {
        User user = findUserById(id);

        return UserResponseDto.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .phone(user.getPhone())
            .role(user.getRole().getRoleName().name())
            .status(user.getStatus())
            .build();
    }

    @Override
    public List<UserResponseDto> getAllUserDtos() {
        return userRepository.findAll().stream()
            .map(user -> UserResponseDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().getRoleName().name())
                .status(user.getStatus())
                .build())
            .collect(Collectors.toList());
    }

    // ========= ACTIONS =========
    @Override
    public User updateUser(Long id, User updatedUser) {
        User existingUser = findUserById(id);

        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    @Override
    public User updateUserStatus(Long id, String status) {
        User user = findUserById(id);
        user.setStatus(status.toUpperCase());
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("Cannot delete: User not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public String createOfficer(UserCreationRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Creation failed: Email already registered.");
        }

        RoleType roleType = RoleType.valueOf(request.getRole());
        Role role = roleRepository.findByRoleName(roleType)
            .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setVerified(true);
        user.setStatus("ACTIVE");

        userRepository.save(user);
        return "Officer created successfully.";
    }

    @Override
    public void deleteOfficer(Long id) {
        User user = findUserById(id);

        if (user.getRole().getRoleName() == RoleType.ROLE_ADMIN) {
            throw new RuntimeException("Admin accounts cannot be deleted.");
        }

        userRepository.delete(user);
    }
}
