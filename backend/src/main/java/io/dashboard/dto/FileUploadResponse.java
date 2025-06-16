package io.dashboard.dto;

import lombok.Data;

@Data
public class FileUploadResponse {
    private Long jobId;
    private String filename;
    private String status;
    private String message;
} 