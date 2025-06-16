package io.dashboard.dto;

import lombok.Data;
import java.util.List;

@Data
public class CsvPreviewResponse {
    private String filename;
    private Long rowCount;
    private Integer columnCount;
    private List<String> headers;
    private List<CsvRowData> previewData;
    private String delimiter;
    private String encoding;
    private Boolean hasHeader;
} 