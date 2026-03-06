package com.inventory.warehouse.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.inventory.warehouse.entity.WarehouseUser;
import com.inventory.warehouse.repository.WarehouseUserRepository;

@Configuration
public class DataSeeder {
    
    @Bean
    public CommandLineRunner seedUsers(WarehouseUserRepository userRepository) {
        return args -> {
            // Only seed if no users exist
            if (userRepository.count() == 0) {
                userRepository.save(new WarehouseUser(
                    null, 
                    "warehouse1", 
                    "pass1", 
                    1L, 
                    "Warehouse-1"
                ));
                
                userRepository.save(new WarehouseUser(
                    null, 
                    "warehouse2", 
                    "pass2", 
                    2L, 
                    "Warehouse-2"
                ));
                
                userRepository.save(new WarehouseUser(
                    null, 
                    "warehouse3", 
                    "pass3", 
                    3L, 
                    "Warehouse-3"
                ));
                
                System.out.println("✅ Seeded warehouse users");
            }
        };
    }
}
