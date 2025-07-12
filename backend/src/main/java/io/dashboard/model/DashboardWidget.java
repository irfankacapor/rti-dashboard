package io.dashboard.model;

import jakarta.persistence.*;

@Entity
public class DashboardWidget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long dashboardId;
    private String title;
    @Enumerated(EnumType.STRING)
    private WidgetType widgetType;
    private Long referenceId;
    private Integer positionX;
    private Integer positionY;
    private Integer width;
    private Integer height;
    @Lob
    private String config;
    private Boolean snapToGrid;

    public DashboardWidget() {}

    public DashboardWidget(Long dashboardId, String title, WidgetType widgetType, Long referenceId, Integer positionX, Integer positionY, Integer width, Integer height, String config, Boolean snapToGrid) {
        this.dashboardId = dashboardId;
        this.title = title;
        this.widgetType = widgetType;
        this.referenceId = referenceId;
        this.positionX = positionX;
        this.positionY = positionY;
        this.width = width;
        this.height = height;
        this.config = config;
        this.snapToGrid = snapToGrid;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDashboardId() { return dashboardId; }
    public void setDashboardId(Long dashboardId) { this.dashboardId = dashboardId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public WidgetType getWidgetType() { return widgetType; }
    public void setWidgetType(WidgetType widgetType) { this.widgetType = widgetType; }
    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }
    public Integer getPositionX() { return positionX; }
    public void setPositionX(Integer positionX) { this.positionX = positionX; }
    public Integer getPositionY() { return positionY; }
    public void setPositionY(Integer positionY) { this.positionY = positionY; }
    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }
    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }
    public String getConfig() { return config; }
    public void setConfig(String config) { this.config = config; }
    public Boolean getSnapToGrid() { return snapToGrid; }
    public void setSnapToGrid(Boolean snapToGrid) { this.snapToGrid = snapToGrid; }
} 