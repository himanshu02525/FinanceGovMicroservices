package com.finance.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.finance.model.User;
import com.finance.repository.UserRepository;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Fetch the user from the database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Access Denied: No account associated with " + email));

        // 1. Extract the role name from our Enum
        String roleName = user.getRole().getRoleName().name();

        // 2. Map the role to a GrantedAuthority. 
        // Spring Security uses these "Authorities" to decide if a user can access an endpoint.
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleName);

        // 3. We return Spring's built-in User object.
        // It handles the password comparison behind the scenes using the BCryptPasswordEncoder.
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), 
                user.getPassword(),
                Collections.singletonList(authority)
        );
    }
}