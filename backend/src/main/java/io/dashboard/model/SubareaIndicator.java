package io.dashboard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "subarea_indicators")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubareaIndicator {
    
    @EmbeddedId
    private SubareaIndicatorId id;
    
    @NotNull(message = "Direction is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Direction direction;
    
    @Column(name = "aggregation_weight")
    private Double aggregationWeight;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("subareaId")
    @JoinColumn(name = "subarea_id")
    private Subarea subarea;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("indicatorId")
    @JoinColumn(name = "indicator_id")
    private Indicator indicator;
    
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubareaIndicatorId {
        @Column(name = "subarea_id")
        private Long subareaId;
        
        @Column(name = "indicator_id")
        private Long indicatorId;
    }
} 