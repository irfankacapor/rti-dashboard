package io.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dashboard.dto.DimensionMappingRequest;
import io.dashboard.dto.DimensionMappingResponse;
import io.dashboard.dto.DimensionValidationResponse;
import io.dashboard.dto.MultiDimensionalAnalysis;
import io.dashboard.model.*;
import io.dashboard.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class DimensionControllerIntegrationTest {
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private UploadJobRepository uploadJobRepository;
    
    @Autowired
    private UploadFileRepository uploadFileRepository;
    
    @Autowired
    private CsvAnalysisRepository csvAnalysisRepository;
    
    @Autowired
    private CsvColumnMappingRepository csvColumnMappingRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private MockMvc mockMvc;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }
    
    @Test
    void createColumnDimensionMapping_shouldCreateMapping() throws Exception {
        // Given
        UploadJob job = createTestJob();
        CsvAnalysis analysis = createTestAnalysis(job);
        
        DimensionMappingRequest request = DimensionMappingRequest.builder()
                .columnIndex(0)
                .dimensionType(DimensionType.INDICATOR_NAME)
                .columnHeader("Indicator")
                .mappingRules(Map.of("pattern", ".*"))
                .build();
        
        // When/Then
        mockMvc.perform(post("/api/v1/uploads/{jobId}/dimension-mapping", job.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columnIndex").value(0))
                .andExpect(jsonPath("$.dimensionType").value("INDICATOR_NAME"))
                .andExpect(jsonPath("$.confidenceScore").value(1.0))
                .andExpect(jsonPath("$.isAutoDetected").value(false));
        
        // Verify mapping was saved
        assertThat(csvColumnMappingRepository.findByAnalysisIdAndColumnIndex(analysis.getId(), 0)).isPresent();
    }
    
    @Test
    void createColumnDimensionMapping_shouldUpdateExistingMapping() throws Exception {
        // Given
        UploadJob job = createTestJob();
        CsvAnalysis analysis = createTestAnalysis(job);
        
        // Create existing mapping
        CsvColumnMapping existingMapping = CsvColumnMapping.builder()
                .analysis(analysis)
                .columnIndex(0)
                .dimensionType(DimensionType.ADDITIONAL)
                .columnHeader("Old Header")
                .build();
        csvColumnMappingRepository.save(existingMapping);
        
        DimensionMappingRequest request = DimensionMappingRequest.builder()
                .columnIndex(0)
                .dimensionType(DimensionType.INDICATOR_NAME)
                .columnHeader("New Header")
                .mappingRules(Map.of("pattern", ".*"))
                .build();
        
        // When/Then
        mockMvc.perform(post("/api/v1/uploads/{jobId}/dimension-mapping", job.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dimensionType").value("INDICATOR_NAME"));
        
        // Verify mapping was updated
        CsvColumnMapping updatedMapping = csvColumnMappingRepository.findByAnalysisIdAndColumnIndex(analysis.getId(), 0).orElseThrow();
        assertThat(updatedMapping.getDimensionType()).isEqualTo(DimensionType.INDICATOR_NAME);
    }
    
    @Test
    void createColumnDimensionMapping_shouldReturnBadRequestForInvalidJob() throws Exception {
        // Given
        DimensionMappingRequest request = DimensionMappingRequest.builder()
                .columnIndex(0)
                .dimensionType(DimensionType.INDICATOR_NAME)
                .build();
        
        // When/Then
        mockMvc.perform(post("/api/v1/uploads/999/dimension-mapping")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void getDimensionSuggestions_shouldReturnSuggestions() throws Exception {
        // Given
        UploadJob job = createTestJob();
        CsvAnalysis analysis = createTestAnalysis(job);
        
        // Create test CSV file
        Path jobDir = Paths.get("uploads", job.getId().toString());
        Files.createDirectories(jobDir);
        Path csvFile = jobDir.resolve("test.csv");
        Files.write(csvFile, "Indicator,2020,2021,2022\nGDP,100,110,120\nPopulation,1000,1010,1020".getBytes());
        
        // Update analysis with correct file path
        analysis.setFilePath(csvFile.toString());
        csvAnalysisRepository.save(analysis);
        
        // When/Then
        mockMvc.perform(get("/api/v1/uploads/{jobId}/dimension-suggestions", job.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].columnIndex").exists())
                .andExpect(jsonPath("$[0].dimensionType").exists())
                .andExpect(jsonPath("$[0].confidenceScore").exists());
    }
    
    @Test
    void getDimensionSuggestions_shouldReturnBadRequestForInvalidJob() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/v1/uploads/999/dimension-suggestions"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void validateDimensionMappings_shouldReturnValidWhenRequiredDimensionsPresent() throws Exception {
        // Given
        UploadJob job = createTestJob();
        CsvAnalysis analysis = createTestAnalysis(job);
        
        // Create required mappings
        CsvColumnMapping indicatorNameMapping = CsvColumnMapping.builder()
                .analysis(analysis)
                .columnIndex(0)
                .dimensionType(DimensionType.INDICATOR_NAME)
                .columnHeader("Indicator")
                .build();
        
        CsvColumnMapping indicatorValueMapping = CsvColumnMapping.builder()
                .analysis(analysis)
                .columnIndex(1)
                .dimensionType(DimensionType.INDICATOR_VALUE)
                .columnHeader("Value")
                .build();
        
        csvColumnMappingRepository.save(indicatorNameMapping);
        csvColumnMappingRepository.save(indicatorValueMapping);
        
        // When/Then
        mockMvc.perform(post("/api/v1/uploads/{jobId}/validate-mappings", job.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isValid").value(true))
                .andExpect(jsonPath("$.errors").isEmpty())
                .andExpect(jsonPath("$.totalMappings").value(2))
                .andExpect(jsonPath("$.requiredMappings").value(2))
                .andExpect(jsonPath("$.missingMappings").value(0));
    }
    
    @Test
    void validateDimensionMappings_shouldReturnInvalidWhenMissingRequiredDimensions() throws Exception {
        // Given
        UploadJob job = createTestJob();
        CsvAnalysis analysis = createTestAnalysis(job);
        
        // Create only one required mapping
        CsvColumnMapping indicatorNameMapping = CsvColumnMapping.builder()
                .analysis(analysis)
                .columnIndex(0)
                .dimensionType(DimensionType.INDICATOR_NAME)
                .columnHeader("Indicator")
                .build();
        
        csvColumnMappingRepository.save(indicatorNameMapping);
        
        // When/Then
        mockMvc.perform(post("/api/v1/uploads/{jobId}/validate-mappings", job.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isValid").value(false))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").value(org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.missingMappings").value(1));
    }
    
    @Test
    void validateDimensionMappings_shouldReturnBadRequestForInvalidJob() throws Exception {
        // When/Then
        mockMvc.perform(post("/api/v1/uploads/999/validate-mappings"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void getMultiDimensionalAnalysis_shouldReturnAnalysis() throws Exception {
        // Given
        UploadJob job = createTestJob();
        CsvAnalysis analysis = createTestAnalysis(job);
        
        // Create test CSV file
        Path jobDir = Paths.get("uploads", job.getId().toString());
        Files.createDirectories(jobDir);
        Path csvFile = jobDir.resolve("test.csv");
        Files.write(csvFile, "Indicator,2020,2021,2022\nGDP,100,110,120\nPopulation,1000,1010,1020".getBytes());
        
        // Update analysis with correct file path
        analysis.setFilePath(csvFile.toString());
        csvAnalysisRepository.save(analysis);
        
        // Create mappings
        CsvColumnMapping indicatorNameMapping = CsvColumnMapping.builder()
                .analysis(analysis)
                .columnIndex(0)
                .dimensionType(DimensionType.INDICATOR_NAME)
                .columnHeader("Indicator")
                .build();
        
        CsvColumnMapping timeMapping = CsvColumnMapping.builder()
                .analysis(analysis)
                .columnIndex(1)
                .dimensionType(DimensionType.TIME)
                .columnHeader("2020")
                .build();
        
        CsvColumnMapping valueMapping = CsvColumnMapping.builder()
                .analysis(analysis)
                .columnIndex(2)
                .dimensionType(DimensionType.INDICATOR_VALUE)
                .columnHeader("2021")
                .build();
        
        csvColumnMappingRepository.save(indicatorNameMapping);
        csvColumnMappingRepository.save(timeMapping);
        csvColumnMappingRepository.save(valueMapping);
        
        // When/Then
        mockMvc.perform(get("/api/v1/uploads/{jobId}/multi-dimensional-analysis", job.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orientation").exists())
                .andExpect(jsonPath("$.totalDimensions").value(3))
                .andExpect(jsonPath("$.isComplete").exists());
    }
    
    @Test
    void getMultiDimensionalAnalysis_shouldReturnBadRequestForInvalidJob() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/v1/uploads/999/multi-dimensional-analysis"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void getDimensionTypes_shouldReturnAllTypes() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/v1/dimension-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").value(org.hamcrest.Matchers.hasSize(8)))
                .andExpect(jsonPath("$").value(org.hamcrest.Matchers.hasItems("TIME", "LOCATION", "INDICATOR_NAME", "INDICATOR_VALUE", "SOURCE", "UNIT", "GOAL", "ADDITIONAL")));
    }
    
    private UploadJob createTestJob() {
        UploadJob job = new UploadJob();
        job.setUserId("test-user");
        job.setStatus(UploadStatus.COMPLETED);
        job.setStartedAt(java.time.LocalDateTime.now());
        job.setFinishedAt(java.time.LocalDateTime.now());
        return uploadJobRepository.save(job);
    }
    
    private CsvAnalysis createTestAnalysis(UploadJob job) {
        CsvAnalysis analysis = CsvAnalysis.builder()
                .jobId(job.getId())
                .filename("test.csv")
                .rowCount(5L)
                .columnCount(4)
                .headers("[\"Indicator\",\"2020\",\"2021\",\"2022\"]")
                .delimiter(",")
                .hasHeader(true)
                .encoding("UTF-8")
                .filePath("uploads/" + job.getId() + "/test.csv")
                .build();
        return csvAnalysisRepository.save(analysis);
    }
} 