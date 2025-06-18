package io.dashboard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "goal_targets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalTarget {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Goal is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;
    
    @NotNull(message = "Indicator is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indicator_id", nullable = false)
    private Indicator indicator;
    
    @NotNull(message = "Target year is required")
    @Column(name = "target_year", nullable = false)
    private Integer targetYear;
    
    @NotNull(message = "Target value is required")
    @Positive(message = "Target value must be positive")
    @Column(name = "target_value", nullable = false, precision = 19, scale = 6)
    private BigDecimal targetValue;
    
    @NotNull(message = "Target type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private TargetType targetType;
    
    @Column(name = "target_percentage", precision = 5, scale = 2)
    private BigDecimal targetPercentage;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
} 