package io.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dashboard.dto.BulkGoalIndicatorRequest;
import io.dashboard.dto.GoalIndicatorLinkRequest;
import io.dashboard.dto.GoalIndicatorUpdateRequest;
import io.dashboard.model.Goal;
import io.dashboard.model.GoalIndicator;
import io.dashboard.model.GoalType;
import io.dashboard.model.ImpactDirection;
import io.dashboard.model.Indicator;
import io.dashboard.model.Unit;
import io.dashboard.service.GoalIndicatorService;
import io.dashboard.service.GoalService;
import io.dashboard.service.IndicatorService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({GoalIndicatorController.class, IndicatorGoalController.class})
class GoalIndicatorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GoalIndicatorService goalIndicatorService;

    @MockBean
    private GoalService goalService;

    @MockBean
    private IndicatorService indicatorService;

    private Goal goal;
    private Indicator indicator;
    private GoalType goalType;
    private Unit unit;
    private GoalIndicator goalIndicator;

    @BeforeEach
    void setUp() {
        goalType = new GoalType();
        goalType.setId(1L);
        goalType.setName("SDGs");

        goal = new Goal();
        goal.setId(1L);
        goal.setName("Test Goal");
        goal.setGoalType(goalType);

        unit = new Unit();
        unit.setId(1L);
        unit.setCode("PERCENT");

        indicator = new Indicator();
        indicator.setId(1L);
        indicator.setName("Test Indicator");
        indicator.setCode("TEST_IND");
        indicator.setUnit(unit);

        goalIndicator = new GoalIndicator();
        goalIndicator.setGoal(goal);
        goalIndicator.setIndicator(indicator);
        goalIndicator.setAggregationWeight(0.5);
        goalIndicator.setImpactDirection(ImpactDirection.POSITIVE);
        goalIndicator.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void linkGoalToIndicator_shouldSucceed() throws Exception {
        // Given
        GoalIndicatorLinkRequest request = new GoalIndicatorLinkRequest();
        request.setIndicatorId(1L);
        request.setAggregationWeight(0.5);
        request.setImpactDirection(ImpactDirection.POSITIVE);

        when(goalIndicatorService.linkGoalToIndicator(eq(1L), eq(1L), eq(0.5), eq(ImpactDirection.POSITIVE)))
                .thenReturn(createGoalIndicatorResponse());

        // When & Then
        mockMvc.perform(post("/api/v1/goals/1/indicators/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.goalId").value(1))
                .andExpect(jsonPath("$.indicatorId").value(1))
                .andExpect(jsonPath("$.aggregationWeight").value(0.5))
                .andExpect(jsonPath("$.impactDirection").value("POSITIVE"));
    }

    @Test
    void linkGoalToIndicator_shouldFail_whenInvalidRequest() throws Exception {
        // Given
        GoalIndicatorLinkRequest request = new GoalIndicatorLinkRequest();
        request.setIndicatorId(1L);
        request.setAggregationWeight(1.5); // Invalid weight > 1.0
        request.setImpactDirection(ImpactDirection.POSITIVE);

        // When & Then
        mockMvc.perform(post("/api/v1/goals/1/indicators/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unlinkGoalFromIndicator_shouldSucceed() throws Exception {
        // Given
        doNothing().when(goalIndicatorService).unlinkGoalFromIndicator(1L, 1L);

        // When & Then
        mockMvc.perform(delete("/api/v1/goals/1/indicators/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateGoalIndicatorWeight_shouldSucceed() throws Exception {
        // Given
        GoalIndicatorUpdateRequest request = new GoalIndicatorUpdateRequest();
        request.setAggregationWeight(0.7);
        request.setImpactDirection(ImpactDirection.POSITIVE);

        when(goalIndicatorService.updateGoalIndicatorWeight(eq(1L), eq(1L), eq(0.7)))
                .thenReturn(createGoalIndicatorResponse());

        // When & Then
        mockMvc.perform(put("/api/v1/goals/1/indicators/1/weight")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aggregationWeight").value(0.5));
    }

    @Test
    void updateGoalIndicatorDirection_shouldSucceed() throws Exception {
        // Given
        GoalIndicatorUpdateRequest request = new GoalIndicatorUpdateRequest();
        request.setAggregationWeight(0.5);
        request.setImpactDirection(ImpactDirection.NEGATIVE);

        when(goalIndicatorService.updateGoalIndicatorDirection(eq(1L), eq(1L), eq(ImpactDirection.NEGATIVE)))
                .thenReturn(createGoalIndicatorResponse());

        // When & Then
        mockMvc.perform(put("/api/v1/goals/1/indicators/1/direction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.impactDirection").value("POSITIVE"));
    }

    @Test
    void getIndicatorsByGoal_shouldReturnList() throws Exception {
        // Given
        List<io.dashboard.dto.GoalIndicatorResponse> responses = Arrays.asList(createGoalIndicatorResponse());
        when(goalIndicatorService.findIndicatorsByGoal(1L)).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/api/v1/goals/1/indicators"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].goalId").value(1))
                .andExpect(jsonPath("$[0].indicatorId").value(1));
    }

    @Test
    void getGoalsByIndicator_shouldReturnList() throws Exception {
        // Given
        List<io.dashboard.dto.GoalIndicatorResponse> responses = Arrays.asList(createGoalIndicatorResponse());
        when(goalIndicatorService.findGoalsByIndicator(1L)).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/api/v1/indicators/1/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].goalId").value(1))
                .andExpect(jsonPath("$[0].indicatorId").value(1));
    }

    @Test
    void bulkLinkIndicators_shouldSucceed() throws Exception {
        // Given
        GoalIndicatorLinkRequest request1 = new GoalIndicatorLinkRequest();
        request1.setIndicatorId(1L);
        request1.setAggregationWeight(0.6);
        request1.setImpactDirection(ImpactDirection.POSITIVE);

        GoalIndicatorLinkRequest request2 = new GoalIndicatorLinkRequest();
        request2.setIndicatorId(2L);
        request2.setAggregationWeight(0.4);
        request2.setImpactDirection(ImpactDirection.NEGATIVE);

        BulkGoalIndicatorRequest bulkRequest = new BulkGoalIndicatorRequest();
        bulkRequest.setLinks(Arrays.asList(request1, request2));

        List<io.dashboard.dto.GoalIndicatorResponse> responses = Arrays.asList(
                createGoalIndicatorResponse(), createGoalIndicatorResponse());
        when(goalIndicatorService.bulkLinkIndicators(eq(1L), anyList())).thenReturn(responses);

        // When & Then
        mockMvc.perform(post("/api/v1/goals/1/indicators/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bulkRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].goalId").value(1))
                .andExpect(jsonPath("$[1].goalId").value(1));
    }

    @Test
    void bulkLinkIndicators_shouldFail_whenEmptyList() throws Exception {
        // Given
        BulkGoalIndicatorRequest bulkRequest = new BulkGoalIndicatorRequest();
        bulkRequest.setLinks(Arrays.asList());

        // When & Then
        mockMvc.perform(post("/api/v1/goals/1/indicators/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bulkRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getGoalProgress_shouldReturnProgress() throws Exception {
        // Given
        io.dashboard.dto.GoalProgressResponse progressResponse = new io.dashboard.dto.GoalProgressResponse();
        progressResponse.setGoalId(1L);
        progressResponse.setGoalName("Test Goal");
        progressResponse.setOverallProgress(75.0);
        progressResponse.setTotalWeight(1.0);
        progressResponse.setProgressStatus("ON_TRACK");

        when(goalIndicatorService.calculateGoalProgress(1L)).thenReturn(progressResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/goals/1/progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goalId").value(1))
                .andExpect(jsonPath("$.goalName").value("Test Goal"))
                .andExpect(jsonPath("$.overallProgress").value(75.0))
                .andExpect(jsonPath("$.totalWeight").value(1.0))
                .andExpect(jsonPath("$.progressStatus").value("ON_TRACK"));
    }

    @Test
    void linkGoalToIndicator_shouldFail_whenNegativeWeight() throws Exception {
        // Given
        GoalIndicatorLinkRequest request = new GoalIndicatorLinkRequest();
        request.setIndicatorId(1L);
        request.setAggregationWeight(-0.1); // Invalid negative weight
        request.setImpactDirection(ImpactDirection.POSITIVE);

        // When & Then
        mockMvc.perform(post("/api/v1/goals/1/indicators/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void linkGoalToIndicator_shouldFail_whenMissingRequiredFields() throws Exception {
        // Given
        GoalIndicatorLinkRequest request = new GoalIndicatorLinkRequest();
        request.setIndicatorId(1L);
        // Missing aggregationWeight and impactDirection

        // When & Then
        mockMvc.perform(post("/api/v1/goals/1/indicators/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateGoalIndicatorWeight_shouldFail_whenInvalidWeight() throws Exception {
        // Given
        GoalIndicatorUpdateRequest request = new GoalIndicatorUpdateRequest();
        request.setAggregationWeight(1.5); // Invalid weight > 1.0
        request.setImpactDirection(ImpactDirection.POSITIVE);

        // When & Then
        mockMvc.perform(put("/api/v1/goals/1/indicators/1/weight")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private io.dashboard.dto.GoalIndicatorResponse createGoalIndicatorResponse() {
        io.dashboard.dto.GoalIndicatorResponse response = new io.dashboard.dto.GoalIndicatorResponse();
        response.setGoalId(1L);
        response.setGoalName("Test Goal");
        response.setIndicatorId(1L);
        response.setIndicatorName("Test Indicator");
        response.setIndicatorCode("TEST_IND");
        response.setAggregationWeight(0.5);
        response.setImpactDirection(ImpactDirection.POSITIVE);
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }
} 