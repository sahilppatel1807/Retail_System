package com.inventory.warehouse.service;

import java.util.Base64;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.inventory.warehouse.dto.LoginRequest;
import com.inventory.warehouse.dto.LoginResponse;
import com.inventory.warehouse.entity.WarehouseUser;
import com.inventory.warehouse.repository.WarehouseUserRepository;

@Service
public class AuthService {
    
    private final WarehouseUserRepository userRepository;
    
    public AuthService(WarehouseUserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Login: Check if username and password are correct
     */
    public LoginResponse login(LoginRequest request) {
        
        // Check database for user
        Optional<WarehouseUser> userOpt = userRepository.findByUsernameAndPassword(
            request.getUsername(),
            request.getPassword()
        );
        
        // If not found, throw error
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Invalid credentials");
        }
        
        WarehouseUser user = userOpt.get();
        
        // Generate token (simple version)
        String token = generateToken(user.getWarehouseId(), user.getUsername());
        
        // Return success response
        return new LoginResponse(
            token,
            user.getWarehouseId(),
            user.getWarehouseName(),
            "Login successful"
        );
    }
    
    /**
     * Validate token and extract warehouse ID
     */
    public Long validateToken(String token) {
        try {
            // Decode token
            String decoded = new String(Base64.getDecoder().decode(token));
            // Extract warehouse ID (format: "1:warehouse1")
            String[] parts = decoded.split(":");
            return Long.parseLong(parts[0]);
        } catch (Exception e) {
            throw new RuntimeException("Invalid token");
        }
    }
    
    /**
     * Generate simple token (warehouseId:username)
     * Encoded in Base64
     */
    private String generateToken(Long warehouseId, String username) {
        String data = warehouseId + ":" + username;
        return Base64.getEncoder().encodeToString(data.getBytes());
    }
    
}
