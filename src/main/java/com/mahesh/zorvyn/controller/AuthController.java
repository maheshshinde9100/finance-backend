package com.mahesh.zorvyn.controller;

import com.mahesh.zorvyn.dto.JwtResponse;
import com.mahesh.zorvyn.dto.LoginRequest;
import com.mahesh.zorvyn.dto.MessageResponse;
import com.mahesh.zorvyn.dto.SignupRequest;
import com.mahesh.zorvyn.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        JwtResponse response = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        MessageResponse response = authService.registerUser(signupRequest);
        return ResponseEntity.ok(response);
    }
}
