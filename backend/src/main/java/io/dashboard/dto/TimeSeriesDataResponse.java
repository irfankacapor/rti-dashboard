package io.dashboard.dto;

import java.util.List;

public class TimeSeriesDataResponse {
    private List<String> labels;
    private List<Dataset> datasets;
    private ChartConfig chartConfig;

    public static class Dataset {
        private String label;
        private List<Double> data;
        private String borderColor;
        private String backgroundColor;
        private boolean fill;

        // Getters and setters
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public List<Double> getData() { return data; }
        public void setData(List<Double> data) { this.data = data; }
        public String getBorderColor() { return borderColor; }
        public void setBorderColor(String borderColor) { this.borderColor = borderColor; }
        public String getBackgroundColor() { return backgroundColor; }
        public void setBackgroundColor(String backgroundColor) { this.backgroundColor = backgroundColor; }
        public boolean isFill() { return fill; }
        public void setFill(boolean fill) { this.fill = fill; }
    }

    // Getters and setters
    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels; }
    public List<Dataset> getDatasets() { return datasets; }
    public void setDatasets(List<Dataset> datasets) { this.datasets = datasets; }
    public ChartConfig getChartConfig() { return chartConfig; }
    public void setChartConfig(ChartConfig chartConfig) { this.chartConfig = chartConfig; }
} 