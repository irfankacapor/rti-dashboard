package io.dashboard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subareas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subarea {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Subarea code is required")
    @Size(max = 50, message = "Subarea code must not exceed 50 characters")
    @Column(unique = true, nullable = false, length = 50)
    private String code;
    
    @NotBlank(message = "Subarea name is required")
    @Size(max = 255, message = "Subarea name must not exceed 255 characters")
    @Column(nullable = false)
    private String name;
    
    @Size(max = 1000, message = "Subarea description must not exceed 1000 characters")
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @NotNull(message = "Area is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "subarea", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SubareaIndicator> subareaIndicators = new ArrayList<>();
} 