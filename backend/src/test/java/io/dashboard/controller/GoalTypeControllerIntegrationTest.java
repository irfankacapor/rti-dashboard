package io.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dashboard.dto.GoalTypeCreateRequest;
import io.dashboard.dto.GoalTypeUpdateRequest;
import io.dashboard.service.GoalTypeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GoalTypeController.class)
class GoalTypeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GoalTypeService goalTypeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllGoalTypes_ShouldReturnEmptyList_WhenNoGoalTypes() throws Exception {
        mockMvc.perform(get("/api/v1/goal-types"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void createGoalType_ShouldCreateGoalType_WhenValidData() throws Exception {
        // Given
        GoalTypeCreateRequest request = GoalTypeCreateRequest.builder()
                .name("SDGs")
                .description("Sustainable Development Goals")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/goal-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void createGoalType_ShouldReturn400_WhenNameIsEmpty() throws Exception {
        // Given
        GoalTypeCreateRequest request = GoalTypeCreateRequest.builder()
                .name("")
                .description("Sustainable Development Goals")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/goal-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateGoalType_ShouldUpdateGoalType_WhenValidData() throws Exception {
        // Given
        GoalTypeUpdateRequest request = GoalTypeUpdateRequest.builder()
                .name("New Name")
                .description("New Description")
                .build();

        // When & Then
        mockMvc.perform(put("/api/v1/goal-types/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
} 