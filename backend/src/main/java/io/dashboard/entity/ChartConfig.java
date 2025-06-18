package io.dashboard.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
public class ChartConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String xAxisLabel;
    private String yAxisLabel;
    private String colorScheme;
    @Lob
    private String chartOptions;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChartConfig that = (ChartConfig) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(title, that.title) &&
                Objects.equals(xAxisLabel, that.xAxisLabel) &&
                Objects.equals(yAxisLabel, that.yAxisLabel) &&
                Objects.equals(colorScheme, that.colorScheme) &&
                Objects.equals(chartOptions, that.chartOptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, xAxisLabel, yAxisLabel, colorScheme, chartOptions);
    }

    @Override
    public String toString() {
        return "ChartConfig{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", xAxisLabel='" + xAxisLabel + '\'' +
                ", yAxisLabel='" + yAxisLabel + '\'' +
                ", colorScheme='" + colorScheme + '\'' +
                '}';
    }
} 