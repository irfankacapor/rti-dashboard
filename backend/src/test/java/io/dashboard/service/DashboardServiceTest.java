package io.dashboard.service;

import io.dashboard.dto.DashboardCreateRequest;
import io.dashboard.dto.DashboardUpdateRequest;
import io.dashboard.model.Dashboard;
import io.dashboard.model.DashboardWidget;
import io.dashboard.model.LayoutType;
import io.dashboard.model.WidgetType;
import io.dashboard.repository.DashboardRepository;
import io.dashboard.repository.DashboardWidgetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {
    @Mock
    private DashboardRepository dashboardRepository;
    @Mock
    private DashboardWidgetRepository dashboardWidgetRepository;
    
    @InjectMocks
    private DashboardService dashboardService;

    private Dashboard testDashboard;
    private DashboardWidget testWidget;
    private DashboardCreateRequest createRequest;
    private DashboardUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testDashboard = new Dashboard();
        testDashboard.setId(1L);
        testDashboard.setName("Test Dashboard");
        testDashboard.setDescription("Test Description");
        testDashboard.setDefaultLocationId(1L);
        testDashboard.setDefaultYear(2023);
        testDashboard.setLayoutType(LayoutType.GRID);
        testDashboard.setCreatedAt(LocalDateTime.now());

        testWidget = new DashboardWidget();
        testWidget.setId(1L);
        testWidget.setDashboardId(1L);
        testWidget.setTitle("Test Widget");
        testWidget.setWidgetType(WidgetType.AREA);
        testWidget.setPositionX(10);
        testWidget.setPositionY(20);
        testWidget.setWidth(200);
        testWidget.setHeight(150);
        testWidget.setConfig("{\"refreshInterval\": 30}");

        createRequest = new DashboardCreateRequest();
        createRequest.setName("New Dashboard");
        createRequest.setDescription("New Description");
        createRequest.setDefaultLocationId(1L);
        createRequest.setDefaultYear(2023);
        createRequest.setLayoutType(LayoutType.GRID);

        updateRequest = new DashboardUpdateRequest();
        updateRequest.setName("Updated Dashboard");
        updateRequest.setDescription("Updated Description");
        updateRequest.setDefaultLocationId(2L);
        updateRequest.setDefaultYear(2024);
        updateRequest.setLayoutType(LayoutType.FREE);
    }

    @Test
    void findAll_Success() {
        // Given
        when(dashboardRepository.findAll()).thenReturn(Arrays.asList(testDashboard));

        // When
        List<Dashboard> result = dashboardService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testDashboard, result.get(0));
        verify(dashboardRepository).findAll();
    }

    @Test
    void findAll_EmptyList() {
        // Given
        when(dashboardRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<Dashboard> result = dashboardService.findAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findById_Success() {
        // Given
        when(dashboardRepository.findById(1L)).thenReturn(Optional.of(testDashboard));

        // When
        Dashboard result = dashboardService.findById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testDashboard, result);
        verify(dashboardRepository).findById(1L);
    }

    @Test
    void findById_NotFound() {
        // Given
        when(dashboardRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            dashboardService.findById(1L);
        });
    }

    @Test
    void create_Success() {
        // Given
        when(dashboardRepository.save(any(Dashboard.class))).thenReturn(testDashboard);

        // When
        Dashboard result = dashboardService.create(testDashboard);

        // Then
        assertNotNull(result);
        assertEquals(testDashboard, result);
        verify(dashboardRepository).save(testDashboard);
    }

    @Test
    void update_Success() {
        // Given
        Dashboard updatedDashboard = new Dashboard();
        updatedDashboard.setName("Updated Dashboard");
        updatedDashboard.setDescription("Updated Description");
        updatedDashboard.setLayoutType(LayoutType.FREE);

        when(dashboardRepository.findById(1L)).thenReturn(Optional.of(testDashboard));
        when(dashboardRepository.save(any(Dashboard.class))).thenReturn(updatedDashboard);

        // When
        Dashboard result = dashboardService.update(1L, updatedDashboard);

        // Then
        assertNotNull(result);
        verify(dashboardRepository).findById(1L);
        verify(dashboardRepository).save(any(Dashboard.class));
    }

    @Test
    void update_NotFound() {
        // Given
        when(dashboardRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            dashboardService.update(1L, testDashboard);
        });
    }

    @Test
    void delete_Success() {
        // Given
        when(dashboardRepository.findById(1L)).thenReturn(Optional.of(testDashboard));
        when(dashboardWidgetRepository.findAll()).thenReturn(Arrays.asList(testWidget));

        // When
        dashboardService.delete(1L);

        // Then
        verify(dashboardRepository).findById(1L);
        verify(dashboardWidgetRepository).findAll();
        verify(dashboardWidgetRepository).delete(testWidget);
        verify(dashboardRepository).delete(testDashboard);
    }

    @Test
    void delete_NotFound() {
        // Given
        when(dashboardRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            dashboardService.delete(1L);
        });
    }

    @Test
    void delete_NoWidgets() {
        // Given
        when(dashboardRepository.findById(1L)).thenReturn(Optional.of(testDashboard));
        when(dashboardWidgetRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        dashboardService.delete(1L);

        // Then
        verify(dashboardRepository).findById(1L);
        verify(dashboardWidgetRepository).findAll();
        verify(dashboardWidgetRepository, never()).delete(any());
        verify(dashboardRepository).delete(testDashboard);
    }

    @Test
    void getDashboardWithWidgets_Success() {
        // Given
        when(dashboardRepository.findById(1L)).thenReturn(Optional.of(testDashboard));
        when(dashboardWidgetRepository.findAll()).thenReturn(Arrays.asList(testWidget));

        // When
        DashboardService.DashboardWithWidgets result = dashboardService.getDashboardWithWidgets(1L);

        // Then
        assertNotNull(result);
        assertEquals(testDashboard, result.getDashboard());
        assertEquals(1, result.getWidgets().size());
        assertEquals(testWidget, result.getWidgets().get(0));
        verify(dashboardRepository).findById(1L);
        verify(dashboardWidgetRepository).findAll();
    }

    @Test
    void getDashboardWithWidgets_NotFound() {
        // Given
        when(dashboardRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            dashboardService.getDashboardWithWidgets(1L);
        });
    }

    @Test
    void getDashboardWithWidgets_NoWidgets() {
        // Given
        when(dashboardRepository.findById(1L)).thenReturn(Optional.of(testDashboard));
        when(dashboardWidgetRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        DashboardService.DashboardWithWidgets result = dashboardService.getDashboardWithWidgets(1L);

        // Then
        assertNotNull(result);
        assertEquals(testDashboard, result.getDashboard());
        assertTrue(result.getWidgets().isEmpty());
    }

    @Test
    void getDashboardWithWidgets_WidgetsForOtherDashboard() {
        // Given
        DashboardWidget otherWidget = new DashboardWidget();
        otherWidget.setId(2L);
        otherWidget.setDashboardId(2L);

        when(dashboardRepository.findById(1L)).thenReturn(Optional.of(testDashboard));
        when(dashboardWidgetRepository.findAll()).thenReturn(Arrays.asList(testWidget, otherWidget));

        // When
        DashboardService.DashboardWithWidgets result = dashboardService.getDashboardWithWidgets(1L);

        // Then
        assertNotNull(result);
        assertEquals(testDashboard, result.getDashboard());
        assertEquals(1, result.getWidgets().size());
        assertEquals(testWidget, result.getWidgets().get(0));
    }

    @Test
    void create_WithNullValues() {
        // Given
        Dashboard nullDashboard = new Dashboard();
        nullDashboard.setName(null);
        nullDashboard.setDescription(null);
        nullDashboard.setLayoutType(null);

        when(dashboardRepository.save(any(Dashboard.class))).thenReturn(nullDashboard);

        // When
        Dashboard result = dashboardService.create(nullDashboard);

        // Then
        assertNotNull(result);
        verify(dashboardRepository).save(nullDashboard);
    }

    @Test
    void update_WithNullValues() {
        // Given
        Dashboard updatedDashboard = new Dashboard();
        updatedDashboard.setName(null);
        updatedDashboard.setDescription(null);
        updatedDashboard.setLayoutType(null);

        when(dashboardRepository.findById(1L)).thenReturn(Optional.of(testDashboard));
        when(dashboardRepository.save(any(Dashboard.class))).thenReturn(updatedDashboard);

        // When
        Dashboard result = dashboardService.update(1L, updatedDashboard);

        // Then
        assertNotNull(result);
        verify(dashboardRepository).findById(1L);
        verify(dashboardRepository).save(any(Dashboard.class));
    }

    @Test
    void delete_WithMultipleWidgets() {
        // Given
        DashboardWidget widget2 = new DashboardWidget();
        widget2.setId(2L);
        widget2.setDashboardId(1L);

        when(dashboardRepository.findById(1L)).thenReturn(Optional.of(testDashboard));
        when(dashboardWidgetRepository.findAll()).thenReturn(Arrays.asList(testWidget, widget2));

        // When
        dashboardService.delete(1L);

        // Then
        verify(dashboardRepository).findById(1L);
        verify(dashboardWidgetRepository).findAll();
        verify(dashboardWidgetRepository).delete(testWidget);
        verify(dashboardWidgetRepository).delete(widget2);
        verify(dashboardRepository).delete(testDashboard);
    }

    @Test
    void findAll_RepositoryException() {
        // Given
        when(dashboardRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            dashboardService.findAll();
        });
    }

    @Test
    void findById_RepositoryException() {
        // Given
        when(dashboardRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            dashboardService.findById(1L);
        });
    }

    @Test
    void create_RepositoryException() {
        // Given
        when(dashboardRepository.save(any(Dashboard.class))).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            dashboardService.create(testDashboard);
        });
    }

    @Test
    void update_RepositoryException() {
        // Given
        when(dashboardRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            dashboardService.update(1L, testDashboard);
        });
    }

    @Test
    void delete_RepositoryException() {
        // Given
        when(dashboardRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            dashboardService.delete(1L);
        });
    }

    @Test
    void getDashboardWithWidgets_RepositoryException() {
        // Given
        when(dashboardRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            dashboardService.getDashboardWithWidgets(1L);
        });
    }
} 