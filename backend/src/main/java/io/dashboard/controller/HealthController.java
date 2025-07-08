package io.dashboard.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.security.PermitAll;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1")
public class HealthController {
    
    @GetMapping("/health")
    @PermitAll
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse("UP", LocalDateTime.now()));
    }
    
    @Data
    public static class HealthResponse {
        private String status;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime timestamp;
        
        public HealthResponse(String status, LocalDateTime timestamp) {
            this.status = status;
            this.timestamp = timestamp;
        }
    }
} 