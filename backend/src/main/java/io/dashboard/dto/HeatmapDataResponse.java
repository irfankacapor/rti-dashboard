package io.dashboard.dto;

import java.util.List;

public class HeatmapDataResponse {
    private List<String> xLabels;
    private List<String> yLabels;
    private List<List<Double>> data;
    private ColorScale colorScale;
    private ChartConfig chartConfig;

    public static class ColorScale {
        private String minColor;
        private String maxColor;
        private Double minValue;
        private Double maxValue;

        // Getters and setters
        public String getMinColor() { return minColor; }
        public void setMinColor(String minColor) { this.minColor = minColor; }
        public String getMaxColor() { return maxColor; }
        public void setMaxColor(String maxColor) { this.maxColor = maxColor; }
        public Double getMinValue() { return minValue; }
        public void setMinValue(Double minValue) { this.minValue = minValue; }
        public Double getMaxValue() { return maxValue; }
        public void setMaxValue(Double maxValue) { this.maxValue = maxValue; }
    }

    // Getters and setters
    public List<String> getXLabels() { return xLabels; }
    public void setXLabels(List<String> xLabels) { this.xLabels = xLabels; }
    public List<String> getYLabels() { return yLabels; }
    public void setYLabels(List<String> yLabels) { this.yLabels = yLabels; }
    public List<List<Double>> getData() { return data; }
    public void setData(List<List<Double>> data) { this.data = data; }
    public ColorScale getColorScale() { return colorScale; }
    public void setColorScale(ColorScale colorScale) { this.colorScale = colorScale; }
    public ChartConfig getChartConfig() { return chartConfig; }
    public void setChartConfig(ChartConfig chartConfig) { this.chartConfig = chartConfig; }
} 