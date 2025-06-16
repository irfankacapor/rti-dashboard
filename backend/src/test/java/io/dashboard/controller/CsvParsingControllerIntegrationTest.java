package io.dashboard.controller;

import io.dashboard.model.CsvAnalysis;
import io.dashboard.model.UploadFile;
import io.dashboard.model.UploadJob;
import io.dashboard.model.UploadStatus;
import io.dashboard.repository.CsvAnalysisRepository;
import io.dashboard.repository.UploadFileRepository;
import io.dashboard.repository.UploadJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CsvParsingControllerIntegrationTest {
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private UploadJobRepository uploadJobRepository;
    
    @Autowired
    private UploadFileRepository uploadFileRepository;
    
    @Autowired
    private CsvAnalysisRepository csvAnalysisRepository;
    
    private MockMvc mockMvc;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Create uploads directory
        try {
            Path uploadsDir = Paths.get("uploads");
            if (!Files.exists(uploadsDir)) {
                Files.createDirectories(uploadsDir);
            }
        } catch (Exception e) {
            // Ignore if directory already exists
        }
    }
    
    @Test
    void getCsvPreview_shouldReturnPreviewData() throws Exception {
        // Create upload job
        UploadJob job = new UploadJob();
        job.setUserId("test-user");
        job.setStatus(UploadStatus.COMPLETED);
        job.setStartedAt(java.time.LocalDateTime.now());
        job.setFinishedAt(java.time.LocalDateTime.now());
        UploadJob savedJob = uploadJobRepository.save(job);
        
        // Create test CSV file
        Path jobDir = Paths.get("uploads", savedJob.getId().toString());
        Files.createDirectories(jobDir);
        Path csvFile = jobDir.resolve("test.csv");
        Files.write(csvFile, "Name,Age,City\nJohn,25,New York\nJane,30,London".getBytes());
        
        // Create upload file record
        UploadFile uploadFile = new UploadFile();
        uploadFile.setUploadJob(savedJob);
        uploadFile.setFilename("test.csv");
        uploadFile.setSizeBytes(1000L);
        uploadFile.setMimeType("text/csv");
        uploadFile.setStoredPath(csvFile.toString());
        uploadFileRepository.save(uploadFile);
        
        mockMvc.perform(get("/api/v1/uploads/{jobId}/csv-preview", savedJob.getId())
                .param("filename", "test.csv"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filename").value("test.csv"))
                .andExpect(jsonPath("$.rowCount").value(2))
                .andExpect(jsonPath("$.columnCount").value(3))
                .andExpect(jsonPath("$.headers").isArray())
                .andExpect(jsonPath("$.previewData").isArray())
                .andExpect(jsonPath("$.hasHeader").value(true))
                .andExpect(jsonPath("$.delimiter").value(","));
    }
    
    @Test
    void getCsvPreview_shouldHandleNoHeader() throws Exception {
        // Create upload job
        UploadJob job = new UploadJob();
        job.setUserId("test-user");
        job.setStatus(UploadStatus.COMPLETED);
        job.setStartedAt(java.time.LocalDateTime.now());
        job.setFinishedAt(java.time.LocalDateTime.now());
        UploadJob savedJob = uploadJobRepository.save(job);
        
        // Create test CSV file without header
        Path jobDir = Paths.get("uploads", savedJob.getId().toString());
        Files.createDirectories(jobDir);
        Path csvFile = jobDir.resolve("test.csv");
        Files.write(csvFile, "John,25,New York\nJane,30,London".getBytes());
        
        // Create upload file record
        UploadFile uploadFile = new UploadFile();
        uploadFile.setUploadJob(savedJob);
        uploadFile.setFilename("test.csv");
        uploadFile.setSizeBytes(1000L);
        uploadFile.setMimeType("text/csv");
        uploadFile.setStoredPath(csvFile.toString());
        uploadFileRepository.save(uploadFile);
        
        mockMvc.perform(get("/api/v1/uploads/{jobId}/csv-preview", savedJob.getId())
                .param("filename", "test.csv"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasHeader").value(false))
                .andExpect(jsonPath("$.headers").isArray());
    }
    
    @Test
    void getCsvPreview_shouldHandleDifferentDelimiters() throws Exception {
        // Create upload job
        UploadJob job = new UploadJob();
        job.setUserId("test-user");
        job.setStatus(UploadStatus.COMPLETED);
        job.setStartedAt(java.time.LocalDateTime.now());
        job.setFinishedAt(java.time.LocalDateTime.now());
        UploadJob savedJob = uploadJobRepository.save(job);
        
        // Create test CSV file with semicolon delimiter
        Path jobDir = Paths.get("uploads", savedJob.getId().toString());
        Files.createDirectories(jobDir);
        Path csvFile = jobDir.resolve("test.csv");
        Files.write(csvFile, "Name;Age;City\nJohn;25;New York".getBytes());
        
        // Create upload file record
        UploadFile uploadFile = new UploadFile();
        uploadFile.setUploadJob(savedJob);
        uploadFile.setFilename("test.csv");
        uploadFile.setSizeBytes(1000L);
        uploadFile.setMimeType("text/csv");
        uploadFile.setStoredPath(csvFile.toString());
        uploadFileRepository.save(uploadFile);
        
        mockMvc.perform(get("/api/v1/uploads/{jobId}/csv-preview", savedJob.getId())
                .param("filename", "test.csv"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.delimiter").value(";"));
    }
    
    @Test
    void getCsvPreview_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/uploads/999/csv-preview")
                .param("filename", "nonexistent.csv"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void getCsvStructure_shouldReturnStructureAnalysis() throws Exception {
        // Create upload job
        UploadJob job = new UploadJob();
        job.setUserId("test-user");
        job.setStatus(UploadStatus.COMPLETED);
        job.setStartedAt(java.time.LocalDateTime.now());
        job.setFinishedAt(java.time.LocalDateTime.now());
        UploadJob savedJob = uploadJobRepository.save(job);
        
        // Create test CSV file
        Path jobDir = Paths.get("uploads", savedJob.getId().toString());
        Files.createDirectories(jobDir);
        Path csvFile = jobDir.resolve("test.csv");
        Files.write(csvFile, "Name,Age,City\nJohn,25,New York\nJane,30,London".getBytes());
        
        // Create upload file record
        UploadFile uploadFile = new UploadFile();
        uploadFile.setUploadJob(savedJob);
        uploadFile.setFilename("test.csv");
        uploadFile.setSizeBytes(1000L);
        uploadFile.setMimeType("text/csv");
        uploadFile.setStoredPath(csvFile.toString());
        uploadFileRepository.save(uploadFile);
        
        mockMvc.perform(get("/api/v1/uploads/{jobId}/csv-structure", savedJob.getId())
                .param("filename", "test.csv"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filename").value("test.csv"))
                .andExpect(jsonPath("$.rowCount").value(2))
                .andExpect(jsonPath("$.columnCount").value(3))
                .andExpect(jsonPath("$.headers").isArray())
                .andExpect(jsonPath("$.columns").isArray())
                .andExpect(jsonPath("$.hasHeader").value(true))
                .andExpect(jsonPath("$.delimiter").value(","));
        
        // Verify analysis was saved
        assertThat(csvAnalysisRepository.findByJobIdAndFilename(savedJob.getId(), "test.csv")).isPresent();
    }
    
    @Test
    void getCsvStructure_shouldReturnCachedAnalysis() throws Exception {
        // Create upload job
        UploadJob job = new UploadJob();
        job.setUserId("test-user");
        job.setStatus(UploadStatus.COMPLETED);
        job.setStartedAt(java.time.LocalDateTime.now());
        job.setFinishedAt(java.time.LocalDateTime.now());
        UploadJob savedJob = uploadJobRepository.save(job);
        
        // Create test CSV file
        Path jobDir = Paths.get("uploads", savedJob.getId().toString());
        Files.createDirectories(jobDir);
        Path csvFile = jobDir.resolve("test.csv");
        Files.write(csvFile, "Name,Age,City\nJohn,25,New York".getBytes());
        
        // Create upload file record
        UploadFile uploadFile = new UploadFile();
        uploadFile.setUploadJob(savedJob);
        uploadFile.setFilename("test.csv");
        uploadFile.setSizeBytes(1000L);
        uploadFile.setMimeType("text/csv");
        uploadFile.setStoredPath(csvFile.toString());
        uploadFileRepository.save(uploadFile);
        
        // First call - should create analysis
        mockMvc.perform(get("/api/v1/uploads/{jobId}/csv-structure", savedJob.getId())
                .param("filename", "test.csv"))
                .andExpect(status().isOk());
        
        // Second call - should return cached analysis
        mockMvc.perform(get("/api/v1/uploads/{jobId}/csv-structure", savedJob.getId())
                .param("filename", "test.csv"))
                .andExpect(status().isOk());
        
        // Verify only one analysis was created
        assertThat(csvAnalysisRepository.findByJobId(savedJob.getId())).hasSize(1);
    }
    
    @Test
    void analyzeAllCsvFiles_shouldAnalyzeMultipleFiles() throws Exception {
        // Create upload job
        UploadJob job = new UploadJob();
        job.setUserId("test-user");
        job.setStatus(UploadStatus.COMPLETED);
        job.setStartedAt(java.time.LocalDateTime.now());
        job.setFinishedAt(java.time.LocalDateTime.now());
        UploadJob savedJob = uploadJobRepository.save(job);
        
        // Create test CSV files
        Path jobDir = Paths.get("uploads", savedJob.getId().toString());
        Files.createDirectories(jobDir);
        
        Path csvFile1 = jobDir.resolve("test1.csv");
        Files.write(csvFile1, "Name,Age\nJohn,25".getBytes());
        
        Path csvFile2 = jobDir.resolve("test2.csv");
        Files.write(csvFile2, "City,Country\nNew York,USA".getBytes());
        
        // Create upload file records
        UploadFile uploadFile1 = new UploadFile();
        uploadFile1.setUploadJob(savedJob);
        uploadFile1.setFilename("test1.csv");
        uploadFile1.setSizeBytes(1000L);
        uploadFile1.setMimeType("text/csv");
        uploadFile1.setStoredPath(csvFile1.toString());
        uploadFileRepository.save(uploadFile1);
        
        UploadFile uploadFile2 = new UploadFile();
        uploadFile2.setUploadJob(savedJob);
        uploadFile2.setFilename("test2.csv");
        uploadFile2.setSizeBytes(1000L);
        uploadFile2.setMimeType("text/csv");
        uploadFile2.setStoredPath(csvFile2.toString());
        uploadFileRepository.save(uploadFile2);
        
        mockMvc.perform(post("/api/v1/uploads/{jobId}/analyze-csv", savedJob.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].filename").value("test1.csv"))
                .andExpect(jsonPath("$[1].filename").value("test2.csv"));
        
        // Verify analyses were created
        assertThat(csvAnalysisRepository.findByJobId(savedJob.getId())).hasSize(2);
    }
    
    @Test
    void analyzeAllCsvFiles_shouldHandleEmptyJob() throws Exception {
        // Create upload job with no files
        UploadJob job = new UploadJob();
        job.setUserId("test-user");
        job.setStatus(UploadStatus.COMPLETED);
        job.setStartedAt(java.time.LocalDateTime.now());
        job.setFinishedAt(java.time.LocalDateTime.now());
        UploadJob savedJob = uploadJobRepository.save(job);
        
        mockMvc.perform(post("/api/v1/uploads/{jobId}/analyze-csv", savedJob.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
    
    @Test
    void analyzeAllCsvFiles_shouldHandleInvalidJob() throws Exception {
        mockMvc.perform(post("/api/v1/uploads/999/analyze-csv"))
                .andExpect(status().isBadRequest());
    }
} 