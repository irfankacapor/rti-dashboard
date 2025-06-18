package io.dashboard.dto;

import java.util.List;

public class DimensionBreakdownResponse {
    private List<String> labels;
    private List<Double> data;
    private List<String> colors;
    private ChartConfig chartConfig;

    // Getters and setters
    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels; }
    public List<Double> getData() { return data; }
    public void setData(List<Double> data) { this.data = data; }
    public List<String> getColors() { return colors; }
    public void setColors(List<String> colors) { this.colors = colors; }
    public ChartConfig getChartConfig() { return chartConfig; }
    public void setChartConfig(ChartConfig chartConfig) { this.chartConfig = chartConfig; }
} 