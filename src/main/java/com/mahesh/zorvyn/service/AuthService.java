package com.mahesh.zorvyn.service;

import com.mahesh.zorvyn.dto.JwtResponse;
import com.mahesh.zorvyn.dto.LoginRequest;
import com.mahesh.zorvyn.dto.MessageResponse;
import com.mahesh.zorvyn.dto.SignupRequest;
import com.mahesh.zorvyn.model.Role;
import com.mahesh.zorvyn.model.User;
import com.mahesh.zorvyn.repository.UserRepository;
import com.mahesh.zorvyn.security.JwtUtils;
import com.mahesh.zorvyn.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toSet());
        
        return new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), 
                userDetails.getEmail(), roles);
    }
    
    public MessageResponse registerUser(SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            return new MessageResponse("Error: Username is already taken!");
        }
        
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return new MessageResponse("Error: Email is already in use!");
        }
        
        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        
        Set<String> strRoles = signupRequest.getRoles();
        Set<Role> roles = new HashSet<>();
        
        if (strRoles == null || strRoles.isEmpty()) {
            roles.add(Role.ROLE_USER);
        } else {
            strRoles.forEach(role -> {
                if ("admin".equalsIgnoreCase(role)) {
                    roles.add(Role.ROLE_ADMIN);
                } else {
                    roles.add(Role.ROLE_USER);
                }
            });
        }
        
        user.setRoles(roles);
        userRepository.save(user);
        
        return new MessageResponse("User registered successfully!");
    }
}
