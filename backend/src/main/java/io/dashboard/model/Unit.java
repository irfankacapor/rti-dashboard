package io.dashboard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "units")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Unit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Unit code is required")
    @Size(max = 50, message = "Unit code must not exceed 50 characters")
    @Column(unique = true, nullable = false, length = 50)
    private String code;
    
    @Size(max = 255, message = "Unit description must not exceed 255 characters")
    @Column
    private String description;
    
    @Size(max = 255, message = "Unit group must not exceed 255 characters")
    @Column(name = "unit_group", length = 255)
    private String group;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
} 