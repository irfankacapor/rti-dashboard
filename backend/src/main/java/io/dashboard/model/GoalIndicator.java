package io.dashboard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "goal_indicators")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalIndicator {
    
    @EmbeddedId
    private GoalIndicatorId id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("goalId")
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("indicatorId")
    @JoinColumn(name = "indicator_id", nullable = false)
    private Indicator indicator;
    
    @NotNull(message = "Aggregation weight is required")
    @DecimalMin(value = "0.0", message = "Aggregation weight must be at least 0.0")
    @DecimalMax(value = "1.0", message = "Aggregation weight must be at most 1.0")
    @Column(name = "aggregation_weight", nullable = false)
    private Double aggregationWeight;
    
    @NotNull(message = "Impact direction is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "impact_direction", nullable = false)
    private ImpactDirection impactDirection;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoalIndicatorId implements Serializable {
        
        @Column(name = "goal_id")
        private Long goalId;
        
        @Column(name = "indicator_id")
        private Long indicatorId;
    }
} 