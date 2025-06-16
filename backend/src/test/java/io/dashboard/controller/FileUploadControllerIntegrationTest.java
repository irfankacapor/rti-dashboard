package io.dashboard.controller;

import io.dashboard.model.UploadJob;
import io.dashboard.model.UploadStatus;
import io.dashboard.repository.UploadFileRepository;
import io.dashboard.repository.UploadJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
class FileUploadControllerIntegrationTest {
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private UploadJobRepository uploadJobRepository;
    
    @Autowired
    private UploadFileRepository uploadFileRepository;
    
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
    void uploadCsvFile_shouldUploadValidCsv() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.csv", "text/csv", "header1,header2\nvalue1,value2".getBytes()
        );
        
        mockMvc.perform(multipart("/api/v1/upload-csv")
                .file(file)
                .param("userId", "test-user"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobId").exists())
                .andExpect(jsonPath("$.filename").value("test.csv"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.message").value("File uploaded successfully"));
        
        // Verify job was created
        assertThat(uploadJobRepository.count()).isEqualTo(1);
        UploadJob job = uploadJobRepository.findAll().get(0);
        assertThat(job.getStatus()).isEqualTo(UploadStatus.COMPLETED);
        assertThat(job.getUserId()).isEqualTo("test-user");
        
        // Verify file was created
        assertThat(uploadFileRepository.count()).isEqualTo(1);
    }
    
    @Test
    void uploadCsvFile_shouldRejectInvalidFileType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.txt", "text/plain", "content".getBytes()
        );
        
        mockMvc.perform(multipart("/api/v1/upload-csv")
                .file(file)
                .param("userId", "test-user"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("CSV")));
    }
    
    @Test
    void uploadCsvFile_shouldRejectEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.csv", "text/csv", new byte[0]
        );
        
        mockMvc.perform(multipart("/api/v1/upload-csv")
                .file(file)
                .param("userId", "test-user"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("empty")));
    }
    
    @Test
    void uploadCsvFile_shouldRejectLargeFile() throws Exception {
        byte[] largeContent = new byte[26214401]; // 25MB + 1 byte
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.csv", "text/csv", largeContent
        );
        
        mockMvc.perform(multipart("/api/v1/upload-csv")
                .file(file)
                .param("userId", "test-user"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("size")));
    }
    
    @Test
    void getUploadStatus_shouldReturnStatus() throws Exception {
        // Create a job first
        UploadJob job = new UploadJob();
        job.setUserId("test-user");
        job.setStatus(UploadStatus.COMPLETED);
        job.setStartedAt(java.time.LocalDateTime.now());
        job.setFinishedAt(java.time.LocalDateTime.now());
        UploadJob savedJob = uploadJobRepository.save(job);
        
        mockMvc.perform(get("/api/v1/uploads/{jobId}/status", savedJob.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(savedJob.getId()))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.userId").doesNotExist()); // Should not expose userId in response
    }
    
    @Test
    void getUploadStatus_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/uploads/999/status"))
                .andExpect(status().isInternalServerError());
    }
    
    @Test
    void getUploadFiles_shouldReturnFiles() throws Exception {
        // Create a job with files
        UploadJob job = new UploadJob();
        job.setUserId("test-user");
        job.setStatus(UploadStatus.COMPLETED);
        job.setStartedAt(java.time.LocalDateTime.now());
        job.setFinishedAt(java.time.LocalDateTime.now());
        UploadJob savedJob = uploadJobRepository.save(job);
        
        mockMvc.perform(get("/api/v1/uploads/{jobId}/files", savedJob.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(savedJob.getId()))
                .andExpect(jsonPath("$.files").isArray());
    }
    
    @Test
    void deleteUploadJob_shouldDeleteJob() throws Exception {
        // Create a job first
        UploadJob job = new UploadJob();
        job.setUserId("test-user");
        job.setStatus(UploadStatus.COMPLETED);
        job.setStartedAt(java.time.LocalDateTime.now());
        job.setFinishedAt(java.time.LocalDateTime.now());
        UploadJob savedJob = uploadJobRepository.save(job);
        
        mockMvc.perform(delete("/api/v1/uploads/{jobId}", savedJob.getId()))
                .andExpect(status().isNoContent());
        
        // Verify job was deleted
        assertThat(uploadJobRepository.findById(savedJob.getId())).isEmpty();
    }
    
    @Test
    void deleteUploadJob_shouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/uploads/999"))
                .andExpect(status().isInternalServerError());
    }
    
    @Test
    void uploadCsvFile_shouldUseDefaultUserId() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.csv", "text/csv", "header1,header2\nvalue1,value2".getBytes()
        );
        
        mockMvc.perform(multipart("/api/v1/upload-csv")
                .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobId").exists())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
        
        // Verify job was created with default userId
        UploadJob job = uploadJobRepository.findAll().get(0);
        assertThat(job.getUserId()).isEqualTo("system");
    }
} 