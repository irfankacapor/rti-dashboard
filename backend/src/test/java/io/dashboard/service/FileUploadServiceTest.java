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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FileUploadServiceTest {
    @Mock
    private UploadJobRepository uploadJobRepository;
    @Mock
    private UploadFileRepository uploadFileRepository;
    @InjectMocks
    private FileUploadService fileUploadService;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(fileUploadService, "maxFileSize", 26214400L); // 25MB
        ReflectionTestUtils.setField(fileUploadService, "maxRows", 200000);
        ReflectionTestUtils.setField(fileUploadService, "uploadDirectory", tempDir.toString());
    }
    
    @Test
    void createUploadJob_shouldCreateJob() {
        UploadJob job = new UploadJob();
        job.setId(1L);
        job.setUserId("test-user");
        job.setStatus(UploadStatus.PENDING);
        when(uploadJobRepository.save(any(UploadJob.class))).thenReturn(job);
        
        FileUploadResponse response = fileUploadService.createUploadJob("test-user");
        
        assertThat(response.getJobId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getMessage()).isEqualTo("Upload job created successfully");
        verify(uploadJobRepository).save(any(UploadJob.class));
    }
    
    @Test
    void validateFile_shouldAcceptValidCsv() {
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.csv", "text/csv", "header1,header2\nvalue1,value2".getBytes()
        );
        
        fileUploadService.validateFile(file);
        // Should not throw exception
    }
    
    @Test
    void validateFile_shouldRejectEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", new byte[0]);
        
        assertThatThrownBy(() -> fileUploadService.validateFile(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("empty");
    }
    
    @Test
    void validateFile_shouldRejectLargeFile() {
        byte[] largeContent = new byte[26214401]; // 25MB + 1 byte
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", largeContent);
        
        assertThatThrownBy(() -> fileUploadService.validateFile(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("size");
    }
    
    @Test
    void validateFile_shouldRejectNonCsvContentType() {
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/plain", "content".getBytes());
        
        assertThatThrownBy(() -> fileUploadService.validateFile(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("CSV");
    }
    
    @Test
    void validateFile_shouldRejectNonCsvExtension() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/csv", "content".getBytes());
        
        assertThatThrownBy(() -> fileUploadService.validateFile(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("extension");
    }
    
    @Test
    void validateFile_shouldRejectTooManyRows() {
        StringBuilder largeCsv = new StringBuilder();
        for (int i = 0; i < 200001; i++) {
            largeCsv.append("row").append(i).append("\n");
        }
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", largeCsv.toString().getBytes());
        
        assertThatThrownBy(() -> fileUploadService.validateFile(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("rows");
    }
    
    @Test
    void validateFile_shouldRejectInvalidCsvFormat() {
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", "invalid csv without commas".getBytes());
        
        assertThatThrownBy(() -> fileUploadService.validateFile(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("comma");
    }
    
    @Test
    void getUploadJobStatus_shouldReturnStatus() {
        UploadJob job = new UploadJob();
        job.setId(1L);
        job.setStatus(UploadStatus.COMPLETED);
        job.setStartedAt(java.time.LocalDateTime.now());
        
        UploadFile file = new UploadFile();
        file.setId(1L);
        file.setFilename("test.csv");
        file.setSizeBytes(1000L);
        
        when(uploadJobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(uploadFileRepository.findByUploadJobId(1L)).thenReturn(Arrays.asList(file));
        
        UploadStatusResponse response = fileUploadService.getUploadJobStatus(1L);
        
        assertThat(response.getJobId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(response.getFiles()).hasSize(1);
        assertThat(response.getFiles().get(0).getFilename()).isEqualTo("test.csv");
    }
    
    @Test
    void getUploadJobStatus_shouldThrowNotFound() {
        when(uploadJobRepository.findById(999L)).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> fileUploadService.getUploadJobStatus(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
    
    @Test
    void updateJobStatus_shouldUpdateStatus() {
        UploadJob job = new UploadJob();
        job.setId(1L);
        job.setStatus(UploadStatus.PENDING);
        
        when(uploadJobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(uploadJobRepository.save(any(UploadJob.class))).thenReturn(job);
        
        fileUploadService.updateJobStatus(1L, UploadStatus.COMPLETED, "Success");
        
        verify(uploadJobRepository).save(any(UploadJob.class));
    }
    
    @Test
    void updateJobStatus_shouldThrowNotFound() {
        when(uploadJobRepository.findById(999L)).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> fileUploadService.updateJobStatus(999L, UploadStatus.COMPLETED, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }
    
    @Test
    void deleteUploadJob_shouldDeleteJob() {
        UploadJob job = new UploadJob();
        job.setId(1L);
        
        UploadFile file = new UploadFile();
        file.setStoredPath(tempDir.resolve("test.txt").toString());
        
        when(uploadJobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(uploadFileRepository.findByUploadJobId(1L)).thenReturn(Arrays.asList(file));
        
        fileUploadService.deleteUploadJob(1L);
        
        verify(uploadJobRepository).delete(job);
    }
    
    @Test
    void deleteUploadJob_shouldThrowNotFound() {
        when(uploadJobRepository.findById(999L)).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> fileUploadService.deleteUploadJob(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
} 