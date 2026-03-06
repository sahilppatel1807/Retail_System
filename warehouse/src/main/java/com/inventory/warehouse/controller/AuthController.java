package com.inventory.warehouse.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inventory.warehouse.dto.LoginRequest;
import com.inventory.warehouse.dto.LoginResponse;
import com.inventory.warehouse.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")  // Allow React to connect
public class AuthController {
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * Login endpoint
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Return error response
            return ResponseEntity.status(401)
                    .body(new LoginResponse(null, null, null, e.getMessage()));
        }
    }
}
