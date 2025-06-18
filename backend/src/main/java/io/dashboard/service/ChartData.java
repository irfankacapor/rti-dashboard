package io.dashboard.service;

import java.util.List;

public class ChartData {
    private List<String> labels;
    private List<Double> data;
    private String color;

    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels; }
    public List<Double> getData() { return data; }
    public void setData(List<Double> data) { this.data = data; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
} 