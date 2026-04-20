package com.finance.controller;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

	// Only Admins and Auditors can see the full list of everyone in the system.
	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'GOVERNMENT_AUDITOR')")
	public List<User> getAllUsers() {
		log.info("An administrator or auditor is pulling the full user list.");
		return userService.findAllUsers();
	}

	// Looking up a specific person by their ID.
	// Compliance officers might need this for investigations.
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER')")
	public ResponseEntity<User> getUserById(@PathVariable Long id) {
		log.info("Fetching details for User ID: {}", id);
		return ResponseEntity.ok(userService.getUserById(id));
	}

	// Searching by email is a powerful tool, so we keep it restricted to Admins
	// only.
	@GetMapping("/email/{email}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
		return ResponseEntity.ok(userService.getUserByEmail(email));
	}

	// Deleting a user is a permanent action. Only the top-level Admins can do this.
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<String> deleteUser(@PathVariable Long id) {
		log.warn("CAUTION: Admin is deleting User ID: {}", id);
		userService.deleteUser(id);
		return ResponseEntity.ok("The user has been successfully removed from the system.");
	}

	// Updating a full user profile (changing names, roles, etc.)
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody User updatedUser) {
		log.info("Admin is updating profile info for User ID: {}", id);
		return ResponseEntity.ok(userService.updateUser(id, updatedUser));
	}

	// Just changing the status (e.g., Suspending an account or Activating it).
	// A Financial Officer might need to freeze an account if they see suspicious
	// activity.
	@PatchMapping("/{id}/status")
	@PreAuthorize("hasAnyRole('ADMIN', 'FINANCIAL_OFFICER')")
	public ResponseEntity<User> updateUserStatus(@PathVariable Long id, @RequestBody String status) {
		log.info("Changing account status to '{}' for User ID: {}", status, id);
		return ResponseEntity.ok(userService.updateUserStatus(id, status));
	}
}