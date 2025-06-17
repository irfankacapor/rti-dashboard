package io.dashboard.model;

import io.dashboard.model.Indicator;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "goal_targets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalTarget {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indicator_id", nullable = false)
    private Indicator indicator;
    
    @Column(name = "target_year", nullable = false)
    private Integer targetYear;
    
    @Column(name = "target_value", nullable = false, precision = 19, scale = 6)
    private BigDecimal targetValue;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private TargetType targetType;
    
    @Column(name = "target_per", precision = 5, scale = 2)
    private BigDecimal targetPer;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum TargetType {
        ABSOLUTE,
        RELATIVE,
        PERCENTAGE_CHANGE
    }
} 