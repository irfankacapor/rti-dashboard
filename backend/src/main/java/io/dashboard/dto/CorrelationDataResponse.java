package io.dashboard.dto;

import java.util.List;

public class CorrelationDataResponse {
    private List<Dataset> datasets;
    private Axis xAxis;
    private Axis yAxis;
    private ChartConfig chartConfig;

    public static class Dataset {
        private String label;
        private List<Point> data;
        private String backgroundColor;

        // Getters and setters
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public List<Point> getData() { return data; }
        public void setData(List<Point> data) { this.data = data; }
        public String getBackgroundColor() { return backgroundColor; }
        public void setBackgroundColor(String backgroundColor) { this.backgroundColor = backgroundColor; }
    }

    public static class Point {
        private Double x;
        private Double y;

        // Getters and setters
        public Double getX() { return x; }
        public void setX(Double x) { this.x = x; }
        public Double getY() { return y; }
        public void setY(Double y) { this.y = y; }
    }

    public static class Axis {
        private String label;
        private String type;

        // Getters and setters
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    // Getters and setters
    public List<Dataset> getDatasets() { return datasets; }
    public void setDatasets(List<Dataset> datasets) { this.datasets = datasets; }
    public Axis getXAxis() { return xAxis; }
    public void setXAxis(Axis xAxis) { this.xAxis = xAxis; }
    public Axis getYAxis() { return yAxis; }
    public void setYAxis(Axis yAxis) { this.yAxis = yAxis; }
    public ChartConfig getChartConfig() { return chartConfig; }
    public void setChartConfig(ChartConfig chartConfig) { this.chartConfig = chartConfig; }
} 