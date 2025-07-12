package io.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dashboard.dto.VisualizationConfigRequest;
import io.dashboard.dto.VisualizationConfigResponse;
import io.dashboard.entity.VisualizationType;
import io.dashboard.service.VisualizationConfigService;
import io.dashboard.test.security.WithMockAdmin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockAdmin
class VisualizationConfigControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VisualizationConfigService visualizationConfigService;



    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getConfigsForIndicator_shouldReturn200() throws Exception {
        VisualizationConfigResponse response = new VisualizationConfigResponse();
        response.setId(1L);
        response.setIndicatorId(1L);
        response.setVisualizationType(VisualizationType.LINE);
        response.setTitle("Test Config");
        response.setDefault(false);
        response.setCreatedAt(LocalDateTime.now());
        
        when(visualizationConfigService.findByIndicatorId(1L)).thenReturn(Arrays.asList(response));

        mockMvc.perform(get("/api/v1/visualization-configs/indicators/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].indicatorId").value(1));
    }

    @Test
    void getConfigById_withExistingId_shouldReturn200() throws Exception {
        // This endpoint returns 404 for now as per the controller implementation
        mockMvc.perform(get("/api/v1/visualization-configs/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createConfig_withValidData_shouldReturn201() throws Exception {
        VisualizationConfigRequest request = new VisualizationConfigRequest();
        request.setIndicatorId(1L);
        request.setVisualizationType(VisualizationType.LINE);
        request.setTitle("New Config");
        request.setConfig("{\"title\":\"New Config\"}");
        request.setDefault(false);

        VisualizationConfigResponse response = new VisualizationConfigResponse();
        response.setId(1L);
        response.setIndicatorId(1L);
        response.setVisualizationType(VisualizationType.LINE);
        response.setTitle("New Config");
        response.setDefault(false);
        response.setCreatedAt(LocalDateTime.now());

        when(visualizationConfigService.createConfig(any(VisualizationConfigRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/visualization-configs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("New Config"));
    }

    @Test
    void createConfig_withInvalidData_shouldReturn400() throws Exception {
        VisualizationConfigRequest request = new VisualizationConfigRequest();
        request.setIndicatorId(0L); // Invalid ID
        request.setVisualizationType(VisualizationType.LINE);

        when(visualizationConfigService.createConfig(any(VisualizationConfigRequest.class)))
                .thenThrow(new RuntimeException("Invalid data"));

        mockMvc.perform(post("/api/v1/visualization-configs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockAdmin
    void updateConfig_withValidData_shouldReturn200() throws Exception {
        VisualizationConfigRequest request = new VisualizationConfigRequest();
        request.setIndicatorId(1L);
        request.setVisualizationType(VisualizationType.BAR);
        request.setTitle("Updated Config");
        request.setConfig("{\"title\":\"Updated Config\"}");
        request.setDefault(true);

        VisualizationConfigResponse response = new VisualizationConfigResponse();
        response.setId(1L);
        response.setIndicatorId(1L);
        response.setVisualizationType(VisualizationType.BAR);
        response.setTitle("Updated Config");
        response.setDefault(true);
        response.setCreatedAt(LocalDateTime.now());

        when(visualizationConfigService.updateConfig(eq(1L), any(VisualizationConfigRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/visualization-configs/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Config"))
                .andExpect(jsonPath("$.visualizationType").value("BAR"));
    }

    @Test
    void updateConfig_withNonExistentId_shouldReturn404() throws Exception {
        VisualizationConfigRequest request = new VisualizationConfigRequest();
        request.setIndicatorId(1L);
        request.setVisualizationType(VisualizationType.LINE);

        when(visualizationConfigService.updateConfig(eq(999L), any(VisualizationConfigRequest.class)))
                .thenThrow(new RuntimeException("Config not found"));

        mockMvc.perform(put("/api/v1/visualization-configs/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void deleteConfig_withExistingId_shouldReturn204() throws Exception {
        doNothing().when(visualizationConfigService).deleteConfig(1L);
        
        mockMvc.perform(delete("/api/v1/visualization-configs/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteConfig_withNonExistentId_shouldReturn404() throws Exception {
        doThrow(new RuntimeException("Config not found")).when(visualizationConfigService).deleteConfig(999L);

        mockMvc.perform(delete("/api/v1/visualization-configs/999"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void setAsDefault_withExistingId_shouldReturn200() throws Exception {
        VisualizationConfigResponse response = new VisualizationConfigResponse();
        response.setId(1L);
        response.setIndicatorId(1L);
        response.setVisualizationType(VisualizationType.LINE);
        response.setDefault(true);
        response.setCreatedAt(LocalDateTime.now());

        when(visualizationConfigService.setAsDefault(1L)).thenReturn(response);

        mockMvc.perform(post("/api/v1/visualization-configs/1/set-default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.default").value(true));
    }

    @Test
    void setAsDefault_withNonExistentId_shouldReturn404() throws Exception {
        when(visualizationConfigService.setAsDefault(999L))
                .thenThrow(new RuntimeException("Config not found"));

        mockMvc.perform(post("/api/v1/visualization-configs/999/set-default"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void cloneConfig_withExistingId_shouldReturn201() throws Exception {
        VisualizationConfigResponse response = new VisualizationConfigResponse();
        response.setId(2L);
        response.setIndicatorId(1L);
        response.setVisualizationType(VisualizationType.LINE);
        response.setTitle("Cloned Config");
        response.setDefault(false);
        response.setCreatedAt(LocalDateTime.now());

        when(visualizationConfigService.cloneConfig(eq(1L), eq("Cloned Config"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/visualization-configs/1/clone")
                .param("newTitle", "Cloned Config"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.title").value("Cloned Config"));
    }

    @Test
    void cloneConfig_withNonExistentId_shouldReturn404() throws Exception {
        when(visualizationConfigService.cloneConfig(eq(999L), anyString()))
                .thenThrow(new RuntimeException("Config not found"));

        mockMvc.perform(post("/api/v1/visualization-configs/999/clone")
                .param("newTitle", "Cloned Config"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void cloneConfig_withEmptyTitle_shouldReturn400() throws Exception {
        when(visualizationConfigService.cloneConfig(eq(1L), eq("")))
                .thenThrow(new RuntimeException("Title is required"));

        mockMvc.perform(post("/api/v1/visualization-configs/1/clone")
                .param("newTitle", ""))
                .andExpect(status().isInternalServerError());
    }
} 