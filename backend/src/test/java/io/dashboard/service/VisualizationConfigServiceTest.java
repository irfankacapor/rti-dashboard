package io.dashboard.service;

import io.dashboard.dto.VisualizationConfigRequest;
import io.dashboard.dto.VisualizationConfigResponse;
import io.dashboard.entity.VisualizationConfig;
import io.dashboard.entity.VisualizationType;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.repository.VisualizationConfigRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class VisualizationConfigServiceTest {

    @Mock
    private VisualizationConfigRepository visualizationConfigRepository;

    @InjectMocks
    private VisualizationConfigService service;

    private VisualizationConfig config;
    private VisualizationConfigRequest request;

    @BeforeEach
    void setUp() {
        config = new VisualizationConfig();
        config.setId(1L);
        config.setIndicatorId(1L);
        config.setVisualizationType(VisualizationType.LINE);
        config.setConfig("{\"title\":\"Test\"}");
        config.setDefault(false);
        config.setCreatedAt(LocalDateTime.now());

        request = new VisualizationConfigRequest();
        request.setIndicatorId(1L);
        request.setVisualizationType(VisualizationType.LINE);
        request.setConfig("{\"title\":\"Test\"}");
        request.setDefault(false);
    }

    @Test
    void findByIndicatorId_withExistingConfigs_shouldReturnList() {
        when(visualizationConfigRepository.findByIndicatorId(1L)).thenReturn(Collections.singletonList(config));
        List<VisualizationConfigResponse> result = service.findByIndicatorId(1L);
        assertEquals(1, result.size());
    }

    @Test
    void findByIndicatorId_withNoConfigs_shouldReturnEmptyList() {
        when(visualizationConfigRepository.findByIndicatorId(1L)).thenReturn(Collections.emptyList());
        List<VisualizationConfigResponse> result = service.findByIndicatorId(1L);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByIndicatorId_withInvalidId_shouldThrowException() {
        assertThrows(BadRequestException.class, () -> service.findByIndicatorId(0L));
    }

    @Test
    void getDefaultConfig_withDefaultExists_shouldReturnDefault() {
        config.setDefault(true);
        when(visualizationConfigRepository.findByIndicatorIdAndIsDefault(1L, true)).thenReturn(Collections.singletonList(config));
        VisualizationConfigResponse result = service.getDefaultConfig(1L);
        assertNotNull(result);
        assertEquals(1L, result.getIndicatorId());
    }

    @Test
    void getDefaultConfig_withNoDefault_shouldReturnNull() {
        when(visualizationConfigRepository.findByIndicatorIdAndIsDefault(1L, true)).thenReturn(Collections.emptyList());
        VisualizationConfigResponse result = service.getDefaultConfig(1L);
        assertNull(result);
    }

    @Test
    void getDefaultConfig_withInvalidId_shouldThrowException() {
        assertThrows(BadRequestException.class, () -> service.getDefaultConfig(0L));
    }

    @Test
    void createConfig_withValidData_shouldCreateAndReturn() {
        when(visualizationConfigRepository.save(any())).thenReturn(config);
        VisualizationConfigResponse result = service.createConfig(request);
        assertNotNull(result);
        verify(visualizationConfigRepository).save(any());
    }

    @Test
    void createConfig_withInvalidData_shouldThrowException() {
        request.setIndicatorId(0L);
        assertThrows(BadRequestException.class, () -> service.createConfig(request));
    }

    @Test
    void updateConfig_withExistingConfig_shouldUpdate() {
        when(visualizationConfigRepository.findById(1L)).thenReturn(Optional.of(config));
        when(visualizationConfigRepository.save(any())).thenReturn(config);
        VisualizationConfigResponse result = service.updateConfig(1L, request);
        assertNotNull(result);
        verify(visualizationConfigRepository).save(any());
    }

    @Test
    void updateConfig_withNonExistentConfig_shouldThrowException() {
        when(visualizationConfigRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.updateConfig(1L, request));
    }

    @Test
    void updateConfig_withInvalidId_shouldThrowException() {
        assertThrows(BadRequestException.class, () -> service.updateConfig(0L, request));
    }

    @Test
    void deleteConfig_withExistingConfig_shouldDelete() {
        when(visualizationConfigRepository.existsById(1L)).thenReturn(true);
        doNothing().when(visualizationConfigRepository).deleteById(1L);
        assertDoesNotThrow(() -> service.deleteConfig(1L));
        verify(visualizationConfigRepository).deleteById(1L);
    }

    @Test
    void deleteConfig_withNonExistentConfig_shouldThrowException() {
        when(visualizationConfigRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> service.deleteConfig(1L));
    }

    @Test
    void deleteConfig_withInvalidId_shouldThrowException() {
        assertThrows(BadRequestException.class, () -> service.deleteConfig(0L));
    }

    @Test
    void setAsDefault_shouldUpdateAllConfigsForIndicator() {
        when(visualizationConfigRepository.findById(1L)).thenReturn(Optional.of(config));
        when(visualizationConfigRepository.save(any())).thenReturn(config);
        when(visualizationConfigRepository.findByIndicatorIdAndIsDefault(1L, true)).thenReturn(Collections.singletonList(config));
        VisualizationConfigResponse result = service.setAsDefault(1L);
        assertNotNull(result);
        verify(visualizationConfigRepository, atLeastOnce()).save(any());
    }

    @Test
    void setAsDefault_withInvalidId_shouldThrowException() {
        assertThrows(BadRequestException.class, () -> service.setAsDefault(0L));
    }

    @Test
    void setAsDefault_withNonExistentConfig_shouldThrowException() {
        when(visualizationConfigRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.setAsDefault(1L));
    }

    @Test
    void cloneConfig_shouldCreateExactCopy() {
        when(visualizationConfigRepository.findById(1L)).thenReturn(Optional.of(config));
        when(visualizationConfigRepository.save(any())).thenReturn(config);
        VisualizationConfigResponse result = service.cloneConfig(1L, "Cloned Title");
        assertNotNull(result);
        verify(visualizationConfigRepository).save(any());
    }

    @Test
    void cloneConfig_withInvalidId_shouldThrowException() {
        assertThrows(BadRequestException.class, () -> service.cloneConfig(0L, "Cloned Title"));
    }

    @Test
    void cloneConfig_withEmptyTitle_shouldThrowException() {
        assertThatThrownBy(() -> service.cloneConfig(1L, ""))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("New title is required");
    }

    @Test
    void cloneConfig_withNonExistentConfig_shouldThrowException() {
        when(visualizationConfigRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.cloneConfig(1L, "Cloned Title"));
    }

    @Test
    void validateConfig_withValidConfig_shouldPass() {
        assertTrue(service.validateConfig(config));
    }

    @Test
    void validateConfig_withInvalidConfig_shouldFail() {
        VisualizationConfig invalid = new VisualizationConfig();
        assertFalse(service.validateConfig(invalid));
    }

    @Test
    void validateConfig_withInvalidType_shouldFail() {
        VisualizationConfig invalid = new VisualizationConfig();
        invalid.setIndicatorId(1L);
        invalid.setVisualizationType(null);
        assertFalse(service.validateConfig(invalid));
    }

    @Test
    void validateConfig_withInvalidJson_shouldFail() {
        VisualizationConfig invalid = new VisualizationConfig();
        invalid.setIndicatorId(1L);
        invalid.setVisualizationType(VisualizationType.LINE);
        invalid.setConfig("notjson");
        assertFalse(service.validateConfig(invalid));
    }
} 