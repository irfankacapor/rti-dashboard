package io.dashboard.dto;

import lombok.Data;
import java.util.List;

@Data
public class CsvRowData {
    private Integer rowIndex;
    private List<String> values;
} 