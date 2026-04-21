package com.finance.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.finance.dto.UserResponseDto;
import com.finance.model.User;
import com.finance.service.UserService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    // =============================
    // GET ALL USERS (SAFE DTO)
    // =============================
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GOVERNMENT_AUDITOR')")
    public List<UserResponseDto> getAllUsers() {
        log.info("Admin/Auditor fetching user list");
        return userService.getAllUserDtos();
    }

    // =============================
    // GET USER BY ID (SAFE DTO)
    // =============================
    @GetMapping("/getuserbyid/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER')")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        log.info("Fetching user DTO for ID: {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // =============================
    // GET USER BY EMAIL (INTERNAL)
    // =============================
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    // =============================
    // DELETE USER
    // =============================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        log.warn("Admin deleting User ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok("The user has been successfully removed from the system.");
    }

    // =============================
    // UPDATE USER PROFILE
    // =============================
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody User updatedUser
    ) {
        log.info("Admin updating User ID: {}", id);
        return ResponseEntity.ok(userService.updateUser(id, updatedUser));
    }

    // =============================
    // UPDATE USER STATUS
    // =============================
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCIAL_OFFICER')")
    public ResponseEntity<User> updateUserStatus(
            @PathVariable Long id,
            @RequestBody String status
    ) {
        log.info("Updating status for User ID: {}", id);
        return ResponseEntity.ok(userService.updateUserStatus(id, status));
    }
}
