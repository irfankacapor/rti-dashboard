package io.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dashboard.dto.goal.*;
import io.dashboard.entity.Goal;
import io.dashboard.entity.GoalIndicator;
import io.dashboard.entity.GoalType;
import io.dashboard.enums.ImpactDirection;
import io.dashboard.model.Indicator;
import io.dashboard.repository.GoalIndicatorRepository;
import io.dashboard.repository.GoalRepository;
import io.dashboard.repository.GoalTypeRepository;
import io.dashboard.repository.IndicatorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GoalIndicatorControllerIntegrationTest {
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private GoalIndicatorRepository goalIndicatorRepository;
    
    @Autowired
    private GoalRepository goalRepository;
    
    @Autowired
    private GoalTypeRepository goalTypeRepository;
    
    @Autowired
    private IndicatorRepository indicatorRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private MockMvc mockMvc;
    
    private Goal goal;
    private Indicator indicator;
    private GoalIndicator goalIndicator;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Create test data
        GoalType goalType = GoalType.builder()
                .name("Test Goal Type")
                .description("Test Goal Type Description")
                .build();
        goalType = goalTypeRepository.save(goalType);
        
        goal = Goal.builder()
                .name("Test Goal")
                .description("Test Goal Description")
                .goalType(goalType)
                .build();
        goal = goalRepository.save(goal);
        
        indicator = Indicator.builder()
                .name("Test Indicator")
                .build();
        indicator = indicatorRepository.save(indicator);
        
        goalIndicator = GoalIndicator.builder()
                .goal(goal)
                .indicator(indicator)
                .aggregationWeight(new BigDecimal("0.5"))
                .impactDirection(ImpactDirection.POSITIVE)
                .build();
        goalIndicator = goalIndicatorRepository.save(goalIndicator);
    }
    
    @Test
    void linkGoalToIndicator_shouldCreateRelationship_whenValidRequest() throws Exception {
        // Given
        GoalIndicatorLinkRequest request = GoalIndicatorLinkRequest.builder()
                .indicatorId(indicator.getId())
                .weight(new BigDecimal("0.3"))
                .impactDirection(ImpactDirection.NEGATIVE)
                .build();
        
        // When & Then
        mockMvc.perform(post("/api/v1/goals/{goalId}/indicators/{indicatorId}", goal.getId(), indicator.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.goalId").value(goal.getId()))
                .andExpect(jsonPath("$.indicatorId").value(indicator.getId()))
                .andExpect(jsonPath("$.aggregationWeight").value("0.3"))
                .andExpect(jsonPath("$.impactDirection").value("NEGATIVE"));
    }
    
    @Test
    void linkGoalToIndicator_shouldReturn400_whenInvalidWeight() throws Exception {
        // Given
        GoalIndicatorLinkRequest request = GoalIndicatorLinkRequest.builder()
                .indicatorId(indicator.getId())
                .weight(new BigDecimal("1.5"))
                .impactDirection(ImpactDirection.POSITIVE)
                .build();
        
        // When & Then
        mockMvc.perform(post("/api/v1/goals/{goalId}/indicators/{indicatorId}", goal.getId(), indicator.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void linkGoalToIndicator_shouldReturn409_whenRelationshipExists() throws Exception {
        // Given
        GoalIndicatorLinkRequest request = GoalIndicatorLinkRequest.builder()
                .indicatorId(indicator.getId())
                .weight(new BigDecimal("0.3"))
                .impactDirection(ImpactDirection.NEGATIVE)
                .build();
        
        // When & Then
        mockMvc.perform(post("/api/v1/goals/{goalId}/indicators/{indicatorId}", goal.getId(), indicator.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Goal is already linked to this indicator"));
    }
    
    @Test
    void unlinkGoalFromIndicator_shouldDeleteRelationship_whenExists() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/goals/{goalId}/indicators/{indicatorId}", goal.getId(), indicator.getId()))
                .andExpect(status().isNoContent());
        
        // Verify relationship is deleted
        assertFalse(goalIndicatorRepository.existsByGoalIdAndIndicatorId(goal.getId(), indicator.getId()));
    }
    
    @Test
    void unlinkGoalFromIndicator_shouldReturn404_whenRelationshipNotFound() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/goals/{goalId}/indicators/{indicatorId}", 999L, 999L))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void updateGoalIndicatorWeight_shouldUpdateWeight_whenValidRequest() throws Exception {
        // Given
        GoalIndicatorUpdateRequest request = GoalIndicatorUpdateRequest.builder()
                .weight(new BigDecimal("0.7"))
                .build();
        
        // When & Then
        mockMvc.perform(put("/api/v1/goals/{goalId}/indicators/{indicatorId}/weight", goal.getId(), indicator.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aggregationWeight").value("0.7"));
    }
    
    @Test
    void updateGoalIndicatorWeight_shouldReturn400_whenInvalidWeight() throws Exception {
        // Given
        GoalIndicatorUpdateRequest request = GoalIndicatorUpdateRequest.builder()
                .weight(new BigDecimal("1.5"))
                .build();
        
        // When & Then
        mockMvc.perform(put("/api/v1/goals/{goalId}/indicators/{indicatorId}/weight", goal.getId(), indicator.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void updateImpactDirection_shouldUpdateImpactDirection_whenValidRequest() throws Exception {
        // Given
        GoalIndicatorUpdateRequest request = GoalIndicatorUpdateRequest.builder()
                .impactDirection(ImpactDirection.NEGATIVE)
                .build();
        
        // When & Then
        mockMvc.perform(put("/api/v1/goals/{goalId}/indicators/{indicatorId}/impact", goal.getId(), indicator.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.impactDirection").value("NEGATIVE"));
    }
    
    @Test
    void getIndicatorsByGoal_shouldReturnIndicators_whenGoalExists() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/goals/{goalId}/indicators", goal.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].goalId").value(goal.getId()))
                .andExpect(jsonPath("$[0].indicatorId").value(indicator.getId()))
                .andExpect(jsonPath("$[0].aggregationWeight").value("0.5"))
                .andExpect(jsonPath("$[0].impactDirection").value("POSITIVE"));
    }
    
    @Test
    void getIndicatorsByGoal_shouldReturn404_whenGoalNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/goals/{goalId}/indicators", 999L))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void getGoalsByIndicator_shouldReturnGoals_whenIndicatorExists() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/indicators/{indicatorId}/goals", indicator.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].goalId").value(goal.getId()))
                .andExpect(jsonPath("$[0].indicatorId").value(indicator.getId()));
    }
    
    @Test
    void getGoalsByIndicator_shouldReturn404_whenIndicatorNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/indicators/{indicatorId}/goals", 999L))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void bulkLinkIndicators_shouldCreateMultipleRelationships_whenValidRequest() throws Exception {
        // Given
        Indicator indicator2 = Indicator.builder().name("Test Indicator 2").build();
        indicator2 = indicatorRepository.save(indicator2);
        
        GoalIndicatorLinkRequest request1 = GoalIndicatorLinkRequest.builder()
                .indicatorId(indicator2.getId())
                .weight(new BigDecimal("0.3"))
                .impactDirection(ImpactDirection.NEGATIVE)
                .build();
        
        GoalIndicatorLinkRequest request2 = GoalIndicatorLinkRequest.builder()
                .indicatorId(indicator.getId())
                .weight(new BigDecimal("0.2"))
                .impactDirection(ImpactDirection.POSITIVE)
                .build();
        
        BulkGoalIndicatorRequest bulkRequest = BulkGoalIndicatorRequest.builder()
                .indicators(Arrays.asList(request1, request2))
                .build();
        
        // When & Then
        mockMvc.perform(post("/api/v1/goals/{goalId}/indicators/bulk", goal.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bulkRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }
    
    @Test
    void bulkLinkIndicators_shouldReturn400_whenDuplicateIndicatorIds() throws Exception {
        // Given
        GoalIndicatorLinkRequest request1 = GoalIndicatorLinkRequest.builder()
                .indicatorId(indicator.getId())
                .weight(new BigDecimal("0.3"))
                .impactDirection(ImpactDirection.NEGATIVE)
                .build();
        
        GoalIndicatorLinkRequest request2 = GoalIndicatorLinkRequest.builder()
                .indicatorId(indicator.getId())
                .weight(new BigDecimal("0.2"))
                .impactDirection(ImpactDirection.POSITIVE)
                .build();
        
        BulkGoalIndicatorRequest bulkRequest = BulkGoalIndicatorRequest.builder()
                .indicators(Arrays.asList(request1, request2))
                .build();
        
        // When & Then
        mockMvc.perform(post("/api/v1/goals/{goalId}/indicators/bulk", goal.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bulkRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Duplicate indicator ID in request: " + indicator.getId()));
    }
    
    @Test
    void getGoalProgress_shouldReturnProgress_whenGoalExists() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/goals/{goalId}/progress", goal.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goalId").value(goal.getId()))
                .andExpect(jsonPath("$.goalName").value("Test Goal"))
                .andExpect(jsonPath("$.totalIndicators").value(1))
                .andExpect(jsonPath("$.indicatorsWithTargets").value(0))
                .andExpect(jsonPath("$.overallProgress").value("0.0000"));
    }
    
    @Test
    void getGoalProgress_shouldReturn404_whenGoalNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/goals/{goalId}/progress", 999L))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void validateGoalIndicatorLink_shouldReturnTrue_whenRelationshipExists() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/goals/{goalId}/indicators/{indicatorId}", goal.getId(), indicator.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
    
    @Test
    void validateGoalIndicatorLink_shouldReturnFalse_whenRelationshipNotExists() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/goals/{goalId}/indicators/{indicatorId}", 999L, 999L))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
} 