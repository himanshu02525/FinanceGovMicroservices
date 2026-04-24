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
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Access Denied: No account associated with " + email));

        // 1. Get the role name from the database (it is already "ROLE_ADMIN")
        String roleName = user.getRole().getRoleName().name();

        // 2. Remove the "ROLE_" prefix here to avoid "ROLE_ROLE_ADMIN"
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleName);

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), 
                user.getPassword(),
                Collections.singletonList(authority)
        );
    }
}