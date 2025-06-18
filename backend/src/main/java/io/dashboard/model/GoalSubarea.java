package io.dashboard.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "goal_subareas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalSubarea {
    
    @EmbeddedId
    private GoalSubareaId id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("goalId")
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("subareaId")
    @JoinColumn(name = "subarea_id", nullable = false)
    private Subarea subarea;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoalSubareaId implements Serializable {
        
        @Column(name = "goal_id")
        private Long goalId;
        
        @Column(name = "subarea_id")
        private Long subareaId;
    }
} 