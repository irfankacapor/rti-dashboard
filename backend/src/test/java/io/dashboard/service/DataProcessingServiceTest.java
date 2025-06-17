package io.dashboard.service;

import io.dashboard.dto.DataProcessingRequest;
import io.dashboard.dto.DataProcessingResponse;
import io.dashboard.dto.DataQualityReport;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.*;
import io.dashboard.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataProcessingServiceTest {
    
    @Mock
    private CsvAnalysisRepository csvAnalysisRepository;
    
    @Mock
    private CsvColumnMappingRepository csvColumnMappingRepository;
    
    @Mock
    private FactIndicatorValueRepository factIndicatorValueRepository;
    
    @Mock
    private ProcessingJobRepository processingJobRepository;
    
    @Mock
    private ProcessingErrorRepository processingErrorRepository;
    
    @Mock
    private IndicatorRepository indicatorRepository;
    
    @Mock
    private DimTimeRepository dimTimeRepository;
    
    @Mock
    private DimLocationRepository dimLocationRepository;
    
    @Mock
    private DimGenericRepository dimGenericRepository;
    
    @Mock
    private UnitRepository unitRepository;
    
    @InjectMocks
    private DataProcessingService dataProcessingService;
    
    @TempDir
    Path tempDir;
    
    private CsvAnalysis testAnalysis;
    private List<CsvColumnMapping> testMappings;
    private ProcessingJob testProcessingJob;
    
    @BeforeEach
    void setUp() {
        // Set default values
        ReflectionTestUtils.setField(dataProcessingService, "defaultBatchSize", 1000);
        ReflectionTestUtils.setField(dataProcessingService, "processingTimeoutMinutes", 60);
        ReflectionTestUtils.setField(dataProcessingService, "maxErrors", 1000);
        ReflectionTestUtils.setField(dataProcessingService, "confidenceThreshold", 0.7);
        
        // Create test analysis
        testAnalysis = CsvAnalysis.builder()
                .id(1L)
                .jobId(1L)
                .filename("test.csv")
                .filePath(tempDir.resolve("test.csv").toString())
                .hasHeader(true)
                .headers("[\"Indicator\", \"2020\", \"2021\", \"2022\"]")
                .encoding("UTF-8")
                .delimiter(",")
                .rowCount(4L)
                .columnCount(4)
                .build();
        
        // Create test mappings
        testMappings = Arrays.asList(
                CsvColumnMapping.builder()
                        .id(1L)
                        .analysis(testAnalysis)
                        .columnIndex(0)
                        .dimensionType(DimensionType.INDICATOR_NAME)
                        .build(),
                CsvColumnMapping.builder()
                        .id(2L)
                        .analysis(testAnalysis)
                        .columnIndex(1)
                        .dimensionType(DimensionType.TIME)
                        .build(),
                CsvColumnMapping.builder()
                        .id(3L)
                        .analysis(testAnalysis)
                        .columnIndex(2)
                        .dimensionType(DimensionType.INDICATOR_VALUE)
                        .build()
        );
        
        // Create test processing job
        testProcessingJob = ProcessingJob.builder()
                .id(1L)
                .uploadJobId(1L)
                .status(ProcessingStatus.PENDING)
                .recordsProcessed(0L)
                .errorCount(0L)
                .progressPercentage(0.0)
                .batchSize(1000)
                .build();
    }
    
    @Test
    void processUploadJob_shouldProcessSuccessfully() throws Exception {
        // Given
        Path csvFile = tempDir.resolve("test.csv");
        Files.write(csvFile, "Indicator,Time,Value\nGDP,2020,100\nPopulation,2020,1000".getBytes());
        
        // Update testAnalysis to use the correct file path
        testAnalysis.setFilePath(csvFile.toString());
        
        List<CsvColumnMapping> rowMappings = Arrays.asList(
                CsvColumnMapping.builder()
                        .id(1L)
                        .analysis(testAnalysis)
                        .columnIndex(0)
                        .dimensionType(DimensionType.INDICATOR_NAME)
                        .build(),
                CsvColumnMapping.builder()
                        .id(2L)
                        .analysis(testAnalysis)
                        .columnIndex(1)
                        .dimensionType(DimensionType.TIME)
                        .build(),
                CsvColumnMapping.builder()
                        .id(3L)
                        .analysis(testAnalysis)
                        .columnIndex(2)
                        .dimensionType(DimensionType.INDICATOR_VALUE)
                        .build()
        );
        
        when(csvAnalysisRepository.findByJobId(1L)).thenReturn(Arrays.asList(testAnalysis));
        when(csvColumnMappingRepository.findByAnalysisIdOrderByColumnIndex(1L)).thenReturn(rowMappings);
        when(processingJobRepository.save(any(ProcessingJob.class))).thenReturn(testProcessingJob);
        
        // Mock indicator repository with lenient stubbing
        lenient().when(indicatorRepository.findByName("GDP")).thenReturn(Optional.of(Indicator.builder().id(1L).name("GDP").build()));
        lenient().when(indicatorRepository.findByName("Population")).thenReturn(Optional.of(Indicator.builder().id(2L).name("Population").build()));
        lenient().when(indicatorRepository.save(any(Indicator.class))).thenAnswer(invocation -> {
            Indicator indicator = invocation.getArgument(0);
            if (indicator.getId() == null) {
                indicator.setId(3L);
            }
            return indicator;
        });
        
        // Mock time repository with lenient stubbing
        lenient().when(dimTimeRepository.findByValue("2020")).thenReturn(Optional.of(DimTime.builder().id(1L).value("2020").year(2020).build()));
        lenient().when(dimTimeRepository.save(any(DimTime.class))).thenAnswer(invocation -> {
            DimTime time = invocation.getArgument(0);
            if (time.getId() == null) {
                time.setId(2L);
            }
            return time;
        });
        
        // Mock fact repository
        doAnswer(invocation -> invocation.getArgument(0)).when(factIndicatorValueRepository).saveAll(any());
        
        // When
        CompletableFuture<DataProcessingResponse> future = dataProcessingService.processUploadJob(1L);
        DataProcessingResponse response = future.get();
        
        // Then
        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(response.getProcessingJobId()).isEqualTo(1L);
        assertThat(response.getMessage()).contains("successfully");
        
        verify(processingJobRepository, atLeastOnce()).save(any(ProcessingJob.class));
        verify(factIndicatorValueRepository, atLeastOnce()).saveAll(any());
    }
    
    @Test
    void processUploadJob_shouldHandleMissingAnalysis() throws Exception {
        // Given
        when(csvAnalysisRepository.findByJobId(1L)).thenReturn(Collections.emptyList());
        when(processingJobRepository.save(any(ProcessingJob.class))).thenReturn(testProcessingJob);
        
        // When
        CompletableFuture<DataProcessingResponse> future = dataProcessingService.processUploadJob(1L);
        DataProcessingResponse response = future.get();
        
        // Then
        assertThat(response.getStatus()).isEqualTo("FAILED");
        assertThat(response.getMessage()).contains("CsvAnalysis not found");
    }
    
    @Test
    void processUploadJob_shouldHandleMissingMappings() throws Exception {
        // Given
        when(csvAnalysisRepository.findByJobId(1L)).thenReturn(Arrays.asList(testAnalysis));
        when(csvColumnMappingRepository.findByAnalysisIdOrderByColumnIndex(1L)).thenReturn(Collections.emptyList());
        when(processingJobRepository.save(any(ProcessingJob.class))).thenReturn(testProcessingJob);
        
        // When
        CompletableFuture<DataProcessingResponse> future = dataProcessingService.processUploadJob(1L);
        DataProcessingResponse response = future.get();
        
        // Then
        assertThat(response.getStatus()).isEqualTo("FAILED");
        assertThat(response.getMessage()).contains("No dimension mappings found");
    }
    
    @Test
    void transformCsvToFactTable_shouldProcessRowsOrientation() throws Exception {
        // Given
        Path csvFile = tempDir.resolve("test.csv");
        // Simple format: indicator, time, value in columns
        Files.write(csvFile, "Indicator,Time,Value\nGDP,2020,100\nPopulation,2020,1000".getBytes());
        
        // Update testAnalysis to use the correct file path
        testAnalysis.setFilePath(csvFile.toString());
        
        List<CsvColumnMapping> rowMappings = Arrays.asList(
                CsvColumnMapping.builder()
                        .id(1L)
                        .analysis(testAnalysis)
                        .columnIndex(0)
                        .dimensionType(DimensionType.INDICATOR_NAME)
                        .build(),
                CsvColumnMapping.builder()
                        .id(2L)
                        .analysis(testAnalysis)
                        .columnIndex(1)
                        .dimensionType(DimensionType.TIME)
                        .build(),
                CsvColumnMapping.builder()
                        .id(3L)
                        .analysis(testAnalysis)
                        .columnIndex(2)
                        .dimensionType(DimensionType.INDICATOR_VALUE)
                        .build()
        );
        
        // Mock indicator repository with lenient stubbing
        lenient().when(indicatorRepository.findByName("GDP")).thenReturn(Optional.of(Indicator.builder().id(1L).name("GDP").build()));
        lenient().when(indicatorRepository.findByName("Population")).thenReturn(Optional.of(Indicator.builder().id(2L).name("Population").build()));
        lenient().when(indicatorRepository.save(any(Indicator.class))).thenAnswer(invocation -> {
            Indicator indicator = invocation.getArgument(0);
            if (indicator.getId() == null) {
                indicator.setId(3L);
            }
            return indicator;
        });
        
        // Mock time repository with lenient stubbing
        lenient().when(dimTimeRepository.findByValue("2020")).thenReturn(Optional.of(DimTime.builder().id(1L).value("2020").year(2020).build()));
        lenient().when(dimTimeRepository.save(any(DimTime.class))).thenAnswer(invocation -> {
            DimTime time = invocation.getArgument(0);
            if (time.getId() == null) {
                time.setId(2L);
            }
            return time;
        });
        
        // When
        List<FactIndicatorValue> result = dataProcessingService.transformCsvToFactTable(testAnalysis, rowMappings, testProcessingJob);
        
        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSizeGreaterThan(0);
        
        // Verify indicator creation
        verify(indicatorRepository, atLeastOnce()).findByName(anyString());
    }
    
    @Test
    void transformCsvToFactTable_shouldProcessColumnsOrientation() throws Exception {
        // Given
        Path csvFile = tempDir.resolve("test.csv");
        Files.write(csvFile, "Time,GDP,Population\n2020,100,1000\n2021,110,1010".getBytes());
        
        // Update testAnalysis to use the correct file path
        testAnalysis.setFilePath(csvFile.toString());
        
        List<CsvColumnMapping> columnMappings = Arrays.asList(
                CsvColumnMapping.builder()
                        .id(1L)
                        .analysis(testAnalysis)
                        .columnIndex(0)
                        .dimensionType(DimensionType.TIME)
                        .build(),
                CsvColumnMapping.builder()
                        .id(2L)
                        .analysis(testAnalysis)
                        .columnIndex(1)
                        .dimensionType(DimensionType.INDICATOR_NAME)
                        .build(),
                CsvColumnMapping.builder()
                        .id(3L)
                        .analysis(testAnalysis)
                        .columnIndex(2)
                        .dimensionType(DimensionType.INDICATOR_NAME)
                        .build()
        );
        
        // Mock indicator repository with lenient stubbing
        lenient().when(indicatorRepository.findByName("GDP")).thenReturn(Optional.of(Indicator.builder().id(1L).name("GDP").build()));
        lenient().when(indicatorRepository.findByName("Population")).thenReturn(Optional.of(Indicator.builder().id(2L).name("Population").build()));
        lenient().when(indicatorRepository.save(any(Indicator.class))).thenAnswer(invocation -> {
            Indicator indicator = invocation.getArgument(0);
            if (indicator.getId() == null) {
                indicator.setId(3L);
            }
            return indicator;
        });
        
        // Mock time repository with lenient stubbing
        lenient().when(dimTimeRepository.findByValue("2020")).thenReturn(Optional.of(DimTime.builder().id(1L).value("2020").year(2020).build()));
        lenient().when(dimTimeRepository.findByValue("2021")).thenReturn(Optional.of(DimTime.builder().id(2L).value("2021").year(2021).build()));
        lenient().when(dimTimeRepository.save(any(DimTime.class))).thenAnswer(invocation -> {
            DimTime time = invocation.getArgument(0);
            if (time.getId() == null) {
                time.setId(3L);
            }
            return time;
        });
        
        // When
        List<FactIndicatorValue> result = dataProcessingService.transformCsvToFactTable(testAnalysis, columnMappings, testProcessingJob);
        
        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSizeGreaterThan(0);
    }
    
    @Test
    void transformCsvToFactTable_shouldHandleEmptyCsvData() {
        // Given
        testAnalysis.setFilePath("/nonexistent/file.csv");
        
        // When/Then
        assertThatThrownBy(() -> dataProcessingService.transformCsvToFactTable(testAnalysis, testMappings, testProcessingJob))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("CSV file not found");
    }
    
    @Test
    void processTimeValues_shouldCreateNewTimeRecord() {
        // Given
        String timeValue = "2023";
        when(dimTimeRepository.findByValue(timeValue)).thenReturn(Optional.empty());
        when(dimTimeRepository.save(any(DimTime.class))).thenAnswer(invocation -> {
            DimTime time = invocation.getArgument(0);
            time.setId(1L);
            return time;
        });
        
        // When
        DimTime result = dataProcessingService.processTimeValues(timeValue, DimensionType.TIME);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getValue()).isEqualTo(timeValue);
        assertThat(result.getTimeType()).isEqualTo(DimensionType.TIME);
        assertThat(result.getYear()).isEqualTo(2023);
        
        verify(dimTimeRepository).findByValue(timeValue);
        verify(dimTimeRepository).save(any(DimTime.class));
    }
    
    @Test
    void processTimeValues_shouldReturnExistingTimeRecord() {
        // Given
        String timeValue = "2023";
        DimTime existingTime = DimTime.builder()
                .id(1L)
                .value(timeValue)
                .timeType(DimensionType.TIME)
                .year(2023)
                .build();
        
        when(dimTimeRepository.findByValue(timeValue)).thenReturn(Optional.of(existingTime));
        
        // When
        DimTime result = dataProcessingService.processTimeValues(timeValue, DimensionType.TIME);
        
        // Then
        assertThat(result).isEqualTo(existingTime);
        verify(dimTimeRepository).findByValue(timeValue);
        verify(dimTimeRepository, never()).save(any(DimTime.class));
    }
    
    @Test
    void processLocationValues_shouldCreateNewLocationRecord() {
        // Given
        String locationValue = "USA";
        when(dimLocationRepository.findByValue(locationValue)).thenReturn(Optional.empty());
        when(dimLocationRepository.save(any(DimLocation.class))).thenAnswer(invocation -> {
            DimLocation location = invocation.getArgument(0);
            location.setId(1L);
            return location;
        });
        
        // When
        DimLocation result = dataProcessingService.processLocationValues(locationValue);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getValue()).isEqualTo(locationValue);
        assertThat(result.getType()).isEqualTo(DimLocation.LocationType.COUNTRY);
        
        verify(dimLocationRepository).findByValue(locationValue);
        verify(dimLocationRepository).save(any(DimLocation.class));
    }
    
    @Test
    void processGenericValues_shouldCreateNewGenericRecord() {
        // Given
        String dimensionName = "Category";
        String value = "Economic";
        when(dimGenericRepository.findByDimensionNameAndValue(dimensionName, value)).thenReturn(Optional.empty());
        when(dimGenericRepository.save(any(DimGeneric.class))).thenAnswer(invocation -> {
            DimGeneric generic = invocation.getArgument(0);
            generic.setId(1L);
            return generic;
        });
        
        // When
        DimGeneric result = dataProcessingService.processGenericValues(dimensionName, value);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDimensionName()).isEqualTo(dimensionName);
        assertThat(result.getValue()).isEqualTo(value);
        
        verify(dimGenericRepository).findByDimensionNameAndValue(dimensionName, value);
        verify(dimGenericRepository).save(any(DimGeneric.class));
    }
    
    @Test
    void validateDataQuality_shouldReturnQualityReport() {
        // Given
        List<FactIndicatorValue> records = Arrays.asList(
                FactIndicatorValue.builder()
                        .indicator(Indicator.builder().id(1L).name("GDP").build())
                        .value(new BigDecimal("1000000"))
                        .build(),
                FactIndicatorValue.builder()
                        .indicator(Indicator.builder().id(2L).name("Population").build())
                        .value(new BigDecimal("100000"))
                        .build()
        );
        
        // When
        DataQualityReport result = dataProcessingService.validateDataQuality(records);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalRecords()).isEqualTo(2L);
        assertThat(result.getValidRecords()).isEqualTo(2L);
        assertThat(result.getErrorRecords()).isEqualTo(0L);
        assertThat(result.getQualityScore()).isEqualTo(1.0);
    }
    
    @Test
    void validateDataQuality_shouldDetectErrors() {
        // Given
        List<FactIndicatorValue> records = Arrays.asList(
                FactIndicatorValue.builder()
                        .indicator(Indicator.builder().id(1L).name("GDP").build())
                        .value(new BigDecimal("1000000"))
                        .build(),
                FactIndicatorValue.builder()
                        .indicator(null) // Missing indicator
                        .value(new BigDecimal("100000"))
                        .build(),
                FactIndicatorValue.builder()
                        .indicator(Indicator.builder().id(3L).name("Test").build())
                        .value(null) // Null value
                        .build()
        );
        
        // When
        DataQualityReport result = dataProcessingService.validateDataQuality(records);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalRecords()).isEqualTo(3L);
        assertThat(result.getValidRecords()).isEqualTo(1L);
        assertThat(result.getErrorRecords()).isEqualTo(2L);
        assertThat(result.getQualityScore()).isLessThan(1.0);
        assertThat(result.getErrors()).isNotEmpty();
    }
    
    @Test
    void calculateAggregations_shouldCreateAggregatedRecords() {
        // Given
        Indicator indicator = Indicator.builder().id(1L).name("GDP").build();
        List<FactIndicatorValue> records = Arrays.asList(
                FactIndicatorValue.builder()
                        .indicator(indicator)
                        .value(new BigDecimal("100"))
                        .build(),
                FactIndicatorValue.builder()
                        .indicator(indicator)
                        .value(new BigDecimal("200"))
                        .build(),
                FactIndicatorValue.builder()
                        .indicator(indicator)
                        .value(new BigDecimal("300"))
                        .build()
        );
        
        when(factIndicatorValueRepository.save(any(FactIndicatorValue.class))).thenAnswer(invocation -> {
            FactIndicatorValue record = invocation.getArgument(0);
            record.setId(1L);
            return record;
        });
        
        // When
        dataProcessingService.calculateAggregations(records, testProcessingJob);
        
        // Then
        verify(factIndicatorValueRepository).save(any(FactIndicatorValue.class));
    }
    
    @Test
    void handleDataConflicts_shouldResolveDuplicates() {
        // Given
        List<FactIndicatorValue> duplicates = Arrays.asList(
                FactIndicatorValue.builder()
                        .sourceRowHash("hash1")
                        .confidenceScore(0.8)
                        .build(),
                FactIndicatorValue.builder()
                        .sourceRowHash("hash1")
                        .confidenceScore(0.9)
                        .build(),
                FactIndicatorValue.builder()
                        .sourceRowHash("hash2")
                        .confidenceScore(0.7)
                        .build()
        );
        
        // When
        List<FactIndicatorValue> result = dataProcessingService.handleDataConflicts(duplicates);
        
        // Then
        assertThat(result).hasSize(2); // Should have 2 unique records
        assertThat(result.stream().map(FactIndicatorValue::getSourceRowHash).distinct()).hasSize(2);
    }
} 