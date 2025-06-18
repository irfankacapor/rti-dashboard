package io.dashboard.dto;

import java.util.List;

public class TrendAnalysisResponse {
    private List<DataPoint> historical;
    private List<DataPoint> projected;
    private TrendLine trendLine;
    private ChartConfig chartConfig;

    public static class DataPoint {
        private String label;
        private Double value;
        private String type; // "historical" or "projected"

        // Getters and setters
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public Double getValue() { return value; }
        public void setValue(Double value) { this.value = value; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    public static class TrendLine {
        private Double slope;
        private Double intercept;
        private Double rSquared;
        private List<DataPoint> points;

        // Getters and setters
        public Double getSlope() { return slope; }
        public void setSlope(Double slope) { this.slope = slope; }
        public Double getIntercept() { return intercept; }
        public void setIntercept(Double intercept) { this.intercept = intercept; }
        public Double getRSquared() { return rSquared; }
        public void setRSquared(Double rSquared) { this.rSquared = rSquared; }
        public List<DataPoint> getPoints() { return points; }
        public void setPoints(List<DataPoint> points) { this.points = points; }
    }

    // Getters and setters
    public List<DataPoint> getHistorical() { return historical; }
    public void setHistorical(List<DataPoint> historical) { this.historical = historical; }
    public List<DataPoint> getProjected() { return projected; }
    public void setProjected(List<DataPoint> projected) { this.projected = projected; }
    public TrendLine getTrendLine() { return trendLine; }
    public void setTrendLine(TrendLine trendLine) { this.trendLine = trendLine; }
    public ChartConfig getChartConfig() { return chartConfig; }
    public void setChartConfig(ChartConfig chartConfig) { this.chartConfig = chartConfig; }
} 