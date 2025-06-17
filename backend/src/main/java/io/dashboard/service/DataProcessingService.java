package io.dashboard.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dashboard.dto.*;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.*;
import io.dashboard.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataProcessingService {
    
    private final CsvAnalysisRepository csvAnalysisRepository;
    private final CsvColumnMappingRepository csvColumnMappingRepository;
    private final FactIndicatorValueRepository factIndicatorValueRepository;
    private final ProcessingJobRepository processingJobRepository;
    private final ProcessingErrorRepository processingErrorRepository;
    private final IndicatorRepository indicatorRepository;
    private final DimTimeRepository dimTimeRepository;
    private final DimLocationRepository dimLocationRepository;
    private final DimGenericRepository dimGenericRepository;
    private final UnitRepository unitRepository;
    private final ObjectMapper objectMapper;
    
    @Value("${data.processing.batch-size:1000}")
    private Integer defaultBatchSize;
    
    @Value("${data.processing.timeout-minutes:60}")
    private Integer processingTimeoutMinutes;
    
    @Value("${data.processing.max-errors:1000}")
    private Integer maxErrors;
    
    @Value("${data.processing.confidence-threshold:0.7}")
    private Double confidenceThreshold;
    
    @Async
    @Transactional
    public CompletableFuture<DataProcessingResponse> processUploadJob(Long uploadJobId) {
        ProcessingJob processingJob = null;
        
        try {
            // Create processing job
            processingJob = ProcessingJob.builder()
                    .uploadJobId(uploadJobId)
                    .status(ProcessingStatus.PENDING)
                    .recordsProcessed(0L)
                    .errorCount(0L)
                    .progressPercentage(0.0)
                    .batchSize(defaultBatchSize)
                    .build();
            
            processingJob = processingJobRepository.save(processingJob);
            
            // Start processing
            processingJob.setStatus(ProcessingStatus.RUNNING);
            processingJob.setStartedAt(LocalDateTime.now());
            processingJobRepository.save(processingJob);
            
            // Get CSV analysis
            CsvAnalysis analysis = csvAnalysisRepository.findByJobId(uploadJobId)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("CsvAnalysis", "jobId", uploadJobId));
            
            // Get dimension mappings
            List<CsvColumnMapping> mappings = csvColumnMappingRepository.findByAnalysisIdOrderByColumnIndex(analysis.getId());
            
            if (mappings.isEmpty()) {
                throw new BadRequestException("No dimension mappings found for analysis");
            }
            
            // Transform CSV to fact table
            List<FactIndicatorValue> factRecords = transformCsvToFactTable(analysis, mappings, processingJob);
            
            // Validate data quality
            DataQualityReport qualityReport = validateDataQuality(factRecords);
            
            // Save fact records
            saveFactRecords(factRecords, processingJob);
            
            // Calculate aggregations if enabled
            if (qualityReport.getQualityScore() > 0.8) {
                calculateAggregations(factRecords, processingJob);
            }
            
            // Update job status
            processingJob.setStatus(ProcessingStatus.COMPLETED);
            processingJob.setFinishedAt(LocalDateTime.now());
            processingJob.setProgressPercentage(100.0);
            processingJobRepository.save(processingJob);
            
            return CompletableFuture.completedFuture(DataProcessingResponse.builder()
                    .processingJobId(processingJob.getId())
                    .status("COMPLETED")
                    .message("Data processing completed successfully")
                    .startedAt(processingJob.getStartedAt())
                    .totalRecords(processingJob.getRecordsProcessed())
                    .batchSize(processingJob.getBatchSize())
                    .build());
            
        } catch (Exception e) {
            log.error("Error processing upload job {}: {}", uploadJobId, e.getMessage(), e);
            
            if (processingJob != null) {
                processingJob.setStatus(ProcessingStatus.FAILED);
                processingJob.setFinishedAt(LocalDateTime.now());
                processingJob.setErrorMessage(e.getMessage());
                processingJobRepository.save(processingJob);
            }
            
            return CompletableFuture.completedFuture(DataProcessingResponse.builder()
                    .processingJobId(processingJob != null ? processingJob.getId() : null)
                    .status("FAILED")
                    .message("Data processing failed: " + e.getMessage())
                    .build());
        }
    }
    
    @Transactional
    public List<FactIndicatorValue> transformCsvToFactTable(CsvAnalysis analysis, List<CsvColumnMapping> mappings, ProcessingJob processingJob) {
        List<FactIndicatorValue> factRecords = new ArrayList<>();
        List<List<String>> csvData = getCsvData(analysis);
        
        if (csvData.isEmpty()) {
            throw new BadRequestException("No CSV data found for analysis");
        }
        
        List<String> headers = csvData.get(0);
        List<List<String>> dataRows = csvData.subList(1, csvData.size());
        
        log.debug("CSV data loaded - headers: {}, data rows: {}", headers, dataRows.size());
        log.debug("Mappings: {}", mappings.stream().map(m -> m.getDimensionType() + ":" + m.getColumnIndex()).collect(Collectors.toList()));
        
        // Detect orientation
        String orientation = detectOrientation(mappings, csvData);
        log.debug("Detected orientation: {}", orientation);
        
        // Process data based on orientation
        if ("ROWS".equals(orientation)) {
            factRecords = processRowsOrientation(headers, dataRows, mappings, analysis, processingJob);
        } else {
            factRecords = processColumnsOrientation(headers, dataRows, mappings, analysis, processingJob);
        }
        
        log.debug("Generated {} fact records", factRecords.size());
        return factRecords;
    }
    
    private List<FactIndicatorValue> processRowsOrientation(List<String> headers, List<List<String>> dataRows, 
                                                           List<CsvColumnMapping> mappings, CsvAnalysis analysis, 
                                                           ProcessingJob processingJob) {
        List<FactIndicatorValue> factRecords = new ArrayList<>();
        
        // Find indicator name column
        CsvColumnMapping indicatorMapping = mappings.stream()
                .filter(m -> m.getDimensionType() == DimensionType.INDICATOR_NAME)
                .findFirst()
                .orElseThrow(() -> new BadRequestException("No indicator name mapping found"));
        
        // Find time columns
        List<CsvColumnMapping> timeMappings = mappings.stream()
                .filter(m -> m.getDimensionType() == DimensionType.TIME)
                .collect(Collectors.toList());
        
        // Find value columns
        List<CsvColumnMapping> valueMappings = mappings.stream()
                .filter(m -> m.getDimensionType() == DimensionType.INDICATOR_VALUE)
                .collect(Collectors.toList());
        
        for (int rowIndex = 0; rowIndex < dataRows.size(); rowIndex++) {
            List<String> row = dataRows.get(rowIndex);
            
            try {
                // Get indicator name
                String indicatorName = row.size() > indicatorMapping.getColumnIndex() ? 
                        row.get(indicatorMapping.getColumnIndex()) : "";
                
                if (indicatorName.trim().isEmpty()) {
                    continue;
                }
                
                // Find or create indicator
                Indicator indicator = findOrCreateIndicator(indicatorName);
                
                // Process each time column
                for (CsvColumnMapping timeMapping : timeMappings) {
                    if (timeMapping.getColumnIndex() < row.size()) {
                        String timeValue = row.get(timeMapping.getColumnIndex());
                        DimTime time = processTimeValues(timeValue, timeMapping.getDimensionType());
                        
                        // Find corresponding value
                        for (CsvColumnMapping valueMapping : valueMappings) {
                            if (valueMapping.getColumnIndex() < row.size()) {
                                String valueStr = row.get(valueMapping.getColumnIndex());
                                BigDecimal value = extractNumericValue(valueStr);
                                
                                if (value != null) {
                                    FactIndicatorValue factRecord = createFactRecord(
                                            indicator, time, null, null, value, null,
                                            analysis.getFilename(), rowIndex + 2, row
                                    );
                                    factRecords.add(factRecord);
                                }
                            }
                        }
                    }
                }
                
                // Update progress
                if (rowIndex % 100 == 0) {
                    updateProgress(processingJob, rowIndex, dataRows.size());
                }
                
            } catch (Exception e) {
                logError(processingJob, rowIndex + 2, "ROW_PROCESSING", e.getMessage(), String.join(",", row));
            }
        }
        
        return factRecords;
    }
    
    private List<FactIndicatorValue> processColumnsOrientation(List<String> headers, List<List<String>> dataRows,
                                                              List<CsvColumnMapping> mappings, CsvAnalysis analysis,
                                                              ProcessingJob processingJob) {
        List<FactIndicatorValue> factRecords = new ArrayList<>();
        
        // Find time column
        CsvColumnMapping timeMapping = mappings.stream()
                .filter(m -> m.getDimensionType() == DimensionType.TIME)
                .findFirst()
                .orElseThrow(() -> new BadRequestException("No time mapping found"));
        
        // Find indicator name columns
        List<CsvColumnMapping> indicatorMappings = mappings.stream()
                .filter(m -> m.getDimensionType() == DimensionType.INDICATOR_NAME)
                .collect(Collectors.toList());
        
        for (int rowIndex = 0; rowIndex < dataRows.size(); rowIndex++) {
            List<String> row = dataRows.get(rowIndex);
            
            try {
                // Get time value
                String timeValue = row.size() > timeMapping.getColumnIndex() ? 
                        row.get(timeMapping.getColumnIndex()) : "";
                
                if (timeValue.trim().isEmpty()) {
                    continue;
                }
                
                DimTime time = processTimeValues(timeValue, timeMapping.getDimensionType());
                
                // Process each indicator column
                for (CsvColumnMapping indicatorMapping : indicatorMappings) {
                    if (indicatorMapping.getColumnIndex() < row.size()) {
                        String indicatorName = headers.get(indicatorMapping.getColumnIndex());
                        String valueStr = row.get(indicatorMapping.getColumnIndex());
                        
                        if (!indicatorName.trim().isEmpty() && !valueStr.trim().isEmpty()) {
                            Indicator indicator = findOrCreateIndicator(indicatorName);
                            BigDecimal value = extractNumericValue(valueStr);
                            
                            if (value != null) {
                                FactIndicatorValue factRecord = createFactRecord(
                                        indicator, time, null, null, value, null,
                                        analysis.getFilename(), rowIndex + 2, row
                                );
                                factRecords.add(factRecord);
                            }
                        }
                    }
                }
                
                // Update progress
                if (rowIndex % 100 == 0) {
                    updateProgress(processingJob, rowIndex, dataRows.size());
                }
                
            } catch (Exception e) {
                logError(processingJob, rowIndex + 2, "ROW_PROCESSING", e.getMessage(), String.join(",", row));
            }
        }
        
        return factRecords;
    }
    
    @Transactional
    public DimTime processTimeValues(String value, DimensionType timeType) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        // Try to find existing time record
        Optional<DimTime> existingTime = dimTimeRepository.findByValue(value);
        if (existingTime.isPresent()) {
            return existingTime.get();
        }
        
        // Parse time value
        DimTime time = parseTimeValue(value, timeType);
        if (time != null) {
            return dimTimeRepository.save(time);
        }
        
        // Create generic time record
        DimTime genericTime = DimTime.builder()
                .value(value)
                .timeType(timeType)
                .year(0)
                .month(0)
                .day(0)
                .build();
        
        return dimTimeRepository.save(genericTime);
    }
    
    @Transactional
    public DimLocation processLocationValues(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        // Try to find existing location
        Optional<DimLocation> existingLocation = dimLocationRepository.findByValue(value);
        if (existingLocation.isPresent()) {
            return existingLocation.get();
        }
        
        // Create new location
        DimLocation location = DimLocation.builder()
                .value(value)
                .type(DimLocation.LocationType.COUNTRY)
                .build();
        
        return dimLocationRepository.save(location);
    }
    
    @Transactional
    public DimGeneric processGenericValues(String dimensionName, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        // Try to find existing generic dimension
        Optional<DimGeneric> existingGeneric = dimGenericRepository.findByDimensionNameAndValue(dimensionName, value);
        if (existingGeneric.isPresent()) {
            return existingGeneric.get();
        }
        
        // Create new generic dimension
        DimGeneric generic = DimGeneric.builder()
                .dimensionName(dimensionName)
                .value(value)
                .build();
        
        return dimGenericRepository.save(generic);
    }
    
    @Transactional
    public DataQualityReport validateDataQuality(List<FactIndicatorValue> records) {
        long totalRecords = records.size();
        long validRecords = 0;
        long errorRecords = 0;
        long warningRecords = 0;
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Map<String, Long> errorTypeCounts = new HashMap<>();
        
        for (FactIndicatorValue record : records) {
            boolean isValid = true;
            List<String> recordErrors = new ArrayList<>();
            
            // Check for null values
            if (record.getValue() == null) {
                recordErrors.add("Null value");
                errorTypeCounts.merge("NULL_VALUE", 1L, Long::sum);
                isValid = false;
            }
            
            // Check for negative values (if not allowed)
            if (record.getValue() != null && record.getValue().compareTo(BigDecimal.ZERO) < 0) {
                recordErrors.add("Negative value");
                errorTypeCounts.merge("NEGATIVE_VALUE", 1L, Long::sum);
                warnings.add("Negative value detected: " + record.getValue());
                warningRecords++;
            }
            
            // Check for extreme values
            if (record.getValue() != null && record.getValue().compareTo(new BigDecimal("999999999")) > 0) {
                recordErrors.add("Extreme value");
                errorTypeCounts.merge("EXTREME_VALUE", 1L, Long::sum);
                warnings.add("Extreme value detected: " + record.getValue());
                warningRecords++;
            }
            
            // Check for missing indicator
            if (record.getIndicator() == null) {
                recordErrors.add("Missing indicator");
                errorTypeCounts.merge("MISSING_INDICATOR", 1L, Long::sum);
                isValid = false;
            }
            
            if (isValid) {
                validRecords++;
            } else {
                errorRecords++;
                errors.addAll(recordErrors);
            }
        }
        
        double qualityScore = totalRecords > 0 ? (double) validRecords / totalRecords : 0.0;
        
        return DataQualityReport.builder()
                .totalRecords(totalRecords)
                .validRecords(validRecords)
                .errorRecords(errorRecords)
                .warningRecords(warningRecords)
                .qualityScore(qualityScore)
                .errors(errors)
                .warnings(warnings)
                .errorTypeCounts(errorTypeCounts)
                .build();
    }
    
    @Transactional
    public void calculateAggregations(List<FactIndicatorValue> records, ProcessingJob processingJob) {
        // Group by indicator
        Map<Long, List<FactIndicatorValue>> recordsByIndicator = records.stream()
                .collect(Collectors.groupingBy(r -> r.getIndicator().getId()));
        
        for (Map.Entry<Long, List<FactIndicatorValue>> entry : recordsByIndicator.entrySet()) {
            Long indicatorId = entry.getKey();
            List<FactIndicatorValue> indicatorRecords = entry.getValue();
            
            // Calculate sum
            BigDecimal sum = indicatorRecords.stream()
                    .map(FactIndicatorValue::getValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Calculate average
            BigDecimal average = sum.divide(BigDecimal.valueOf(indicatorRecords.size()), 6, BigDecimal.ROUND_HALF_UP);
            
            // Create aggregated record
            FactIndicatorValue aggregatedRecord = FactIndicatorValue.builder()
                    .indicator(indicatorRecords.get(0).getIndicator())
                    .value(average)
                    .isAggregated(true)
                    .sourceRowHash("AGGREGATED_" + indicatorId + "_" + System.currentTimeMillis())
                    .sourceFile("AGGREGATED")
                    .confidenceScore(1.0)
                    .build();
            
            factIndicatorValueRepository.save(aggregatedRecord);
        }
    }
    
    @Transactional
    public List<FactIndicatorValue> handleDataConflicts(List<FactIndicatorValue> duplicates) {
        List<FactIndicatorValue> resolvedRecords = new ArrayList<>();
        
        // Group by source row hash
        Map<String, List<FactIndicatorValue>> duplicatesByHash = duplicates.stream()
                .collect(Collectors.groupingBy(FactIndicatorValue::getSourceRowHash));
        
        for (List<FactIndicatorValue> duplicateGroup : duplicatesByHash.values()) {
            if (duplicateGroup.size() == 1) {
                resolvedRecords.add(duplicateGroup.get(0));
            } else {
                // Choose the record with highest confidence score
                FactIndicatorValue bestRecord = duplicateGroup.stream()
                        .max(Comparator.comparing(r -> r.getConfidenceScore() != null ? r.getConfidenceScore() : 0.0))
                        .orElse(duplicateGroup.get(0));
                
                resolvedRecords.add(bestRecord);
            }
        }
        
        return resolvedRecords;
    }
    
    // Helper methods
    private List<List<String>> getCsvData(CsvAnalysis analysis) {
        try {
            Path filePath = Paths.get(analysis.getFilePath());
            if (!Files.exists(filePath)) {
                throw new BadRequestException("CSV file not found: " + analysis.getFilePath());
            }
            
            Charset charset = Charset.forName(analysis.getEncoding() != null ? analysis.getEncoding() : "UTF-8");
            return Files.readAllLines(filePath, charset)
                    .stream()
                    .map(line -> Arrays.asList(line.split(analysis.getDelimiter() != null ? analysis.getDelimiter() : ",")))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new BadRequestException("Error reading CSV file: " + e.getMessage());
        }
    }
    
    private String detectOrientation(List<CsvColumnMapping> mappings, List<List<String>> csvData) {
        long indicatorNameCount = mappings.stream()
                .filter(m -> m.getDimensionType() == DimensionType.INDICATOR_NAME)
                .count();
        
        long timeCount = mappings.stream()
                .filter(m -> m.getDimensionType() == DimensionType.TIME)
                .count();
        
        long valueCount = mappings.stream()
                .filter(m -> m.getDimensionType() == DimensionType.INDICATOR_VALUE)
                .count();
        
        log.debug("Orientation detection - indicator mappings: {}, time mappings: {}, value mappings: {}", 
                 indicatorNameCount, timeCount, valueCount);
        
        // If we have more indicator name mappings than time mappings, it's likely COLUMNS orientation
        // (indicators are in column headers, time is in rows)
        // If we have more time mappings than indicator name mappings, it's likely ROWS orientation
        // (time is in column headers, indicators are in rows)
        String orientation = indicatorNameCount > timeCount ? "COLUMNS" : "ROWS";
        log.debug("Detected orientation: {} (indicator count: {}, time count: {})", orientation, indicatorNameCount, timeCount);
        
        return orientation;
    }
    
    private Indicator findOrCreateIndicator(String name) {
        return indicatorRepository.findByName(name)
                .orElseGet(() -> {
                    Indicator indicator = Indicator.builder()
                            .name(name)
                            .description("Auto-generated from data processing")
                            .build();
                    return indicatorRepository.save(indicator);
                });
    }
    
    private DimTime parseTimeValue(String value, DimensionType timeType) {
        // Try various date formats
        String[] dateFormats = {
            "yyyy", "yyyy-MM", "yyyy-MM-dd", "MM/dd/yyyy", "dd/MM/yyyy",
            "yyyy-MM-dd HH:mm:ss", "MM/dd/yyyy HH:mm:ss"
        };
        
        for (String format : dateFormats) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                LocalDateTime dateTime = LocalDateTime.parse(value, formatter);
                
                return DimTime.builder()
                        .value(value)
                        .timeType(timeType)
                        .year(dateTime.getYear())
                        .month(dateTime.getMonthValue())
                        .day(dateTime.getDayOfMonth())
                        .build();
            } catch (DateTimeParseException e) {
                // Continue to next format
            }
        }
        
        // Try to extract year from string
        Pattern yearPattern = Pattern.compile("\\b(19|20)\\d{2}\\b");
        java.util.regex.Matcher matcher = yearPattern.matcher(value);
        if (matcher.find()) {
            int year = Integer.parseInt(matcher.group());
            return DimTime.builder()
                    .value(value)
                    .timeType(timeType)
                    .year(year)
                    .month(0)
                    .day(0)
                    .build();
        }
        
        return null;
    }
    
    private BigDecimal extractNumericValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        // Remove currency symbols, commas, and other formatting
        String cleaned = value.replaceAll("[$,€£¥%\\s]", "").trim();
        
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private FactIndicatorValue createFactRecord(Indicator indicator, DimTime time, DimLocation location, 
                                               DimGeneric generic, BigDecimal value, Unit unit,
                                               String sourceFile, Integer rowNumber, List<String> row) {
        String sourceRowHash = generateSourceRowHash(row);
        
        return FactIndicatorValue.builder()
                .indicator(indicator)
                .time(time)
                .location(location)
                .generic(generic)
                .value(value)
                .unit(unit)
                .sourceRowHash(sourceRowHash)
                .sourceFile(sourceFile)
                .sourceRowNumber(rowNumber)
                .confidenceScore(1.0)
                .isAggregated(false)
                .build();
    }
    
    private String generateSourceRowHash(List<String> row) {
        try {
            String rowString = String.join("|", row);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rowString.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            return UUID.randomUUID().toString();
        }
    }
    
    private void updateProgress(ProcessingJob processingJob, int currentRow, int totalRows) {
        if (processingJob != null) {
            double progress = (double) currentRow / totalRows * 100.0;
            processingJob.setProgressPercentage(progress);
            processingJob.setRecordsProcessed((long) currentRow);
            processingJobRepository.save(processingJob);
        }
    }
    
    private void logError(ProcessingJob processingJob, Integer rowNumber, String errorType, 
                         String errorMessage, String rawValue) {
        if (processingJob != null) {
            ProcessingError error = ProcessingError.builder()
                    .processingJob(processingJob)
                    .rowNumber(rowNumber)
                    .errorType(errorType)
                    .errorMessage(errorMessage)
                    .rawValue(rawValue)
                    .severity("ERROR")
                    .isResolved(false)
                    .build();
            
            processingErrorRepository.save(error);
            
            // Update job error count
            processingJob.setErrorCount(processingJob.getErrorCount() + 1);
            processingJobRepository.save(processingJob);
        }
    }
    
    private void saveFactRecords(List<FactIndicatorValue> records, ProcessingJob processingJob) {
        // Handle duplicates
        List<FactIndicatorValue> uniqueRecords = handleDataConflicts(records);
        
        // Save in batches
        int batchSize = processingJob.getBatchSize() != null ? processingJob.getBatchSize() : defaultBatchSize;
        
        for (int i = 0; i < uniqueRecords.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, uniqueRecords.size());
            List<FactIndicatorValue> batch = uniqueRecords.subList(i, endIndex);
            
            factIndicatorValueRepository.saveAll(batch);
            
            // Update progress
            processingJob.setRecordsProcessed((long) endIndex);
            processingJob.setProgressPercentage((double) endIndex / uniqueRecords.size() * 100.0);
            processingJobRepository.save(processingJob);
        }
    }
} 