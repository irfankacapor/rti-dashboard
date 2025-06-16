package io.dashboard.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "upload_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadFile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private UploadJob uploadJob;
    
    @Column(name = "filename", nullable = false)
    private String filename;
    
    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;
    
    @Column(name = "mime_type", nullable = false)
    private String mimeType;
    
    @Column(name = "stored_path", nullable = false)
    private String storedPath;
    
    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
} 