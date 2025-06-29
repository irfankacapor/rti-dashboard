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
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "goals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Goal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Goal group is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_group_id", nullable = false)
    private GoalGroup goalGroup;
    
    @NotNull(message = "Goal type is required")
    @Column(nullable = false)
    private String type; // 'quantitative' or 'qualitative'
    
    @NotBlank(message = "Goal name is required")
    @Size(max = 200, message = "Goal name must not exceed 200 characters")
    @Column(nullable = false)
    private String name;
    
    @Size(max = 500, message = "Goal URL must not exceed 500 characters")
    @Column(length = 500)
    private String url;
    
    @NotNull(message = "Goal year is required")
    @Column(nullable = false)
    private Integer year;
    
    @Size(max = 1000, message = "Goal description must not exceed 1000 characters")
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "JSONB")
    private String attributes;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GoalTarget> targets = new ArrayList<>();
    
    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GoalIndicator> goalIndicators = new ArrayList<>();
    
    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GoalSubarea> goalSubareas = new ArrayList<>();
} 