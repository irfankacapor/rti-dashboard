package io.dashboard.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dashboard.dto.DimensionMappingRequest;
import io.dashboard.dto.DimensionMappingResponse;
import io.dashboard.dto.DimensionValidationResponse;
import io.dashboard.dto.MultiDimensionalAnalysis;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.*;
import io.dashboard.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DimensionMappingService {
    
    private final CsvColumnMappingRepository csvColumnMappingRepository;
    private final CsvAnalysisRepository csvAnalysisRepository;
    private final CsvColumnRepository csvColumnRepository;
    private final UploadFileRepository uploadFileRepository;
    private final ObjectMapper objectMapper;
    
    @Value("${app.dimension.time-patterns:2020,2021,2022,2023,2024,Jan,Feb,Mar,Apr,May,Jun,Jul,Aug,Sep,Oct,Nov,Dec,Q1,Q2,Q3,Q4}")
    private String timePatterns;
    
    @Value("${app.dimension.location-patterns:USA,Canada,Mexico,UK,Germany,France,Spain,Italy,China,Japan,India,Brazil,Australia}")
    private String locationPatterns;
    
    @Value("${app.dimension.confidence-threshold:0.7}")
    private Double confidenceThreshold;
    
    private static final Pattern YEAR_PATTERN = Pattern.compile("^\\d{4}$");
    private static final Pattern MONTH_PATTERN = Pattern.compile("^(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern QUARTER_PATTERN = Pattern.compile("^Q[1-4]$", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^-?\\d+(\\.\\d+)?$");
    private static final Pattern PERCENTAGE_PATTERN = Pattern.compile("^-?\\d+(\\.\\d+)?%$");
    private static final Pattern UNIT_PATTERN = Pattern.compile(".*(kg|%|index|million|billion|thousand|USD|EUR|GBP|CNY|JPY).*", Pattern.CASE_INSENSITIVE);
    
    @Transactional
    public DimensionMappingResponse createColumnMapping(Long jobId, Integer columnIndex, DimensionType type, Map<String, Object> rules) {
        CsvAnalysis analysis = findAnalysisByJobId(jobId);
        
        // Check if mapping already exists
        Optional<CsvColumnMapping> existingMapping = csvColumnMappingRepository.findByAnalysisIdAndColumnIndex(analysis.getId(), columnIndex);
        
        CsvColumnMapping mapping;
        if (existingMapping.isPresent()) {
            mapping = existingMapping.get();
            mapping.setDimensionType(type);
            mapping.setMappingRules(serializeRules(rules));
            mapping.setIsAutoDetected(false);
        } else {
            mapping = CsvColumnMapping.builder()
                    .analysis(analysis)
                    .columnIndex(columnIndex)
                    .dimensionType(type)
                    .mappingRules(serializeRules(rules))
                    .isAutoDetected(false)
                    .build();
        }
        
        CsvColumnMapping savedMapping = csvColumnMappingRepository.save(mapping);
        
        return DimensionMappingResponse.builder()
                .mappingId(savedMapping.getId())
                .columnIndex(savedMapping.getColumnIndex())
                .columnHeader(savedMapping.getColumnHeader())
                .dimensionType(savedMapping.getDimensionType())
                .confidenceScore(1.0)
                .isAutoDetected(false)
                .mappingRules(rules)
                .build();
    }
    
    @Transactional
    public List<DimensionMappingResponse> suggestDimensionMappings(Long jobId) {
        CsvAnalysis analysis = findAnalysisByJobId(jobId);
        
        // Get CSV data for analysis
        List<List<String>> csvData = getCsvData(analysis);
        if (csvData.isEmpty()) {
            throw new BadRequestException("No CSV data found for analysis");
        }
        
        List<String> headers = csvData.get(0);
        List<List<String>> sampleData = csvData.subList(1, Math.min(csvData.size(), 10)); // Use first 10 rows for analysis
        
        List<DimensionMappingResponse> suggestions = new ArrayList<>();
        
        for (int i = 0; i < headers.size(); i++) {
            final int columnIndex = i;
            String header = headers.get(i);
            List<String> columnData = sampleData.stream()
                    .map(row -> row.size() > columnIndex ? row.get(columnIndex) : "")
                    .collect(Collectors.toList());
            
            DimensionType suggestedType = detectDimensionType(header, columnData, columnIndex);
            Double confidence = calculateConfidence(header, columnData, suggestedType);
            
            if (confidence >= confidenceThreshold) {
                suggestions.add(DimensionMappingResponse.builder()
                        .columnIndex(columnIndex)
                        .columnHeader(header)
                        .dimensionType(suggestedType)
                        .confidenceScore(confidence)
                        .isAutoDetected(true)
                        .reason(generateReason(header, columnData, suggestedType))
                        .build());
            }
        }
        
        return suggestions;
    }
    
    @Transactional
    public DimensionValidationResponse validateMappings(Long jobId) {
        CsvAnalysis analysis = findAnalysisByJobId(jobId);
        List<CsvColumnMapping> mappings = csvColumnMappingRepository.findByAnalysisId(analysis.getId());
        
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        
        // Check for required dimensions
        boolean hasIndicatorName = mappings.stream()
                .anyMatch(m -> m.getDimensionType() == DimensionType.INDICATOR_NAME);
        boolean hasIndicatorValue = mappings.stream()
                .anyMatch(m -> m.getDimensionType() == DimensionType.INDICATOR_VALUE);
        
        if (!hasIndicatorName) {
            errors.add("Missing required dimension: INDICATOR_NAME");
            suggestions.add("Map a column containing indicator names/descriptions to INDICATOR_NAME dimension");
        }
        
        if (!hasIndicatorValue) {
            errors.add("Missing required dimension: INDICATOR_VALUE");
            suggestions.add("Map a column containing numeric values to INDICATOR_VALUE dimension");
        }
        
        // Check for duplicate mappings
        Map<DimensionType, Long> dimensionCounts = mappings.stream()
                .collect(Collectors.groupingBy(CsvColumnMapping::getDimensionType, Collectors.counting()));
        
        dimensionCounts.forEach((type, count) -> {
            if (count > 1 && type == DimensionType.INDICATOR_NAME) {
                warnings.add("Multiple INDICATOR_NAME mappings detected. Consider consolidating.");
            }
        });
        
        // Check for low confidence mappings
        mappings.stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsAutoDetected()) && m.getConfidenceScore() != null && m.getConfidenceScore() < confidenceThreshold)
                .forEach(m -> warnings.add("Low confidence mapping for column " + m.getColumnIndex() + ": " + m.getDimensionType()));
        
        boolean isValid = errors.isEmpty();
        
        return DimensionValidationResponse.builder()
                .isValid(isValid)
                .errors(errors)
                .warnings(warnings)
                .suggestions(suggestions)
                .totalMappings(mappings.size())
                .requiredMappings(2) // INDICATOR_NAME and INDICATOR_VALUE
                .missingMappings(isValid ? 0 : (hasIndicatorName ? 0 : 1) + (hasIndicatorValue ? 0 : 1))
                .build();
    }
    
    @Transactional
    public MultiDimensionalAnalysis processMultiDimensionalData(Long jobId) {
        CsvAnalysis analysis = findAnalysisByJobId(jobId);
        List<CsvColumnMapping> mappings = csvColumnMappingRepository.findByAnalysisIdOrderByColumnIndex(analysis.getId());
        List<List<String>> csvData = getCsvData(analysis);
        
        if (csvData.isEmpty()) {
            throw new BadRequestException("No CSV data found for analysis");
        }
        
        // Detect orientation (indicators in rows vs columns)
        String orientation = detectOrientation(mappings, csvData);
        
        // Extract axes based on mappings
        List<String> indicatorAxis = extractAxis(mappings, csvData, DimensionType.INDICATOR_NAME);
        List<String> timeAxis = extractAxis(mappings, csvData, DimensionType.TIME);
        List<String> locationAxis = extractAxis(mappings, csvData, DimensionType.LOCATION);
        List<String> additionalAxes = extractAdditionalAxes(mappings, csvData);
        
        // Calculate value coordinates
        Map<String, Object> valueCoordinates = calculateValueCoordinates(mappings, csvData, orientation);
        
        // Check completeness
        List<String> missingDimensions = identifyMissingDimensions(mappings);
        boolean isComplete = missingDimensions.isEmpty();
        
        return MultiDimensionalAnalysis.builder()
                .orientation(orientation)
                .indicatorAxis(indicatorAxis)
                .timeAxis(timeAxis)
                .locationAxis(locationAxis)
                .additionalAxes(additionalAxes)
                .valueCoordinates(valueCoordinates)
                .totalDimensions(mappings.size())
                .totalValues(calculateTotalValues(csvData, orientation))
                .isComplete(isComplete)
                .missingDimensions(missingDimensions)
                .build();
    }
    
    private DimensionType detectDimensionType(String header, List<String> columnData, int columnIndex) {
        // Time detection
        if (detectTimePattern(header, columnData)) {
            return DimensionType.TIME;
        }
        
        // Location detection
        if (detectLocationPattern(header, columnData)) {
            return DimensionType.LOCATION;
        }
        
        // Indicator name detection (usually first column or contains descriptive text)
        if (columnIndex == 0 || detectIndicatorNamePattern(header, columnData)) {
            return DimensionType.INDICATOR_NAME;
        }
        
        // Value detection (numeric data)
        if (detectValuePattern(header, columnData)) {
            return DimensionType.INDICATOR_VALUE;
        }
        
        // Unit detection
        if (detectUnitPattern(header, columnData)) {
            return DimensionType.UNIT;
        }
        
        // Source detection
        if (detectSourcePattern(header, columnData)) {
            return DimensionType.SOURCE;
        }
        
        // Default to additional
        return DimensionType.ADDITIONAL;
    }
    
    private boolean detectTimePattern(String header, List<String> columnData) {
        String headerLower = header.toLowerCase();
        if (headerLower.contains("year") || headerLower.contains("month") || headerLower.contains("date") || 
            headerLower.contains("time") || headerLower.contains("period")) {
            return true;
        }
        
        // Check data patterns
        long timeMatches = columnData.stream()
                .filter(data -> !data.isEmpty())
                .filter(data -> YEAR_PATTERN.matcher(data).matches() ||
                               MONTH_PATTERN.matcher(data).matches() ||
                               QUARTER_PATTERN.matcher(data).matches() ||
                               DATE_PATTERN.matcher(data).matches())
                .count();
        
        return timeMatches > columnData.size() * 0.5;
    }
    
    private boolean detectLocationPattern(String header, List<String> columnData) {
        String headerLower = header.toLowerCase();
        if (headerLower.contains("country") || headerLower.contains("state") || headerLower.contains("city") ||
            headerLower.contains("region") || headerLower.contains("location") || headerLower.contains("area")) {
            return true;
        }
        
        // Check against known location patterns
        Set<String> knownLocations = Set.of(locationPatterns.split(","));
        long locationMatches = columnData.stream()
                .filter(data -> !data.isEmpty())
                .filter(data -> knownLocations.contains(data.trim()))
                .count();
        
        return locationMatches > columnData.size() * 0.3;
    }
    
    private boolean detectIndicatorNamePattern(String header, List<String> columnData) {
        String headerLower = header.toLowerCase();
        if (headerLower.contains("indicator") || headerLower.contains("metric") || headerLower.contains("measure") ||
            headerLower.contains("name") || headerLower.contains("description")) {
            return true;
        }
        
        // Check if data contains descriptive text (not numbers)
        long textMatches = columnData.stream()
                .filter(data -> !data.isEmpty())
                .filter(data -> !NUMBER_PATTERN.matcher(data).matches())
                .count();
        
        return textMatches > columnData.size() * 0.7;
    }
    
    private boolean detectValuePattern(String header, List<String> columnData) {
        String headerLower = header.toLowerCase();
        if (headerLower.contains("value") || headerLower.contains("amount") || headerLower.contains("number") ||
            headerLower.contains("score") || headerLower.contains("rate")) {
            return true;
        }
        
        // Check if data is mostly numeric
        long numericMatches = columnData.stream()
                .filter(data -> !data.isEmpty())
                .filter(data -> NUMBER_PATTERN.matcher(data).matches() || PERCENTAGE_PATTERN.matcher(data).matches())
                .count();
        
        return numericMatches > columnData.size() * 0.7;
    }
    
    private boolean detectUnitPattern(String header, List<String> columnData) {
        String headerLower = header.toLowerCase();
        if (headerLower.contains("unit") || headerLower.contains("measurement")) {
            return true;
        }
        
        // Check for unit patterns in data
        long unitMatches = columnData.stream()
                .filter(data -> !data.isEmpty())
                .filter(data -> UNIT_PATTERN.matcher(data).matches())
                .count();
        
        return unitMatches > columnData.size() * 0.5;
    }
    
    private boolean detectSourcePattern(String header, List<String> columnData) {
        String headerLower = header.toLowerCase();
        if (headerLower.contains("source") || headerLower.contains("reference") || headerLower.contains("url")) {
            return true;
        }
        
        // Check for URL patterns
        long urlMatches = columnData.stream()
                .filter(data -> !data.isEmpty())
                .filter(data -> data.startsWith("http://") || data.startsWith("https://"))
                .count();
        
        return urlMatches > columnData.size() * 0.3;
    }
    
    private Double calculateConfidence(String header, List<String> columnData, DimensionType type) {
        double baseConfidence = 0.5;
        
        switch (type) {
            case TIME:
                if (detectTimePattern(header, columnData)) baseConfidence += 0.4;
                break;
            case LOCATION:
                if (detectLocationPattern(header, columnData)) baseConfidence += 0.4;
                break;
            case INDICATOR_NAME:
                if (detectIndicatorNamePattern(header, columnData)) baseConfidence += 0.4;
                break;
            case INDICATOR_VALUE:
                if (detectValuePattern(header, columnData)) baseConfidence += 0.4;
                break;
            case UNIT:
                if (detectUnitPattern(header, columnData)) baseConfidence += 0.4;
                break;
            case SOURCE:
                if (detectSourcePattern(header, columnData)) baseConfidence += 0.4;
                break;
        }
        
        return Math.min(baseConfidence, 1.0);
    }
    
    private String generateReason(String header, List<String> columnData, DimensionType type) {
        switch (type) {
            case TIME:
                return "Detected time patterns in header and data";
            case LOCATION:
                return "Detected location patterns in header and data";
            case INDICATOR_NAME:
                return "Detected descriptive text patterns";
            case INDICATOR_VALUE:
                return "Detected numeric value patterns";
            case UNIT:
                return "Detected unit measurement patterns";
            case SOURCE:
                return "Detected source/reference patterns";
            default:
                return "Generic dimension type";
        }
    }
    
    private String detectOrientation(List<CsvColumnMapping> mappings, List<List<String>> csvData) {
        // Check if indicators are in first column (rows) or first row (columns)
        Optional<CsvColumnMapping> firstColumnMapping = mappings.stream()
                .filter(m -> m.getColumnIndex() == 0)
                .findFirst();
        
        if (firstColumnMapping.isPresent() && firstColumnMapping.get().getDimensionType() == DimensionType.INDICATOR_NAME) {
            return "ROWS";
        }
        
        // Check first row for indicator names
        if (!csvData.isEmpty()) {
            List<String> firstRow = csvData.get(0);
            long indicatorHeaders = firstRow.stream()
                    .filter(header -> !header.isEmpty() && !NUMBER_PATTERN.matcher(header).matches())
                    .count();
            
            if (indicatorHeaders > firstRow.size() * 0.5) {
                return "COLUMNS";
            }
        }
        
        return "ROWS"; // Default assumption
    }
    
    private List<String> extractAxis(List<CsvColumnMapping> mappings, List<List<String>> csvData, DimensionType type) {
        return mappings.stream()
                .filter(m -> m.getDimensionType() == type)
                .flatMap(m -> {
                    if (m.getColumnIndex() < csvData.get(0).size()) {
                        return csvData.stream()
                                .map(row -> row.size() > m.getColumnIndex() ? row.get(m.getColumnIndex()) : "")
                                .filter(val -> !val.isEmpty())
                                .distinct();
                    }
                    return java.util.stream.Stream.empty();
                })
                .distinct()
                .collect(Collectors.toList());
    }
    
    private List<String> extractAdditionalAxes(List<CsvColumnMapping> mappings, List<List<String>> csvData) {
        return mappings.stream()
                .filter(m -> m.getDimensionType() == DimensionType.ADDITIONAL)
                .map(m -> m.getColumnHeader())
                .collect(Collectors.toList());
    }
    
    private Map<String, Object> calculateValueCoordinates(List<CsvColumnMapping> mappings, List<List<String>> csvData, String orientation) {
        Map<String, Object> coordinates = new HashMap<>();
        
        // Find value columns
        List<CsvColumnMapping> valueMappings = mappings.stream()
                .filter(m -> m.getDimensionType() == DimensionType.INDICATOR_VALUE)
                .collect(Collectors.toList());
        
        for (CsvColumnMapping valueMapping : valueMappings) {
            int colIndex = valueMapping.getColumnIndex();
            if (colIndex < csvData.get(0).size()) {
                List<String> values = csvData.stream()
                        .map(row -> row.size() > colIndex ? row.get(colIndex) : "")
                        .collect(Collectors.toList());
                
                coordinates.put("column_" + colIndex, values);
            }
        }
        
        return coordinates;
    }
    
    private List<String> identifyMissingDimensions(List<CsvColumnMapping> mappings) {
        List<String> missing = new ArrayList<>();
        
        boolean hasIndicatorName = mappings.stream()
                .anyMatch(m -> m.getDimensionType() == DimensionType.INDICATOR_NAME);
        boolean hasIndicatorValue = mappings.stream()
                .anyMatch(m -> m.getDimensionType() == DimensionType.INDICATOR_VALUE);
        
        if (!hasIndicatorName) missing.add("INDICATOR_NAME");
        if (!hasIndicatorValue) missing.add("INDICATOR_VALUE");
        
        return missing;
    }
    
    private Integer calculateTotalValues(List<List<String>> csvData, String orientation) {
        if (csvData.isEmpty()) return 0;
        
        if ("ROWS".equals(orientation)) {
            // Values are in columns, indicators in first column
            return (csvData.size() - 1) * (csvData.get(0).size() - 1); // Exclude header row and first column
        } else {
            // Values are in rows, indicators in first row
            return (csvData.size() - 1) * (csvData.get(0).size() - 1); // Exclude header row and first row
        }
    }
    
    private List<List<String>> getCsvData(CsvAnalysis analysis) {
        try {
            Path filePath = Paths.get(analysis.getFilePath());
            if (!Files.exists(filePath)) {
                throw new BadRequestException("CSV file not found: " + analysis.getFilePath());
            }
            
            // Parse CSV using existing logic
            return parseCsvFile(filePath, analysis.getEncoding(), analysis.getDelimiter());
        } catch (IOException e) {
            throw new BadRequestException("Failed to read CSV file: " + e.getMessage());
        }
    }
    
    private List<List<String>> parseCsvFile(Path filePath, String encoding, String delimiter) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        
        try (java.io.BufferedReader reader = Files.newBufferedReader(filePath, java.nio.charset.Charset.forName(encoding));
             org.apache.commons.csv.CSVParser parser = org.apache.commons.csv.CSVParser.parse(reader, 
                     org.apache.commons.csv.CSVFormat.DEFAULT.withDelimiter(delimiter.charAt(0)))) {
            
            for (org.apache.commons.csv.CSVRecord record : parser) {
                List<String> row = new ArrayList<>();
                for (String value : record) {
                    row.add(value.trim());
                }
                rows.add(row);
            }
        }
        
        return rows;
    }
    
    private String serializeRules(Map<String, Object> rules) {
        try {
            return objectMapper.writeValueAsString(rules);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize mapping rules", e);
            return "{}";
        }
    }
    
    private CsvAnalysis findAnalysisByJobId(Long jobId) {
        List<CsvAnalysis> analyses = csvAnalysisRepository.findByJobId(jobId);
        if (analyses.isEmpty()) {
            throw new ResourceNotFoundException("CsvAnalysis", "jobId", jobId);
        }
        return analyses.get(0); // Return the first analysis for the job
    }
} 