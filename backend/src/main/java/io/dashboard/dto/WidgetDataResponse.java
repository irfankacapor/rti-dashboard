package io.dashboard.dto;

import io.dashboard.model.WidgetType;
import java.util.Map;

public class WidgetDataResponse {
    private Long widgetId;
    private WidgetType widgetType;
    private String title;
    private String position;
    private String size;
    private String config;
    private Map<String, Object> data;

    public WidgetDataResponse() {}

    public WidgetDataResponse(Long widgetId, WidgetType widgetType, String title, String position, String size, String config, Map<String, Object> data) {
        this.widgetId = widgetId;
        this.widgetType = widgetType;
        this.title = title;
        this.position = position;
        this.size = size;
        this.config = config;
        this.data = data;
    }

    // Getters and setters
    public Long getWidgetId() { return widgetId; }
    public void setWidgetId(Long widgetId) { this.widgetId = widgetId; }
    
    public WidgetType getWidgetType() { return widgetType; }
    public void setWidgetType(WidgetType widgetType) { this.widgetType = widgetType; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    
    public String getConfig() { return config; }
    public void setConfig(String config) { this.config = config; }
    
    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
} 