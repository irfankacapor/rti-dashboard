package io.dashboard.dto;

import java.util.List;

public class LocationComparisonResponse {
    private List<String> categories;
    private List<Series> series;
    private ChartConfig chartConfig;

    public static class Series {
        private String name;
        private List<Double> data;
        private String color;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<Double> getData() { return data; }
        public void setData(List<Double> data) { this.data = data; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
    }

    // Getters and setters
    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }
    public List<Series> getSeries() { return series; }
    public void setSeries(List<Series> series) { this.series = series; }
    public ChartConfig getChartConfig() { return chartConfig; }
    public void setChartConfig(ChartConfig chartConfig) { this.chartConfig = chartConfig; }
} 