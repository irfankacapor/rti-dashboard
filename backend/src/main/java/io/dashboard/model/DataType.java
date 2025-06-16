package io.dashboard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "data_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Data type name is required")
    @Size(max = 100, message = "Data type name must not exceed 100 characters")
    @Column(unique = true, nullable = false, length = 100)
    private String name;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "dataType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Indicator> indicators = new ArrayList<>();
} 