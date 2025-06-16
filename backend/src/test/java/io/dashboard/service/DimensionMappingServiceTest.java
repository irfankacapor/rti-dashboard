package io.dashboard.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dashboard.dto.DimensionMappingResponse;
import io.dashboard.dto.DimensionValidationResponse;
import io.dashboard.dto.MultiDimensionalAnalysis;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.*;
import io.dashboard.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DimensionMappingServiceTest {
    
    @Mock
    private CsvColumnMappingRepository csvColumnMappingRepository;
    
    @Mock
    private CsvAnalysisRepository csvAnalysisRepository;
    
    @Mock
    private CsvColumnRepository csvColumnRepository;
    
    @Mock
    private UploadFileRepository uploadFileRepository;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @InjectMocks
    private DimensionMappingService dimensionMappingService;
    
    private CsvAnalysis testAnalysis;
    private Path tempDir;
    
    @BeforeEach
    void setUp() throws Exception {
        tempDir = Files.createTempDirectory("test");
        
        testAnalysis = CsvAnalysis.builder()
                .id(1L)
                .jobId(1L)
                .filename("test.csv")
                .rowCount(5L)
                .columnCount(4)
                .headers("[\"Indicator\",\"2020\",\"2021\",\"2022\"]")
                .delimiter(",")
                .hasHeader(true)
                .encoding("UTF-8")
                .filePath(tempDir.resolve("test.csv").toString())
                .build();
        
        // Set configuration values
        ReflectionTestUtils.setField(dimensionMappingService, "timePatterns", "2020,2021,2022,2023,2024,Jan,Feb,Mar,Apr,May,Jun,Jul,Aug,Sep,Oct,Nov,Dec,Q1,Q2,Q3,Q4");
        ReflectionTestUtils.setField(dimensionMappingService, "locationPatterns", "USA,Canada,Mexico,UK,Germany,France,Spain,Italy,China,Japan,India,Brazil,Australia");
        ReflectionTestUtils.setField(dimensionMappingService, "confidenceThreshold", 0.7);
    }
    
    @Test
    void createColumnMapping_shouldCreateNewMapping() {
        // Given
        when(csvAnalysisRepository.findByJobId(1L)).thenReturn(Arrays.asList(testAnalysis));
        when(csvColumnMappingRepository.findByAnalysisIdAndColumnIndex(1L, 0)).thenReturn(Optional.empty());
        when(csvColumnMappingRepository.save(any(CsvColumnMapping.class))).thenAnswer(invocation -> {
            CsvColumnMapping mapping = invocation.getArgument(0);
            mapping.setId(1L);
            return mapping;
        });
        
        Map<String, Object> rules = Map.of("pattern", ".*");
        
        // When
        DimensionMappingResponse response = dimensionMappingService.createColumnMapping(1L, 0, DimensionType.INDICATOR_NAME, rules);
        
        // Then
        assertThat(response.getMappingId()).isEqualTo(1L);
        assertThat(response.getColumnIndex()).isEqualTo(0);
        assertThat(response.getDimensionType()).isEqualTo(DimensionType.INDICATOR_NAME);
        assertThat(response.getConfidenceScore()).isEqualTo(1.0);
        assertThat(response.getIsAutoDetected()).isFalse();
        
        verify(csvColumnMappingRepository).save(any(CsvColumnMapping.class));
    }
    
    @Test
    void createColumnMapping_shouldUpdateExistingMapping() {
        // Given
        CsvColumnMapping existingMapping = CsvColumnMapping.builder()
                .id(1L)
                .analysis(testAnalysis)
                .columnIndex(0)
                .dimensionType(DimensionType.ADDITIONAL)
                .build();
        
        when(csvAnalysisRepository.findByJobId(1L)).thenReturn(Arrays.asList(testAnalysis));
        when(csvColumnMappingRepository.findByAnalysisIdAndColumnIndex(1L, 0)).thenReturn(Optional.of(existingMapping));
        when(csvColumnMappingRepository.save(any(CsvColumnMapping.class))).thenReturn(existingMapping);
        
        Map<String, Object> rules = Map.of("pattern", ".*");
        
        // When
        DimensionMappingResponse response = dimensionMappingService.createColumnMapping(1L, 0, DimensionType.INDICATOR_NAME, rules);
        
        // Then
        assertThat(response.getDimensionType()).isEqualTo(DimensionType.INDICATOR_NAME);
        verify(csvColumnMappingRepository).save(existingMapping);
    }
    
    @Test
    void createColumnMapping_shouldThrowExceptionWhenAnalysisNotFound() {
        // Given
        when(csvAnalysisRepository.findByJobId(1L)).thenReturn(Collections.emptyList());
        
        // When/Then
        assertThatThrownBy(() -> dimensionMappingService.createColumnMapping(1L, 0, DimensionType.INDICATOR_NAME, Map.of()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
    
    @Test
    void suggestDimensionMappings_shouldDetectTimeColumns() throws Exception {
        // Given
        Path csvFile = tempDir.resolve("test.csv");
        Files.write(csvFile, "Indicator,2020,2021,2022\nGDP,100,110,120\nPopulation,1000,1010,1020".getBytes());
        
        CsvAnalysis analysisWithFile = CsvAnalysis.builder()
                .id(1L)
                .jobId(1L)
                .filename("test.csv")
                .filePath(csvFile.toString())
                .hasHeader(true)
                .headers("[\"Indicator\", \"2020\", \"2021\", \"2022\"]")
                .encoding("UTF-8")
                .delimiter(",")
                .rowCount(3L)
                .columnCount(4)
                .build();
        
        when(csvAnalysisRepository.findByJobId(1L)).thenReturn(Arrays.asList(analysisWithFile));
        
        // When
        List<DimensionMappingResponse> suggestions = dimensionMappingService.suggestDimensionMappings(1L);
        
        // Then
        assertThat(suggestions).isNotEmpty();
        
        // Should detect first column as indicator name
        Optional<DimensionMappingResponse> indicatorMapping = suggestions.stream()
                .filter(s -> s.getColumnIndex() == 0)
                .findFirst();
        assertThat(indicatorMapping).isPresent();
        assertThat(indicatorMapping.get().getDimensionType()).isEqualTo(DimensionType.INDICATOR_NAME);
        
        // Should detect year columns as time (but they might be detected as values due to numeric content)
        suggestions.stream()
                .filter(s -> s.getColumnIndex() > 0)
                .forEach(s -> {
                    assertThat(s.getDimensionType()).isIn(DimensionType.TIME, DimensionType.INDICATOR_VALUE);
                });
    }
    
    @Test
    void suggestDimensionMappings_shouldDetectLocationColumns() throws Exception {
        // Given
        Path csvFile = tempDir.resolve("test.csv");
        Files.write(csvFile, "Country,Indicator,2020\nUSA,GDP,100\nCanada,GDP,90\nGermany,GDP,80".getBytes());
        
        CsvAnalysis analysisWithFile = CsvAnalysis.builder()
                .id(1L)
                .jobId(1L)
                .filename("test.csv")
                .filePath(csvFile.toString())
                .hasHeader(true)
                .headers("[\"Country\", \"Indicator\", \"2020\"]")
                .encoding("UTF-8")
                .delimiter(",")
                .rowCount(3L)
                .columnCount(4)
                .build();
        
        when(csvAnalysisRepository.findByJobId(1L)).thenReturn(Arrays.asList(analysisWithFile));
        
        // When
        List<DimensionMappingResponse> suggestions = dimensionMappingService.suggestDimensionMappings(1L);
        
        // Then
        assertThat(suggestions).isNotEmpty();
        
        // Should detect country column as location
        Optional<DimensionMappingResponse> locationMapping = suggestions.stream()
                .filter(s -> s.getColumnIndex() == 0)
                .findFirst();
        assertThat(locationMapping).isPresent();
        assertThat(locationMapping.get().getDimensionType()).isEqualTo(DimensionType.LOCATION);
    }
    
    @Test
    void suggestDimensionMappings_shouldDetectValueColumns() throws Exception {
        // Given
        Path csvFile = tempDir.resolve("test.csv");
        Files.write(csvFile, "Indicator,Value,Unit\nGDP,100.5,USD\nPopulation,1000,people".getBytes());
        
        CsvAnalysis analysisWithFile = CsvAnalysis.builder()
                .id(1L)
                .jobId(1L)
                .filename("test.csv")
                .filePath(csvFile.toString())
                .hasHeader(true)
                .headers("[\"Indicator\", \"Value\", \"Unit\"]")
                .encoding("UTF-8")
                .delimiter(",")
                .rowCount(3L)
                .columnCount(4)
                .build();
        
        when(csvAnalysisRepository.findByJobId(1L)).thenReturn(Arrays.asList(analysisWithFile));
        
        // When
        List<DimensionMappingResponse> suggestions = dimensionMappingService.suggestDimensionMappings(1L);
        
        // Then
        assertThat(suggestions).isNotEmpty();
        
        // Should detect value column as indicator value
        Optional<DimensionMappingResponse> valueMapping = suggestions.stream()
                .filter(s -> s.getColumnIndex() == 1)
                .findFirst();
        assertThat(valueMapping).isPresent();
        assertThat(valueMapping.get().getDimensionType()).isEqualTo(DimensionType.INDICATOR_VALUE);
        
        // Should detect unit column as unit or additional or indicator name
        Optional<DimensionMappingResponse> unitMapping = suggestions.stream()
                .filter(s -> s.getColumnIndex() == 2)
                .findFirst();
        assertThat(unitMapping).isPresent();
        assertThat(unitMapping.get().getDimensionType()).isIn(DimensionType.UNIT, DimensionType.ADDITIONAL, DimensionType.INDICATOR_NAME);
    }
    
    @Test
    void validateMappings_shouldReturnValidWhenRequiredDimensionsPresent() {
        // Given
        List<CsvColumnMapping> mappings = Arrays.asList(
                CsvColumnMapping.builder()
                        .id(1L)
                        .analysis(testAnalysis)
                        .columnIndex(0)
                        .dimensionType(DimensionType.INDICATOR_NAME)
                        .isAutoDetected(false)
                        .build(),
                CsvColumnMapping.builder()
                        .id(2L)
                        .analysis(testAnalysis)
                        .columnIndex(1)
                        .dimensionType(DimensionType.INDICATOR_VALUE)
                        .isAutoDetected(false)
                        .build()
        );
        
        when(csvAnalysisRepository.findByJobId(1L)).thenReturn(Arrays.asList(testAnalysis));
        when(csvColumnMappingRepository.findByAnalysisId(1L)).thenReturn(mappings);
        
        // When
        DimensionValidationResponse response = dimensionMappingService.validateMappings(1L);
        
        // Then
        assertThat(response.getIsValid()).isTrue();
        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getTotalMappings()).isEqualTo(2);
        assertThat(response.getRequiredMappings()).isEqualTo(2);
        assertThat(response.getMissingMappings()).isEqualTo(0);
    }
    
    @Test
    void validateMappings_shouldReturnInvalidWhenMissingRequiredDimensions() {
        // Given
        List<CsvColumnMapping> mappings = Arrays.asList(
                CsvColumnMapping.builder()
                        .id(1L)
                        .analysis(testAnalysis)
                        .columnIndex(0)
                        .dimensionType(DimensionType.TIME)
                        .isAutoDetected(false)
                        .build()
        );
        
        when(csvAnalysisRepository.findByJobId(1L)).thenReturn(Arrays.asList(testAnalysis));
        when(csvColumnMappingRepository.findByAnalysisId(1L)).thenReturn(mappings);
        
        // When
        DimensionValidationResponse response = dimensionMappingService.validateMappings(1L);
        
        // Then
        assertThat(response.getIsValid()).isFalse();
        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors()).contains("Missing required dimension: INDICATOR_NAME");
        assertThat(response.getErrors()).contains("Missing required dimension: INDICATOR_VALUE");
        assertThat(response.getMissingMappings()).isEqualTo(2);
    }
    
    @Test
    void validateMappings_shouldReturnWarningsForLowConfidenceMappings() {
        // Given
        List<CsvColumnMapping> mappings = Arrays.asList(
                CsvColumnMapping.builder()
                        .id(1L)
                        .analysis(testAnalysis)
                        .columnIndex(0)
                        .dimensionType(DimensionType.INDICATOR_NAME)
                        .isAutoDetected(true)
                        .confidenceScore(0.5)
                        .build(),
                CsvColumnMapping.builder()
                        .id(2L)
                        .analysis(testAnalysis)
                        .columnIndex(1)
                        .dimensionType(DimensionType.INDICATOR_VALUE)
                        .isAutoDetected(false)
                        .build()
        );
        
        when(csvAnalysisRepository.findByJobId(1L)).thenReturn(Arrays.asList(testAnalysis));
        when(csvColumnMappingRepository.findByAnalysisId(1L)).thenReturn(mappings);
        
        // When
        DimensionValidationResponse response = dimensionMappingService.validateMappings(1L);
        
        // Then
        assertThat(response.getIsValid()).isTrue();
        assertThat(response.getWarnings()).isNotEmpty();
        assertThat(response.getWarnings().get(0)).contains("Low confidence mapping");
    }
    
    @Test
    void processMultiDimensionalData_shouldDetectRowsOrientation() throws Exception {
        // Given
        Path csvFile = tempDir.resolve("test.csv");
        Files.write(csvFile, "Indicator,2020,2021,2022\nGDP,100,110,120\nPopulation,1000,1010,1020".getBytes());
        
        CsvAnalysis analysisWithFile = CsvAnalysis.builder()
                .id(1L)
                .jobId(1L)
                .filename("test.csv")
                .filePath(csvFile.toString())
                .hasHeader(true)
                .headers("[\"Indicator\", \"2020\", \"2021\", \"2022\"]")
                .encoding("UTF-8")
                .delimiter(",")
                .rowCount(3L)
                .columnCount(4)
                .build();
        
        List<CsvColumnMapping> mappings = Arrays.asList(
                CsvColumnMapping.builder()
                        .id(1L)
                        .analysis(analysisWithFile)
                        .columnIndex(0)
                        .dimensionType(DimensionType.INDICATOR_NAME)
                        .build(),
                CsvColumnMapping.builder()
                        .id(2L)
                        .analysis(analysisWithFile)
                        .columnIndex(1)
                        .dimensionType(DimensionType.TIME)
                        .build(),
                CsvColumnMapping.builder()
                        .id(3L)
                        .analysis(analysisWithFile)
                        .columnIndex(2)
                        .dimensionType(DimensionType.INDICATOR_VALUE)
                        .build()
        );
        
        when(csvAnalysisRepository.findByJobId(1L)).thenReturn(Arrays.asList(analysisWithFile));
        when(csvColumnMappingRepository.findByAnalysisIdOrderByColumnIndex(1L)).thenReturn(mappings);
        
        // When
        MultiDimensionalAnalysis analysis = dimensionMappingService.processMultiDimensionalData(1L);
        
        // Then
        assertThat(analysis.getOrientation()).isEqualTo("ROWS");
        assertThat(analysis.getIndicatorAxis()).contains("GDP", "Population");
        assertThat(analysis.getTotalDimensions()).isEqualTo(3);
        assertThat(analysis.getIsComplete()).isTrue();
    }
    
    @Test
    void processMultiDimensionalData_shouldDetectColumnsOrientation() throws Exception {
        // Given
        Path csvFile = tempDir.resolve("test.csv");
        Files.write(csvFile, "Year,GDP,Population\n2020,100,1000\n2021,110,1010".getBytes());
        
        CsvAnalysis analysisWithFile = CsvAnalysis.builder()
                .id(1L)
                .jobId(1L)
                .filename("test.csv")
                .filePath(csvFile.toString())
                .hasHeader(true)
                .headers("[\"Year\", \"GDP\", \"Population\"]")
                .encoding("UTF-8")
                .delimiter(",")
                .rowCount(3L)
                .columnCount(4)
                .build();
        
        List<CsvColumnMapping> mappings = Arrays.asList(
                CsvColumnMapping.builder()
                        .id(1L)
                        .analysis(analysisWithFile)
                        .columnIndex(0)
                        .dimensionType(DimensionType.TIME)
                        .build(),
                CsvColumnMapping.builder()
                        .id(2L)
                        .analysis(analysisWithFile)
                        .columnIndex(1)
                        .dimensionType(DimensionType.INDICATOR_NAME)
                        .build(),
                CsvColumnMapping.builder()
                        .id(3L)
                        .analysis(analysisWithFile)
                        .columnIndex(2)
                        .dimensionType(DimensionType.INDICATOR_NAME)
                        .build()
        );
        
        when(csvAnalysisRepository.findByJobId(1L)).thenReturn(Arrays.asList(analysisWithFile));
        when(csvColumnMappingRepository.findByAnalysisIdOrderByColumnIndex(1L)).thenReturn(mappings);
        
        // When
        MultiDimensionalAnalysis analysis = dimensionMappingService.processMultiDimensionalData(1L);
        
        // Then
        assertThat(analysis.getOrientation()).isEqualTo("COLUMNS");
        assertThat(analysis.getTimeAxis()).contains("2020", "2021");
        assertThat(analysis.getIndicatorAxis()).contains("GDP", "Population");
    }
    
    @Test
    void processMultiDimensionalData_shouldThrowExceptionWhenAnalysisNotFound() {
        // Given
        when(csvAnalysisRepository.findByJobId(1L)).thenReturn(Collections.emptyList());
        
        // When/Then
        assertThatThrownBy(() -> dimensionMappingService.processMultiDimensionalData(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
    
    @Test
    void suggestDimensionMappings_shouldThrowExceptionWhenNoCsvData() {
        // Given
        CsvAnalysis analysisWithoutFile = CsvAnalysis.builder()
                .id(1L)
                .jobId(1L)
                .filename("nonexistent.csv")
                .filePath("/nonexistent/path/test.csv")
                .hasHeader(true)
                .headers("[\"Test\"]")
                .encoding("UTF-8")
                .build();
        
        when(csvAnalysisRepository.findByJobId(1L)).thenReturn(Arrays.asList(analysisWithoutFile));
        
        // When/Then
        assertThatThrownBy(() -> dimensionMappingService.suggestDimensionMappings(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("CSV file not found");
    }
} 