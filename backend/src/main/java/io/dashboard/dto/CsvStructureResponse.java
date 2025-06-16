package io.dashboard.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CsvStructureResponse {
    private Long analysisId;
    private String filename;
    private Long rowCount;
    private Integer columnCount;
    private List<String> headers;
    private String delimiter;
    private String encoding;
    private Boolean hasHeader;
    private LocalDateTime analyzedAt;
    private List<CsvColumnInfo> columns;
} 