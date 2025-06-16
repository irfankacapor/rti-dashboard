package io.dashboard.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UploadFileResponse {
    private Long id;
    private String filename;
    private Long sizeBytes;
    private LocalDateTime uploadedAt;
} 