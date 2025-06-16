package io.dashboard.controller;

import io.dashboard.dto.FileUploadResponse;
import io.dashboard.dto.UploadStatusResponse;
import io.dashboard.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Slf4j
public class FileUploadController {
    
    private final FileUploadService fileUploadService;
    
    @PostMapping("/upload-csv")
    public ResponseEntity<FileUploadResponse> uploadCsvFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", defaultValue = "system") String userId) {
        
        try {
            // Create upload job
            FileUploadResponse jobResponse = fileUploadService.createUploadJob(userId);
            Long jobId = jobResponse.getJobId();
            
            // Update job status to processing
            fileUploadService.updateJobStatus(jobId, io.dashboard.model.UploadStatus.PROCESSING, null);
            
            // Save the uploaded file
            fileUploadService.saveUploadedFile(file, jobId);
            
            // Update job status to completed
            fileUploadService.updateJobStatus(jobId, io.dashboard.model.UploadStatus.COMPLETED, null);
            
            // Update response
            jobResponse.setFilename(file.getOriginalFilename());
            jobResponse.setStatus("COMPLETED");
            jobResponse.setMessage("File uploaded successfully");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(jobResponse);
            
        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage(), e);
            
            // If we have a jobId, update it to failed
            try {
                FileUploadResponse jobResponse = fileUploadService.createUploadJob(userId);
                fileUploadService.updateJobStatus(jobResponse.getJobId(), 
                    io.dashboard.model.UploadStatus.FAILED, e.getMessage());
                
                jobResponse.setFilename(file.getOriginalFilename());
                jobResponse.setStatus("FAILED");
                jobResponse.setMessage("Upload failed: " + e.getMessage());
                
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jobResponse);
            } catch (Exception ex) {
                log.error("Error creating failed job: {}", ex.getMessage(), ex);
                FileUploadResponse errorResponse = new FileUploadResponse();
                errorResponse.setStatus("FAILED");
                errorResponse.setMessage("Upload failed: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
        }
    }
    
    @GetMapping("/uploads/{jobId}/status")
    public ResponseEntity<UploadStatusResponse> getUploadStatus(@PathVariable Long jobId) {
        try {
            UploadStatusResponse status = fileUploadService.getUploadJobStatus(jobId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error getting upload status for job {}: {}", jobId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/uploads/{jobId}/files")
    public ResponseEntity<UploadStatusResponse> getUploadFiles(@PathVariable Long jobId) {
        try {
            UploadStatusResponse status = fileUploadService.getUploadJobStatus(jobId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error getting upload files for job {}: {}", jobId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/uploads/{jobId}")
    public ResponseEntity<Void> deleteUploadJob(@PathVariable Long jobId) {
        try {
            fileUploadService.deleteUploadJob(jobId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting upload job {}: {}", jobId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 