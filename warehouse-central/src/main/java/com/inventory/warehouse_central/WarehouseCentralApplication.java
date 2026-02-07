package com.inventory.warehouse_central;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class WarehouseCentralApplication {

	public static void main(String[] args) {
		SpringApplication.run(WarehouseCentralApplication.class, args);
	}

}
