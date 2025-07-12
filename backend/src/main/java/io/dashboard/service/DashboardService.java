package io.dashboard.service;

import io.dashboard.model.Dashboard;
import io.dashboard.model.DashboardWidget;
import io.dashboard.repository.DashboardRepository;
import io.dashboard.repository.DashboardWidgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final DashboardRepository dashboardRepository;
    private final DashboardWidgetRepository dashboardWidgetRepository;

    public List<Dashboard> findAll() {
        return dashboardRepository.findAll();
    }

    public Dashboard findById(Long id) {
        return dashboardRepository.findById(id).orElseThrow(() -> new RuntimeException("Dashboard not found"));
    }

    public Dashboard create(Dashboard dashboard) {
        return dashboardRepository.save(dashboard);
    }

    public Dashboard update(Long id, Dashboard dashboard) {
        Dashboard existing = dashboardRepository.findById(id).orElseThrow(() -> new RuntimeException("Dashboard not found"));
        existing.setName(dashboard.getName());
        existing.setDescription(dashboard.getDescription());
        existing.setDefaultLocationId(dashboard.getDefaultLocationId());
        existing.setDefaultYear(dashboard.getDefaultYear());
        existing.setLayoutType(dashboard.getLayoutType());
        return dashboardRepository.save(existing);
    }

    public void delete(Long id) {
        Dashboard dashboard = dashboardRepository.findById(id).orElseThrow(() -> new RuntimeException("Dashboard not found"));
        List<DashboardWidget> widgets = dashboardWidgetRepository.findAll();
        for (DashboardWidget widget : widgets) {
            if (widget.getDashboardId().equals(id)) {
                dashboardWidgetRepository.delete(widget);
            }
        }
        dashboardRepository.delete(dashboard);
    }

    public DashboardWithWidgets getDashboardWithWidgets(Long dashboardId) {
        Dashboard dashboard = dashboardRepository.findById(dashboardId).orElseThrow(() -> new RuntimeException("Dashboard not found"));
        List<DashboardWidget> widgets = dashboardWidgetRepository.findAll();
        return new DashboardWithWidgets(dashboard, widgets.stream().filter(w -> w.getDashboardId().equals(dashboardId)).toList());
    }

    public static class DashboardWithWidgets {
        private final Dashboard dashboard;
        private final List<DashboardWidget> widgets;
        public DashboardWithWidgets(Dashboard dashboard, List<DashboardWidget> widgets) {
            this.dashboard = dashboard;
            this.widgets = widgets;
        }
        public Dashboard getDashboard() { return dashboard; }
        public List<DashboardWidget> getWidgets() { return widgets; }
    }
} 