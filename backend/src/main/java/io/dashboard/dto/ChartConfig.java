package io.dashboard.dto;

public class ChartConfig {
    private String title;
    private String xAxisLabel;
    private String yAxisLabel;
    private String colorScheme;
    private String chartOptions;

    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getXAxisLabel() { return xAxisLabel; }
    public void setXAxisLabel(String xAxisLabel) { this.xAxisLabel = xAxisLabel; }
    public String getYAxisLabel() { return yAxisLabel; }
    public void setYAxisLabel(String yAxisLabel) { this.yAxisLabel = yAxisLabel; }
    public String getColorScheme() { return colorScheme; }
    public void setColorScheme(String colorScheme) { this.colorScheme = colorScheme; }
    public String getChartOptions() { return chartOptions; }
    public void setChartOptions(String chartOptions) { this.chartOptions = chartOptions; }
} 