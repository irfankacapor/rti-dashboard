package io.dashboard.repository;

import io.dashboard.model.DashboardWidget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DashboardWidgetRepository extends JpaRepository<DashboardWidget, Long> {
    List<DashboardWidget> findByDashboardId(Long dashboardId);
} 