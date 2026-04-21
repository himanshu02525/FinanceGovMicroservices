package com.finance.controller;


import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.finance.dto.AuthResponse;
import com.finance.dto.LoginRequest;
import com.finance.dto.OtpPasswordRequest;
import com.finance.dto.RegisterRequest;
import com.finance.model.User;
import com.finance.repository.UserRepository;
import com.finance.service.AuditService;
import com.finance.service.AuthService;
import com.finance.service.LogoutService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

	private final AuthService authService;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JavaMailSender mailSender;
	private final AuditService auditService;
	private final LogoutService logoutService;

	// 1. PUBLIC REGISTRATION
	@PostMapping("/register")
	public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
		log.info("New user registration attempt: {}", request.getUsername());
		String response = authService.register(request);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	// 2. USER LOGIN
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
			HttpServletRequest servletRequest) {
		log.info("Login request received for: {}", request.getEmail());

		// The AuthService handles the password check and JWT generation
		AuthResponse response = authService.login(request);

		// We log every successful login. In a finance app, knowing WHEN
		// and from WHERE (IP) someone logged in is critical for security.
		auditService.logAction(request.getEmail(), "USER_LOGIN", request.getEmail(), "User logged in successfully",
				servletRequest);

		return ResponseEntity.ok(response);
	}

	// 3. FORGOT PASSWORD: OTP REQUEST
	@PostMapping("/request-otp")
	public ResponseEntity<String> requestOtp(@RequestParam("email") String email) {
		// Generate a random 6-digit number
		String otp = String.valueOf((int) (Math.random() * 900000) + 100000);

		User user = userRepository.findByEmail(email.trim()).orElseThrow(() -> new RuntimeException("User not found"));

		// Save OTP and set it to expire in 5 minutes
		user.setOtp(otp);
		user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
		userRepository.save(user);

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom("finance-portal@gov.com");
			message.setTo(email);
			message.setSubject("FinanceGov: Your Password Reset Code");
			message.setText("Hello,\n\nYour code is: " + otp
					+ "\n\nIt expires in 5 minutes. If you didn't ask for this, please ignore this email.");

			mailSender.send(message);
			return ResponseEntity.ok("Check your email. We've sent you a reset code.");
		} catch (Exception e) {
			log.error("Email failed to send: {}", e.getMessage());
			return ResponseEntity.status(500).body("We had trouble sending the email. Please try again later.");
		}
	}

	// 4. VERIFY OTP AND CHANGE PASSWORD
	@PutMapping("/verify-and-update-password")
	public ResponseEntity<String> verifyAndUpdate(@RequestBody OtpPasswordRequest request,
			HttpServletRequest servletRequest) {
		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new RuntimeException("User not found"));

		// Safety Check: Is the OTP correct and still valid?
		if (user.getOtp() == null || !user.getOtp().equals(request.getOtp())) {
			return ResponseEntity.status(401).body("That code isn't correct.");
		}
		if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
			return ResponseEntity.status(401).body("That code has expired. Please request a new one.");
		}

		// Encrypt the new password before saving it
		user.setPassword(passwordEncoder.encode(request.getNewPassword()));

		// Clear the OTP fields so they can't be used again
		user.setOtp(null);
		user.setOtpExpiry(null);
		userRepository.save(user);

		// Audit this change! Password resets are high-risk actions.
		auditService.logAction(user.getEmail(), "PASSWORD_RESET", user.getEmail(),
				"User successfully changed their password via OTP", servletRequest);

		return ResponseEntity.ok("Password updated! You can now log in with your new password.");
	}

	@PostMapping("/logout")
	public ResponseEntity<String> logout(HttpServletRequest request, Authentication authentication) {

		logoutService.logout(request, null, authentication);

		return ResponseEntity.ok("Logout successful");
	}

}