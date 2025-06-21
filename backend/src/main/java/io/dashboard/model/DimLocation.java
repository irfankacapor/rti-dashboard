package io.dashboard.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "dim_location")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DimLocation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "code", length = 20, unique = true)
    private String code; // ISO codes, postal codes, etc.
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "\"value\"", length = 100)
    private String value; // Original location value from CSV
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20)
    private LocationType type;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private DimLocation parent;
    
    @Column(name = "level")
    private Integer level; // 0=country, 1=state, 2=city, etc.
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum LocationType {
        COUNTRY, STATE, CITY, DISTRICT, REGION
    }
} 