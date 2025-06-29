package io.dashboard.controller;

import io.dashboard.dto.GoalSubareaLinkResponse;
import io.dashboard.model.Area;
import io.dashboard.model.Goal;
import io.dashboard.model.GoalSubarea;
import io.dashboard.model.GoalGroup;
import io.dashboard.model.Subarea;
import io.dashboard.service.GoalSubareaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({GoalSubareaController.class, SubareaGoalController.class})
class GoalSubareaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GoalSubareaService goalSubareaService;

    private Goal goal;
    private Subarea subarea;
    private GoalGroup goalGroup;
    private Area area;
    private GoalSubarea goalSubarea;

    @BeforeEach
    void setUp() {
        goalGroup = new GoalGroup();
        goalGroup.setId(1L);
        goalGroup.setName("SDGs");

        goal = new Goal();
        goal.setId(1L);
        goal.setName("Test Goal");
        goal.setGoalGroup(goalGroup);

        area = new Area();
        area.setId(1L);
        area.setName("Test Area");

        subarea = new Subarea();
        subarea.setId(1L);
        subarea.setName("Test Subarea");
        subarea.setCode("TEST_SUB");
        subarea.setArea(area);

        goalSubarea = new GoalSubarea();
        goalSubarea.setGoal(goal);
        goalSubarea.setSubarea(subarea);
        goalSubarea.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void linkGoalToSubarea_shouldSucceed() throws Exception {
        // Given
        GoalSubareaLinkResponse response = createGoalSubareaLinkResponse();
        when(goalSubareaService.linkGoalToSubarea(1L, 1L)).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/goals/1/subareas/1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.goalId").value(1))
                .andExpect(jsonPath("$.subareaId").value(1))
                .andExpect(jsonPath("$.goalName").value("Test Goal"))
                .andExpect(jsonPath("$.subareaName").value("Test Subarea"));
    }

    @Test
    void unlinkGoalFromSubarea_shouldSucceed() throws Exception {
        // Given
        doNothing().when(goalSubareaService).unlinkGoalFromSubarea(1L, 1L);

        // When & Then
        mockMvc.perform(delete("/api/v1/goals/1/subareas/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getSubareasByGoal_shouldReturnList() throws Exception {
        // Given
        List<GoalSubareaLinkResponse> responses = Arrays.asList(createGoalSubareaLinkResponse());
        when(goalSubareaService.findSubareasByGoal(1L)).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/api/v1/goals/1/subareas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].goalId").value(1))
                .andExpect(jsonPath("$[0].subareaId").value(1))
                .andExpect(jsonPath("$[0].goalName").value("Test Goal"))
                .andExpect(jsonPath("$[0].subareaName").value("Test Subarea"));
    }

    @Test
    void getGoalsBySubarea_shouldReturnList() throws Exception {
        // Given
        List<GoalSubareaLinkResponse> responses = Arrays.asList(createGoalSubareaLinkResponse());
        when(goalSubareaService.findGoalsBySubarea(1L)).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/api/v1/subareas/1/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].goalId").value(1))
                .andExpect(jsonPath("$[0].subareaId").value(1))
                .andExpect(jsonPath("$[0].goalName").value("Test Goal"))
                .andExpect(jsonPath("$[0].subareaName").value("Test Subarea"));
    }

    @Test
    void linkGoalToSubarea_shouldFail_whenGoalNotFound() throws Exception {
        // Given
        when(goalSubareaService.linkGoalToSubarea(anyLong(), anyLong()))
                .thenThrow(new io.dashboard.exception.ResourceNotFoundException("Goal", "id", 1L));

        // When & Then
        mockMvc.perform(post("/api/v1/goals/1/subareas/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void linkGoalToSubarea_shouldFail_whenSubareaNotFound() throws Exception {
        // Given
        when(goalSubareaService.linkGoalToSubarea(anyLong(), anyLong()))
                .thenThrow(new io.dashboard.exception.ResourceNotFoundException("Subarea", "id", 1L));

        // When & Then
        mockMvc.perform(post("/api/v1/goals/1/subareas/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void linkGoalToSubarea_shouldFail_whenAlreadyLinked() throws Exception {
        // Given
        when(goalSubareaService.linkGoalToSubarea(anyLong(), anyLong()))
                .thenThrow(new io.dashboard.exception.BadRequestException("Goal is already linked to this subarea"));

        // When & Then
        mockMvc.perform(post("/api/v1/goals/1/subareas/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unlinkGoalFromSubarea_shouldFail_whenNotLinked() throws Exception {
        // Given
        doThrow(new io.dashboard.exception.ResourceNotFoundException("Goal-Subarea link", "id", "1-1"))
                .when(goalSubareaService).unlinkGoalFromSubarea(1L, 1L);

        // When & Then
        mockMvc.perform(delete("/api/v1/goals/1/subareas/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSubareasByGoal_shouldFail_whenGoalNotFound() throws Exception {
        // Given
        when(goalSubareaService.findSubareasByGoal(anyLong()))
                .thenThrow(new io.dashboard.exception.ResourceNotFoundException("Goal", "id", 1L));

        // When & Then
        mockMvc.perform(get("/api/v1/goals/1/subareas"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getGoalsBySubarea_shouldFail_whenSubareaNotFound() throws Exception {
        // Given
        when(goalSubareaService.findGoalsBySubarea(anyLong()))
                .thenThrow(new io.dashboard.exception.ResourceNotFoundException("Subarea", "id", 1L));

        // When & Then
        mockMvc.perform(get("/api/v1/subareas/1/goals"))
                .andExpect(status().isNotFound());
    }

    private GoalSubareaLinkResponse createGoalSubareaLinkResponse() {
        GoalSubareaLinkResponse response = new GoalSubareaLinkResponse();
        response.setGoalId(1L);
        response.setGoalName("Test Goal");
        response.setSubareaId(1L);
        response.setSubareaName("Test Subarea");
        response.setSubareaCode("TEST_SUB");
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }
} 