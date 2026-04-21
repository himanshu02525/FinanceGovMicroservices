package com.finance.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.finance.dto.UserCreationRequest;
import com.finance.dto.UserResponseDto;
import com.finance.service.UserService;
import com.finance.service.AuditService;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final UserService userService;
    private final AuditService auditService;

    // =============================
    // ADMIN DASHBOARD
    // =============================
    @GetMapping("/dashboard")
    public ResponseEntity<?> getAdminDashboard() {
        log.info("Admin accessing dashboard");
        return ResponseEntity.ok(
                Map.of(
                        "status", "Success",
                        "message", "Welcome to the FinanceGov Admin Dashboard",
                        "access", "Authorized"
                )
        );
    }

    // =============================
    // CREATE INTERNAL OFFICER
    // =============================
    @PostMapping("/create-internal-user")
    public ResponseEntity<String> createInternalUser(
            @Valid @RequestBody UserCreationRequest request,
            Authentication authentication,
            HttpServletRequest servletRequest
    ) {
        log.info("Admin creating internal user: {}", request.getEmail());

        String response = userService.createOfficer(request);

        auditService.logAction(
                authentication.getName(),
                "CREATE_INTERNAL_USER",
                request.getEmail(),
                "Created user with role: " + request.getRole(),
                servletRequest
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // =============================
    // DELETE OFFICER
    // =============================
    @DeleteMapping("/officer/{id}")
    public ResponseEntity<String> deleteOfficer(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest request
    ) {
        log.info("Admin attempting to delete officer with ID: {}", id);

        userService.deleteOfficer(id);

        auditService.logAction(
                authentication.getName(),
                "DELETE_OFFICER",
                String.valueOf(id),
                "Admin deleted officer with ID: " + id,
                request
        );

        return ResponseEntity.ok("Officer removed successfully.");
    }

    // =============================
    // DELETE ANY USER
    // =============================
    @DeleteMapping("/user/{id}")
    public ResponseEntity<String> deleteAnyUser(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest request
    ) {
        log.info("Admin attempting to delete USER with ID: {}", id);

        UserResponseDto user = userService.getUserById(id);

        // ✅ Prevent deletion of Admin accounts
        if ("ROLE_ADMIN".equals(user.getRole())) {
            throw new RuntimeException("Admin accounts cannot be deleted.");
        }

        userService.deleteUser(id);

        auditService.logAction(
                authentication.getName(),
                "DELETE_USER",
                String.valueOf(id),
                "Admin deleted user with ID: " + id,
                request
        );

        return ResponseEntity.ok("User deleted successfully.");
    }
}
