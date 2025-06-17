package io.dashboard.model;

import io.dashboard.enums.ImpactDirection;
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
@Table(name = "goal_indicators")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalIndicator {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indicator_id", nullable = false)
    private Indicator indicator;
    
    @Column(name = "aggregation_weight", precision = 5, scale = 4, nullable = false)
    private BigDecimal aggregationWeight;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "impact_direction", nullable = false)
    private ImpactDirection impactDirection;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
} 