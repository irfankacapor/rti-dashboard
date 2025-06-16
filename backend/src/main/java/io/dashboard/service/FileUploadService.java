package io.dashboard.service;

import io.dashboard.dto.FileUploadResponse;
import io.dashboard.dto.UploadFileResponse;
import io.dashboard.dto.UploadStatusResponse;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.UploadFile;
import io.dashboard.model.UploadJob;
import io.dashboard.model.UploadStatus;
import io.dashboard.repository.UploadFileRepository;
import io.dashboard.repository.UploadJobRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {
    
    private final UploadJobRepository uploadJobRepository;
    private final UploadFileRepository uploadFileRepository;
    
    @Value("${app.upload.max-file-size:26214400}") // 25MB default
    private long maxFileSize;
    
    @Value("${app.upload.max-rows:200000}") // 200k rows default
    private int maxRows;
    
    @Value("${app.upload.directory:uploads}")
    private String uploadDirectory;
    
    @Transactional
    public FileUploadResponse createUploadJob(String userId) {
        UploadJob job = new UploadJob();
        job.setUserId(userId);
        job.setStatus(UploadStatus.PENDING);
        UploadJob savedJob = uploadJobRepository.save(job);
        
        FileUploadResponse response = new FileUploadResponse();
        response.setJobId(savedJob.getId());
        response.setStatus(savedJob.getStatus().name());
        response.setMessage("Upload job created successfully");
        return response;
    }
    
    @Transactional
    public UploadFileResponse saveUploadedFile(MultipartFile file, Long jobId) throws IOException {
        UploadJob job = uploadJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("UploadJob", "id", jobId));
        
        validateFile(file);
        
        // Create upload directory for this job
        Path jobDirectory = Paths.get(uploadDirectory, jobId.toString());
        Files.createDirectories(jobDirectory);
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        Path filePath = jobDirectory.resolve(uniqueFilename);
        
        // Save file
        Files.copy(file.getInputStream(), filePath);
        
        // Create database record
        UploadFile uploadFile = new UploadFile();
        uploadFile.setUploadJob(job);
        uploadFile.setFilename(originalFilename);
        uploadFile.setSizeBytes(file.getSize());
        uploadFile.setMimeType(file.getContentType());
        uploadFile.setStoredPath(filePath.toString());
        
        UploadFile savedFile = uploadFileRepository.save(uploadFile);
        
        return toUploadFileResponse(savedFile);
    }
    
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File cannot be empty");
        }
        
        // Check file size
        if (file.getSize() > maxFileSize) {
            throw new BadRequestException("File size exceeds maximum allowed size of " + (maxFileSize / 1024 / 1024) + "MB");
        }
        
        // Check file type
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        
        if (contentType == null || !contentType.equals("text/csv")) {
            throw new BadRequestException("Only CSV files are allowed");
        }
        
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new BadRequestException("File must have .csv extension");
        }
        
        // Basic CSV structure validation
        try {
            String content = new String(file.getBytes());
            String[] lines = content.split("\n");
            
            if (lines.length > maxRows) {
                throw new BadRequestException("CSV file exceeds maximum allowed rows of " + maxRows);
            }
            
            // Check if file has at least one line with comma
            if (lines.length > 0 && !lines[0].contains(",")) {
                throw new BadRequestException("Invalid CSV format: no comma separator found");
            }
            
        } catch (IOException e) {
            throw new BadRequestException("Unable to read file content");
        }
    }
    
    public UploadStatusResponse getUploadJobStatus(Long jobId) {
        UploadJob job = uploadJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("UploadJob", "id", jobId));
        
        List<UploadFile> files = uploadFileRepository.findByUploadJobId(jobId);
        List<UploadFileResponse> fileResponses = files.stream()
                .map(this::toUploadFileResponse)
                .collect(Collectors.toList());
        
        UploadStatusResponse response = new UploadStatusResponse();
        response.setJobId(job.getId());
        response.setStatus(job.getStatus().name());
        response.setStartedAt(job.getStartedAt());
        response.setFinishedAt(job.getFinishedAt());
        response.setErrorMessage(job.getErrorMessage());
        response.setFiles(fileResponses);
        
        return response;
    }
    
    @Transactional
    public void updateJobStatus(Long jobId, UploadStatus status, String errorMessage) {
        UploadJob job = uploadJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("UploadJob", "id", jobId));
        
        job.setStatus(status);
        if (status == UploadStatus.COMPLETED || status == UploadStatus.FAILED) {
            job.setFinishedAt(java.time.LocalDateTime.now());
        }
        if (errorMessage != null) {
            job.setErrorMessage(errorMessage);
        }
        
        uploadJobRepository.save(job);
    }
    
    @Transactional
    public void deleteUploadJob(Long jobId) {
        UploadJob job = uploadJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("UploadJob", "id", jobId));
        
        // Delete files from disk
        List<UploadFile> files = uploadFileRepository.findByUploadJobId(jobId);
        for (UploadFile file : files) {
            try {
                Path filePath = Paths.get(file.getStoredPath());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                log.warn("Failed to delete file: {}", file.getStoredPath(), e);
            }
        }
        
        // Delete job directory
        try {
            Path jobDirectory = Paths.get(uploadDirectory, jobId.toString());
            Files.deleteIfExists(jobDirectory);
        } catch (IOException e) {
            log.warn("Failed to delete job directory: {}", jobId, e);
        }
        
        // Delete from database (cascade will handle files)
        uploadJobRepository.delete(job);
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
    
    private UploadFileResponse toUploadFileResponse(UploadFile uploadFile) {
        UploadFileResponse response = new UploadFileResponse();
        response.setId(uploadFile.getId());
        response.setFilename(uploadFile.getFilename());
        response.setSizeBytes(uploadFile.getSizeBytes());
        response.setUploadedAt(uploadFile.getUploadedAt());
        return response;
    }
} 