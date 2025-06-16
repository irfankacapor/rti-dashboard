package io.dashboard.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dashboard.dto.CsvColumnInfo;
import io.dashboard.dto.CsvPreviewResponse;
import io.dashboard.dto.CsvRowData;
import io.dashboard.dto.CsvStructureResponse;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.CsvAnalysis;
import io.dashboard.model.CsvColumn;
import io.dashboard.model.UploadFile;
import io.dashboard.repository.CsvAnalysisRepository;
import io.dashboard.repository.CsvColumnRepository;
import io.dashboard.repository.UploadFileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvParsingService {
    
    private final CsvAnalysisRepository csvAnalysisRepository;
    private final CsvColumnRepository csvColumnRepository;
    private final UploadFileRepository uploadFileRepository;
    private final ObjectMapper objectMapper;
    
    @Value("${app.csv.preview-row-limit:100}")
    private int previewRowLimit;
    
    @Value("${app.csv.max-columns:50}")
    private int maxColumns;
    
    private static final List<String> SUPPORTED_DELIMITERS = Arrays.asList(",", ";", "\t", "|");
    private static final List<Charset> SUPPORTED_ENCODINGS = Arrays.asList(
        StandardCharsets.UTF_8,
        StandardCharsets.ISO_8859_1,
        Charset.forName("Windows-1252")
    );
    
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^-?\\d+(\\.\\d+)?$");
    private static final Pattern BOOLEAN_PATTERN = Pattern.compile("^(true|false|yes|no|1|0)$", Pattern.CASE_INSENSITIVE);
    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
    );
    
    @Transactional
    public CsvPreviewResponse parseAndPreview(Long jobId, String filename) throws IOException {
        UploadFile uploadFile = uploadFileRepository.findByUploadJobId(jobId).stream()
                .filter(file -> file.getFilename().equals(filename))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("UploadFile", "filename", filename));
        
        Path filePath = Paths.get(uploadFile.getStoredPath());
        if (!Files.exists(filePath)) {
            throw new BadRequestException("File not found: " + filename);
        }
        
        // Detect encoding and delimiter
        String encoding = detectEncoding(filePath);
        String delimiter = detectDelimiter(filePath, encoding);
        
        // Parse CSV
        List<List<String>> allRows = parseCsvFile(filePath, encoding, delimiter);
        
        // Detect if has header
        boolean hasHeader = detectHeader(allRows);
        
        // Extract headers and data
        List<String> headers = hasHeader ? allRows.get(0) : generateHeaders(allRows.get(0).size());
        List<List<String>> dataRows = hasHeader ? allRows.subList(1, allRows.size()) : allRows;
        
        // Create preview data
        List<CsvRowData> previewData = dataRows.stream()
                .limit(previewRowLimit)
                .map(row -> {
                    CsvRowData rowData = new CsvRowData();
                    rowData.setValues(row);
                    return rowData;
                })
                .collect(Collectors.toList());
        
        // Add row indices
        for (int i = 0; i < previewData.size(); i++) {
            previewData.get(i).setRowIndex(i + (hasHeader ? 1 : 0));
        }
        
        CsvPreviewResponse response = new CsvPreviewResponse();
        response.setFilename(filename);
        response.setRowCount((long) dataRows.size());
        response.setColumnCount(headers.size());
        response.setHeaders(headers);
        response.setPreviewData(previewData);
        response.setDelimiter(delimiter);
        response.setEncoding(encoding);
        response.setHasHeader(hasHeader);
        
        return response;
    }
    
    @Transactional
    public CsvStructureResponse analyzeCsvStructure(Long jobId, String filename) throws IOException {
        // Check if analysis already exists
        Optional<CsvAnalysis> existingAnalysis = csvAnalysisRepository.findByJobIdAndFilename(jobId, filename);
        if (existingAnalysis.isPresent()) {
            return buildStructureResponse(existingAnalysis.get());
        }
        
        UploadFile uploadFile = uploadFileRepository.findByUploadJobId(jobId).stream()
                .filter(file -> file.getFilename().equals(filename))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("UploadFile", "filename", filename));
        
        Path filePath = Paths.get(uploadFile.getStoredPath());
        if (!Files.exists(filePath)) {
            throw new BadRequestException("File not found: " + filename);
        }
        
        // Detect encoding and delimiter
        String encoding = detectEncoding(filePath);
        String delimiter = detectDelimiter(filePath, encoding);
        
        // Parse CSV
        List<List<String>> allRows = parseCsvFile(filePath, encoding, delimiter);
        
        // Detect if has header
        boolean hasHeader = detectHeader(allRows);
        
        // Extract headers and data
        List<String> headers = hasHeader ? allRows.get(0) : generateHeaders(allRows.get(0).size());
        List<List<String>> dataRows = hasHeader ? allRows.subList(1, allRows.size()) : allRows;
        
        // Create analysis
        CsvAnalysis analysis = new CsvAnalysis();
        analysis.setJobId(jobId);
        analysis.setFilename(filename);
        analysis.setRowCount((long) dataRows.size());
        analysis.setColumnCount(headers.size());
        try {
            analysis.setHeaders(objectMapper.writeValueAsString(headers));
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Failed to serialize headers");
        }
        analysis.setDelimiter(delimiter);
        analysis.setHasHeader(hasHeader);
        analysis.setEncoding(encoding);
        analysis.setFilePath(uploadFile.getStoredPath());
        
        CsvAnalysis savedAnalysis = csvAnalysisRepository.save(analysis);
        
        // Analyze columns
        List<CsvColumn> columns = analyzeColumns(savedAnalysis, headers, dataRows);
        csvColumnRepository.saveAll(columns);
        
        return buildStructureResponse(savedAnalysis);
    }
    
    @Transactional
    public List<CsvStructureResponse> analyzeAllCsvFiles(Long jobId) throws IOException {
        List<UploadFile> uploadFiles = uploadFileRepository.findByUploadJobId(jobId);
        
        if (uploadFiles.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<CsvStructureResponse> results = new ArrayList<>();
        
        for (UploadFile uploadFile : uploadFiles) {
            try {
                CsvStructureResponse analysis = analyzeCsvStructure(jobId, uploadFile.getFilename());
                results.add(analysis);
            } catch (Exception e) {
                log.error("Failed to analyze CSV file: {}", uploadFile.getFilename(), e);
                // Continue with other files
            }
        }
        
        return results;
    }
    
    public String detectDelimiter(Path filePath, String encoding) throws IOException {
        String firstLine = Files.lines(filePath, Charset.forName(encoding))
                .findFirst()
                .orElse("");
        
        Map<String, Integer> delimiterCounts = new HashMap<>();
        for (String delimiter : SUPPORTED_DELIMITERS) {
            delimiterCounts.put(delimiter, (int) firstLine.chars()
                    .mapToObj(ch -> String.valueOf((char) ch))
                    .filter(ch -> ch.equals(delimiter))
                    .count());
        }
        
        // Find the delimiter with the highest count
        String bestDelimiter = delimiterCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(",");
        
        // If no supported delimiters found or count is 0, default to comma
        if (delimiterCounts.get(bestDelimiter) == 0) {
            return ",";
        }
        
        return bestDelimiter;
    }
    
    public String detectEncoding(Path filePath) throws IOException {
        // Try UTF-8 first
        if (isValidEncoding(filePath, StandardCharsets.UTF_8)) {
            return StandardCharsets.UTF_8.name();
        }
        
        // Try other encodings
        for (Charset charset : SUPPORTED_ENCODINGS) {
            if (isValidEncoding(filePath, charset)) {
                return charset.name();
            }
        }
        
        // Default to UTF-8
        return StandardCharsets.UTF_8.name();
    }
    
    private boolean isValidEncoding(Path filePath, Charset charset) {
        try {
            Files.lines(filePath, charset).limit(10).count();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private List<List<String>> parseCsvFile(Path filePath, String encoding, String delimiter) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        
        try (BufferedReader reader = Files.newBufferedReader(filePath, Charset.forName(encoding));
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withDelimiter(delimiter.charAt(0)))) {
            
            for (CSVRecord record : parser) {
                List<String> row = new ArrayList<>();
                for (String value : record) {
                    row.add(value.trim());
                }
                rows.add(row);
            }
        }
        
        return rows;
    }
    
    private boolean detectHeader(List<List<String>> rows) {
        if (rows.isEmpty()) return false;
        
        List<String> firstRow = rows.get(0);
        
        // Check if first row looks like headers (contains text, not numbers)
        long textColumns = firstRow.stream()
                .filter(cell -> !cell.isEmpty() && !NUMBER_PATTERN.matcher(cell).matches())
                .count();
        
        // If all columns are text (no numbers), likely headers
        boolean allText = textColumns == firstRow.size();
        
        // Additional check: if second row exists and has different pattern, first row is likely header
        if (rows.size() > 1 && allText) {
            List<String> secondRow = rows.get(1);
            long secondRowTextColumns = secondRow.stream()
                    .filter(cell -> !cell.isEmpty() && !NUMBER_PATTERN.matcher(cell).matches())
                    .count();
            
            // If second row has fewer text columns (more numbers), first row is likely header
            if (secondRowTextColumns < textColumns) {
                return true;
            }
        }
        
        // For single row with all text, assume it's a header
        if (rows.size() == 1 && allText) {
            return true;
        }
        
        return false;
    }
    
    private List<String> generateHeaders(int columnCount) {
        List<String> headers = new ArrayList<>();
        for (int i = 0; i < columnCount; i++) {
            headers.add("Column_" + (i + 1));
        }
        return headers;
    }
    
    private List<CsvColumn> analyzeColumns(CsvAnalysis analysis, List<String> headers, List<List<String>> dataRows) {
        List<CsvColumn> columns = new ArrayList<>();
        
        for (int colIndex = 0; colIndex < headers.size(); colIndex++) {
            final int columnIndex = colIndex;
            List<String> columnData = dataRows.stream()
                    .map(row -> row.size() > columnIndex ? row.get(columnIndex) : "")
                    .collect(Collectors.toList());
            
            CsvColumn column = new CsvColumn();
            column.setCsvAnalysis(analysis);
            column.setColumnIndex(columnIndex);
            column.setColumnName(headers.get(columnIndex));
            column.setDataType(inferDataType(columnData));
            try {
                column.setSampleValues(objectMapper.writeValueAsString(getSampleValues(columnData)));
            } catch (JsonProcessingException e) {
                column.setSampleValues("[]");
            }
            column.setNullCount(columnData.stream().filter(val -> val == null || val.equals("null")).count());
            column.setEmptyCount(columnData.stream().filter(String::isEmpty).count());
            column.setUniqueCount(columnData.stream().distinct().count());
            
            columns.add(column);
        }
        
        return columns;
    }
    
    private String inferDataType(List<String> columnData) {
        if (columnData.isEmpty()) return "string";
        
        boolean allNumbers = true;
        boolean allBooleans = true;
        boolean allDates = true;
        
        for (String value : columnData) {
            if (value == null || value.isEmpty()) continue;
            
            if (!NUMBER_PATTERN.matcher(value).matches()) {
                allNumbers = false;
            }
            
            if (!BOOLEAN_PATTERN.matcher(value).matches()) {
                allBooleans = false;
            }
            
            if (!isValidDate(value)) {
                allDates = false;
            }
        }
        
        if (allNumbers) return "number";
        if (allBooleans) return "boolean";
        if (allDates) return "date";
        return "string";
    }
    
    private boolean isValidDate(String value) {
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                LocalDate.parse(value, formatter);
                return true;
            } catch (DateTimeParseException e) {
                // Continue to next formatter
            }
        }
        return false;
    }
    
    private List<String> getSampleValues(List<String> columnData) {
        return columnData.stream()
                .filter(val -> val != null && !val.isEmpty())
                .distinct()
                .limit(5)
                .collect(Collectors.toList());
    }
    
    private CsvStructureResponse buildStructureResponse(CsvAnalysis analysis) {
        CsvStructureResponse response = new CsvStructureResponse();
        response.setAnalysisId(analysis.getId());
        response.setFilename(analysis.getFilename());
        response.setRowCount(analysis.getRowCount());
        response.setColumnCount(analysis.getColumnCount());
        response.setDelimiter(analysis.getDelimiter());
        response.setEncoding(analysis.getEncoding());
        response.setHasHeader(analysis.getHasHeader());
        response.setAnalyzedAt(analysis.getAnalyzedAt());
        
        try {
            response.setHeaders(objectMapper.readValue(analysis.getHeaders(), new TypeReference<List<String>>() {}));
        } catch (Exception e) {
            response.setHeaders(new ArrayList<>());
        }
        
        // Get column information
        List<CsvColumn> columns = csvColumnRepository.findByCsvAnalysisId(analysis.getId());
        List<CsvColumnInfo> columnInfos = columns.stream()
                .map(this::buildColumnInfo)
                .collect(Collectors.toList());
        response.setColumns(columnInfos);
        
        return response;
    }
    
    private CsvColumnInfo buildColumnInfo(CsvColumn column) {
        CsvColumnInfo info = new CsvColumnInfo();
        info.setColumnIndex(column.getColumnIndex());
        info.setColumnName(column.getColumnName());
        info.setDataType(column.getDataType());
        info.setNullCount(column.getNullCount());
        info.setEmptyCount(column.getEmptyCount());
        info.setUniqueCount(column.getUniqueCount());
        
        try {
            info.setSampleValues(objectMapper.readValue(column.getSampleValues(), new TypeReference<List<String>>() {}));
        } catch (Exception e) {
            info.setSampleValues(new ArrayList<>());
        }
        
        return info;
    }
} 