package com.finance.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finance.config.JwtService;
import com.finance.dto.AuthResponse;
import com.finance.dto.LoginRequest;
import com.finance.dto.RegisterRequest;
import com.finance.enums.RoleType;
import com.finance.exceptions.UserNotFoundException;
import com.finance.model.Role;
import com.finance.model.User;
import com.finance.entity.Token;
import com.finance.repository.RoleRepository;
import com.finance.repository.TokenRepository;
import com.finance.repository.UserRepository;
import com.finance.util.RoleRedirectUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final TokenRepository tokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final UserDetailsService userDetailsService;

	@Override
	public String register(RegisterRequest request) {
		log.info("Registering new citizen: {}", request.getEmail());

		if (userRepository.existsByEmail(request.getEmail())) {
			throw new RuntimeException("This email is already associated with an account.");
		}

		Role role = roleRepository.findByRoleName(RoleType.ROLE_CITIZEN)
				.orElseThrow(() -> new RuntimeException("Critical Error: Default role not found."));

		User user = new User();
		user.setUsername(request.getUsername());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setRole(role);
		user.setPhone(request.getPhone());
		user.setStatus("ACTIVE");
		user.setVerified(false);

		userRepository.save(user);
		return "Registration successful! You can now log in.";
	}

	@Override
	public AuthResponse login(LoginRequest request) {
		log.info("Login attempt for: {}", request.getEmail());

		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new UserNotFoundException("No account found with that email."));

		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			log.warn("Invalid password for user: {}", request.getEmail());
			throw new RuntimeException("Invalid email or password.");
		}

		revokeAllUserTokens(user);

		//Load UserDetails from your CustomUserDetailsService
		UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

		// Generate fresh JWT
		String token = jwtService.generateToken(userDetails);

		saveUserToken(user, token);

		String roleName = user.getRole().getRoleName().name();
		String endpoint = RoleRedirectUtil.getEndPoint(roleName);

		log.info("User {} logged in successfully. Redirecting to {}", user.getEmail(), endpoint);

		return new AuthResponse(token, "Welcome back!", roleName, endpoint);
	}

	private void saveUserToken(User user, String jwtToken) {
		Token token = Token.builder().user(user).token(jwtToken).expired(false).revoked(false).build();

		tokenRepository.save(token);
	}

	private void revokeAllUserTokens(User user) {
		var validTokens = tokenRepository.findAllValidTokensByUser(user.getId());
		if (validTokens.isEmpty())
			return;

		validTokens.forEach(t -> {
			t.setExpired(true);
			t.setRevoked(true);
		});

		tokenRepository.saveAll(validTokens);
	}
}