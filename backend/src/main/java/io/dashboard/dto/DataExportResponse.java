package io.dashboard.dto;

import java.time.LocalDateTime;

public class DataExportResponse {
    private Long dashboardId;
    private String format;
    private String data;
    private LocalDateTime exportedAt;

    public DataExportResponse() {}

    public DataExportResponse(Long dashboardId, String format, String data, LocalDateTime exportedAt) {
        this.dashboardId = dashboardId;
        this.format = format;
        this.data = data;
        this.exportedAt = exportedAt;
    }

    // Getters and setters
    public Long getDashboardId() { return dashboardId; }
    public void setDashboardId(Long dashboardId) { this.dashboardId = dashboardId; }
    
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    
    public LocalDateTime getExportedAt() { return exportedAt; }
    public void setExportedAt(LocalDateTime exportedAt) { this.exportedAt = exportedAt; }
} 