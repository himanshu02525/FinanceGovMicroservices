package com.finance.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 50)
	private String username;

	@Column(unique = true, nullable = false, length = 100)
	private String email;

	@Column(nullable = false)
	private String password; // This will store the BCrypt encoded hash, not the raw text!

	@Column(length = 15,nullable=false,unique=true)
	private String phone;

	// Standard status management: ACTIVE, SUSPENDED (for security locks), or
	// INACTIVE
	@Column(name = "status", length = 20)
	private String status = "ACTIVE";

	@Column(name = "is_verified")
	private boolean isVerified = false;

	// We use EAGER because whenever we load a user, we ALMOST ALWAYS
	// need to know their role to check permissions immediately.
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "role_id", nullable = false)
	private Role role;

	// Automatically records when the user first joined
	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	// Automatically updates every time the user profile is changed
	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	// Fields for the Password Reset flow
	private String otp;
	private LocalDateTime otpExpiry;
}