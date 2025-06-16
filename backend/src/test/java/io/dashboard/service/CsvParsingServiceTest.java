package io.dashboard.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dashboard.dto.CsvPreviewResponse;
import io.dashboard.dto.CsvStructureResponse;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.CsvAnalysis;
import io.dashboard.model.CsvColumn;
import io.dashboard.model.UploadFile;
import io.dashboard.repository.CsvAnalysisRepository;
import io.dashboard.repository.CsvColumnRepository;
import io.dashboard.repository.UploadFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CsvParsingServiceTest {
    @Mock
    private CsvAnalysisRepository csvAnalysisRepository;
    @Mock
    private CsvColumnRepository csvColumnRepository;
    @Mock
    private UploadFileRepository uploadFileRepository;
    @Mock
    private ObjectMapper objectMapper;
    @InjectMocks
    private CsvParsingService csvParsingService;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(csvParsingService, "previewRowLimit", 100);
        ReflectionTestUtils.setField(csvParsingService, "maxColumns", 50);
    }
    
    @Test
    void detectDelimiter_shouldDetectComma() throws IOException {
        Path csvFile = tempDir.resolve("test.csv");
        Files.write(csvFile, "header1,header2,header3\nvalue1,value2,value3".getBytes());
        
        String delimiter = csvParsingService.detectDelimiter(csvFile, "UTF-8");
        
        assertThat(delimiter).isEqualTo(",");
    }
    
    @Test
    void detectDelimiter_shouldDetectSemicolon() throws IOException {
        Path csvFile = tempDir.resolve("test.csv");
        Files.write(csvFile, "header1;header2;header3\nvalue1;value2;value3".getBytes());
        
        String delimiter = csvParsingService.detectDelimiter(csvFile, "UTF-8");
        
        assertThat(delimiter).isEqualTo(";");
    }
    
    @Test
    void detectDelimiter_shouldDefaultToComma() throws IOException {
        Path csvFile = tempDir.resolve("test.csv");
        Files.write(csvFile, "header1 header2 header3".getBytes());
        
        String delimiter = csvParsingService.detectDelimiter(csvFile, "UTF-8");
        
        assertThat(delimiter).isEqualTo(",");
    }
    
    @Test
    void detectEncoding_shouldDetectUTF8() throws IOException {
        Path csvFile = tempDir.resolve("test.csv");
        Files.write(csvFile, "header1,header2\nvalue1,value2".getBytes());
        
        String encoding = csvParsingService.detectEncoding(csvFile);
        
        assertThat(encoding).isEqualTo("UTF-8");
    }
    
    @Test
    void parseAndPreview_shouldReturnPreviewData() throws IOException {
        // Create test CSV file
        Path csvFile = tempDir.resolve("test.csv");
        Files.write(csvFile, "Name,Age,City\nJohn,25,New York\nJane,30,London".getBytes());
        
        // Mock upload file
        UploadFile uploadFile = new UploadFile();
        uploadFile.setStoredPath(csvFile.toString());
        uploadFile.setFilename("test.csv");
        
        when(uploadFileRepository.findByUploadJobId(1L))
                .thenReturn(Arrays.asList(uploadFile));
        
        CsvPreviewResponse response = csvParsingService.parseAndPreview(1L, "test.csv");
        
        assertThat(response.getFilename()).isEqualTo("test.csv");
        assertThat(response.getRowCount()).isEqualTo(2L);
        assertThat(response.getColumnCount()).isEqualTo(3);
        assertThat(response.getHeaders()).containsExactly("Name", "Age", "City");
        assertThat(response.getPreviewData()).hasSize(2);
        assertThat(response.getHasHeader()).isTrue();
        assertThat(response.getDelimiter()).isEqualTo(",");
    }
    
    @Test
    void parseAndPreview_shouldHandleNoHeader() throws IOException {
        // Create test CSV file without header
        Path csvFile = tempDir.resolve("test.csv");
        Files.write(csvFile, "John,25,New York\nJane,30,London".getBytes());
        
        UploadFile uploadFile = new UploadFile();
        uploadFile.setStoredPath(csvFile.toString());
        uploadFile.setFilename("test.csv");
        
        when(uploadFileRepository.findByUploadJobId(1L))
                .thenReturn(Arrays.asList(uploadFile));
        
        CsvPreviewResponse response = csvParsingService.parseAndPreview(1L, "test.csv");
        
        assertThat(response.getHasHeader()).isFalse();
        assertThat(response.getHeaders()).containsExactly("Column_1", "Column_2", "Column_3");
    }
    
    @Test
    void parseAndPreview_shouldThrowNotFound() {
        when(uploadFileRepository.findByUploadJobId(1L))
                .thenReturn(Arrays.asList());
        
        assertThatThrownBy(() -> csvParsingService.parseAndPreview(1L, "nonexistent.csv"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
    
    @Test
    void analyzeCsvStructure_shouldReturnCachedAnalysis() throws IOException {
        // Mock existing analysis
        CsvAnalysis existingAnalysis = new CsvAnalysis();
        existingAnalysis.setId(1L);
        existingAnalysis.setFilename("test.csv");
        existingAnalysis.setRowCount(10L);
        existingAnalysis.setColumnCount(3);
        existingAnalysis.setHeaders("[\"Name\",\"Age\",\"City\"]");
        existingAnalysis.setDelimiter(",");
        existingAnalysis.setHasHeader(true);
        existingAnalysis.setEncoding("UTF-8");
        
        when(csvAnalysisRepository.findByJobIdAndFilename(1L, "test.csv"))
                .thenReturn(Optional.of(existingAnalysis));
        when(csvColumnRepository.findByCsvAnalysisId(1L))
                .thenReturn(Arrays.asList());
        
        CsvStructureResponse response = csvParsingService.analyzeCsvStructure(1L, "test.csv");
        
        assertThat(response.getAnalysisId()).isEqualTo(1L);
        assertThat(response.getFilename()).isEqualTo("test.csv");
        verify(csvAnalysisRepository, never()).save(any());
    }
    
    @Test
    void analyzeCsvStructure_shouldCreateNewAnalysis() throws IOException {
        // Create test CSV file
        Path csvFile = tempDir.resolve("test.csv");
        Files.write(csvFile, "Name,Age,City\nJohn,25,New York\nJane,30,London".getBytes());
        
        UploadFile uploadFile = new UploadFile();
        uploadFile.setStoredPath(csvFile.toString());
        uploadFile.setFilename("test.csv");
        
        when(uploadFileRepository.findByUploadJobId(1L))
                .thenReturn(Arrays.asList(uploadFile));
        when(csvAnalysisRepository.findByJobIdAndFilename(1L, "test.csv"))
                .thenReturn(Optional.empty());
        when(csvAnalysisRepository.save(any(CsvAnalysis.class)))
                .thenAnswer(invocation -> {
                    CsvAnalysis analysis = invocation.getArgument(0);
                    analysis.setId(1L);
                    return analysis;
                });
        when(csvColumnRepository.saveAll(any()))
                .thenReturn(Arrays.asList());
        when(objectMapper.writeValueAsString(any()))
                .thenReturn("[\"Name\",\"Age\",\"City\"]");
        
        CsvStructureResponse response = csvParsingService.analyzeCsvStructure(1L, "test.csv");
        
        assertThat(response.getAnalysisId()).isEqualTo(1L);
        assertThat(response.getFilename()).isEqualTo("test.csv");
        assertThat(response.getRowCount()).isEqualTo(2L);
        assertThat(response.getColumnCount()).isEqualTo(3);
        verify(csvAnalysisRepository).save(any(CsvAnalysis.class));
        verify(csvColumnRepository).saveAll(any());
    }
    
    @Test
    void analyzeAllCsvFiles_shouldAnalyzeMultipleFiles() throws IOException {
        // Create test CSV files
        Path csvFile1 = tempDir.resolve("test1.csv");
        Path csvFile2 = tempDir.resolve("test2.csv");
        Files.write(csvFile1, "Name,Age\nJohn,25".getBytes());
        Files.write(csvFile2, "City,Country\nNew York,USA".getBytes());
        
        UploadFile uploadFile1 = new UploadFile();
        uploadFile1.setStoredPath(csvFile1.toString());
        uploadFile1.setFilename("test1.csv");
        
        UploadFile uploadFile2 = new UploadFile();
        uploadFile2.setStoredPath(csvFile2.toString());
        uploadFile2.setFilename("test2.csv");
        
        when(uploadFileRepository.findByUploadJobId(1L))
                .thenReturn(Arrays.asList(uploadFile1, uploadFile2));
        when(csvAnalysisRepository.findByJobIdAndFilename(any(), any()))
                .thenReturn(Optional.empty());
        when(csvAnalysisRepository.save(any(CsvAnalysis.class)))
                .thenAnswer(invocation -> {
                    CsvAnalysis analysis = invocation.getArgument(0);
                    analysis.setId(1L);
                    return analysis;
                });
        when(csvColumnRepository.saveAll(any()))
                .thenReturn(Arrays.asList());
        when(objectMapper.writeValueAsString(any()))
                .thenReturn("[\"Name\",\"Age\"]");
        
        List<CsvStructureResponse> responses = csvParsingService.analyzeAllCsvFiles(1L);
        
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getFilename()).isEqualTo("test1.csv");
        assertThat(responses.get(1).getFilename()).isEqualTo("test2.csv");
    }
    
    @Test
    void analyzeAllCsvFiles_shouldHandleErrorsGracefully() throws IOException {
        // Create one valid and one invalid file
        Path csvFile1 = tempDir.resolve("test1.csv");
        Files.write(csvFile1, "Name,Age\nJohn,25".getBytes());
        
        UploadFile uploadFile1 = new UploadFile();
        uploadFile1.setStoredPath(csvFile1.toString());
        uploadFile1.setFilename("test1.csv");
        
        UploadFile uploadFile2 = new UploadFile();
        uploadFile2.setStoredPath("nonexistent.csv");
        uploadFile2.setFilename("test2.csv");
        
        when(uploadFileRepository.findByUploadJobId(1L))
                .thenReturn(Arrays.asList(uploadFile1, uploadFile2));
        when(csvAnalysisRepository.findByJobIdAndFilename(any(), any()))
                .thenReturn(Optional.empty());
        when(csvAnalysisRepository.save(any(CsvAnalysis.class)))
                .thenAnswer(invocation -> {
                    CsvAnalysis analysis = invocation.getArgument(0);
                    analysis.setId(1L);
                    return analysis;
                });
        when(csvColumnRepository.saveAll(any()))
                .thenReturn(Arrays.asList());
        when(objectMapper.writeValueAsString(any()))
                .thenReturn("[\"Name\",\"Age\"]");
        
        List<CsvStructureResponse> responses = csvParsingService.analyzeAllCsvFiles(1L);
        
        // Should return analysis for the valid file only
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getFilename()).isEqualTo("test1.csv");
    }
    
    @Test
    void debugCsvParsing() throws IOException {
        // Create test CSV file
        Path csvFile = tempDir.resolve("test.csv");
        Files.write(csvFile, "Name,Age,City\nJohn,25,New York\nJane,30,London".getBytes());
        
        // Mock upload file
        UploadFile uploadFile = new UploadFile();
        uploadFile.setStoredPath(csvFile.toString());
        uploadFile.setFilename("test.csv");
        
        when(uploadFileRepository.findByUploadJobId(1L))
                .thenReturn(Arrays.asList(uploadFile));
        
        CsvPreviewResponse response = csvParsingService.parseAndPreview(1L, "test.csv");
        
        System.out.println("Row count: " + response.getRowCount());
        System.out.println("Has header: " + response.getHasHeader());
        System.out.println("Headers: " + response.getHeaders());
        System.out.println("Preview data size: " + response.getPreviewData().size());
        
        // The test expects 2 data rows, but we're getting 3 total rows
        // This suggests the header detection is not working correctly
        assertThat(response.getRowCount()).isEqualTo(2L);
    }
} 