package io.dashboard.dto;

import lombok.Data;
import java.util.List;

@Data
public class CsvColumnInfo {
    private Integer columnIndex;
    private String columnName;
    private String dataType;
    private List<String> sampleValues;
    private Long nullCount;
    private Long emptyCount;
    private Long uniqueCount;
} 