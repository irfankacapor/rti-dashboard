package io.dashboard.config;

import io.dashboard.model.DataType;
import io.dashboard.repository.DataTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataTypeInitializer {
    
    @Bean
    public CommandLineRunner ensureDataTypes(DataTypeRepository dataTypeRepository) {
        return args -> {
            // Define the required data types
            String[] requiredDataTypes = {"integer", "decimal", "percentage", "index"};
            
            for (String dataTypeName : requiredDataTypes) {
                if (!dataTypeRepository.findAll().stream()
                        .anyMatch(dt -> dt.getName().equalsIgnoreCase(dataTypeName))) {
                    DataType dataType = new DataType();
                    dataType.setName(dataTypeName);
                    dataTypeRepository.save(dataType);
                    System.out.println("Data type created: " + dataTypeName);
                }
            }
        };
    }
} 