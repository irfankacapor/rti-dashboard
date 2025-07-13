package io.dashboard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "indicators")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Indicator {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Indicator code is required")
    @Size(max = 50, message = "Indicator code must not exceed 50 characters")
    @Column(unique = true, nullable = false, length = 50)
    private String code;
    
    @NotBlank(message = "Indicator name is required")
    @Size(max = 255, message = "Indicator name must not exceed 255 characters")
    @Column(nullable = false)
    private String name;
    
    @Size(max = 1000, message = "Indicator description must not exceed 1000 characters")
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @NotNull(message = "Is composite flag is required")
    @Column(name = "is_composite", nullable = false)
    private Boolean isComposite = false;
    
    @Column(name = "unit_prefix", length = 100)
    private String unitPrefix;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @Column(name = "unit_suffix", length = 100)
    private String unitSuffix;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_type_id")
    private DataType dataType;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "indicator", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GoalIndicator> goalIndicators = new ArrayList<>();
} 