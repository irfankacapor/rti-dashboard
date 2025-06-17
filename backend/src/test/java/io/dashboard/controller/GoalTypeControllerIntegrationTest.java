package io.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dashboard.dto.goal.GoalTypeCreateRequest;
import io.dashboard.dto.goal.GoalTypeUpdateRequest;
import io.dashboard.entity.GoalType;
import io.dashboard.repository.GoalTypeRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GoalTypeControllerIntegrationTest {
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private GoalTypeRepository goalTypeRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private MockMvc mockMvc;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        goalTypeRepository.deleteAll();
    }
    
    @Test
    void getAllGoalTypes_shouldReturnEmptyList_whenNoGoalTypes() throws Exception {
        mockMvc.perform(get("/api/v1/goal-types"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
    
    @Test
    void getAllGoalTypes_shouldReturnGoalTypes_whenGoalTypesExist() throws Exception {
        // Given
        GoalType goalType1 = GoalType.builder()
                .name("SDG Goals")
                .description("Sustainable Development Goals")
                .build();
        
        GoalType goalType2 = GoalType.builder()
                .name("Local Policy")
                .description("Local government policy goals")
                .build();
        
        goalTypeRepository.save(goalType1);
        goalTypeRepository.save(goalType2);
        
        // When & Then
        mockMvc.perform(get("/api/v1/goal-types"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("SDG Goals"))
                .andExpect(jsonPath("$[0].description").value("Sustainable Development Goals"))
                .andExpect(jsonPath("$[0].goalCount").value(0))
                .andExpect(jsonPath("$[1].name").value("Local Policy"))
                .andExpect(jsonPath("$[1].description").value("Local government policy goals"))
                .andExpect(jsonPath("$[1].goalCount").value(0));
    }
    
    @Test
    void getGoalTypeById_shouldReturnGoalType_whenExists() throws Exception {
        // Given
        GoalType goalType = GoalType.builder()
                .name("SDG Goals")
                .description("Sustainable Development Goals")
                .build();
        
        GoalType savedGoalType = goalTypeRepository.save(goalType);
        
        // When & Then
        mockMvc.perform(get("/api/v1/goal-types/{id}", savedGoalType.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedGoalType.getId()))
                .andExpect(jsonPath("$.name").value("SDG Goals"))
                .andExpect(jsonPath("$.description").value("Sustainable Development Goals"))
                .andExpect(jsonPath("$.goalCount").value(0));
    }
    
    @Test
    void getGoalTypeById_shouldReturn404_whenNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/goal-types/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Goal type not found with ID: 999"));
    }
    
    @Test
    void createGoalType_shouldCreateGoalType_whenValidRequest() throws Exception {
        // Given
        GoalTypeCreateRequest request = GoalTypeCreateRequest.builder()
                .name("Test Goal Type")
                .description("Test description")
                .build();
        
        // When & Then
        mockMvc.perform(post("/api/v1/goal-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test Goal Type"))
                .andExpect(jsonPath("$.description").value("Test description"))
                .andExpect(jsonPath("$.goalCount").value(0));
        
        // Verify it was saved
        assertTrue(goalTypeRepository.existsByName("Test Goal Type"));
    }
    
    @Test
    void createGoalType_shouldReturn400_whenNameIsEmpty() throws Exception {
        // Given
        GoalTypeCreateRequest request = GoalTypeCreateRequest.builder()
                .name("")
                .description("Test description")
                .build();
        
        // When & Then
        mockMvc.perform(post("/api/v1/goal-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed: {name=Goal type name is required}"));
    }
    
    @Test
    void createGoalType_shouldReturn400_whenNameAlreadyExists() throws Exception {
        // Given
        GoalType existingGoalType = GoalType.builder()
                .name("Existing Goal Type")
                .description("Existing description")
                .build();
        goalTypeRepository.save(existingGoalType);
        
        GoalTypeCreateRequest request = GoalTypeCreateRequest.builder()
                .name("Existing Goal Type")
                .description("New description")
                .build();
        
        // When & Then
        mockMvc.perform(post("/api/v1/goal-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Goal type with name 'Existing Goal Type' already exists"));
    }
    
    @Test
    void updateGoalType_shouldUpdateGoalType_whenValidRequest() throws Exception {
        // Given
        GoalType goalType = GoalType.builder()
                .name("Original Name")
                .description("Original description")
                .build();
        
        GoalType savedGoalType = goalTypeRepository.save(goalType);
        
        GoalTypeUpdateRequest request = GoalTypeUpdateRequest.builder()
                .name("Updated Name")
                .description("Updated description")
                .build();
        
        // When & Then
        mockMvc.perform(put("/api/v1/goal-types/{id}", savedGoalType.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.description").value("Updated description"));
        
        // Verify it was updated
        GoalType updatedGoalType = goalTypeRepository.findById(savedGoalType.getId()).orElse(null);
        assertNotNull(updatedGoalType);
        assertEquals("Updated Name", updatedGoalType.getName());
        assertEquals("Updated description", updatedGoalType.getDescription());
    }
    
    @Test
    void updateGoalType_shouldReturn404_whenNotFound() throws Exception {
        // Given
        GoalTypeUpdateRequest request = GoalTypeUpdateRequest.builder()
                .name("Updated Name")
                .description("Updated description")
                .build();
        
        // When & Then
        mockMvc.perform(put("/api/v1/goal-types/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Goal type not found with ID: 999"));
    }
    
    @Test
    void updateGoalType_shouldReturn400_whenNameAlreadyExists() throws Exception {
        // Given
        GoalType goalType1 = GoalType.builder()
                .name("Goal Type 1")
                .description("Description 1")
                .build();
        
        GoalType goalType2 = GoalType.builder()
                .name("Goal Type 2")
                .description("Description 2")
                .build();
        
        GoalType savedGoalType1 = goalTypeRepository.save(goalType1);
        goalTypeRepository.save(goalType2);
        
        GoalTypeUpdateRequest request = GoalTypeUpdateRequest.builder()
                .name("Goal Type 2")
                .description("Updated description")
                .build();
        
        // When & Then
        mockMvc.perform(put("/api/v1/goal-types/{id}", savedGoalType1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Goal type with name 'Goal Type 2' already exists"));
    }
    
    @Test
    void deleteGoalType_shouldDeleteGoalType_whenNoGoals() throws Exception {
        // Given
        GoalType goalType = GoalType.builder()
                .name("To Delete")
                .description("Will be deleted")
                .build();
        
        GoalType savedGoalType = goalTypeRepository.save(goalType);
        
        // When & Then
        mockMvc.perform(delete("/api/v1/goal-types/{id}", savedGoalType.getId()))
                .andExpect(status().isNoContent());
        
        // Verify it was deleted
        assertFalse(goalTypeRepository.existsById(savedGoalType.getId()));
    }
    
    @Test
    void deleteGoalType_shouldReturn404_whenNotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/goal-types/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Goal type not found with ID: 999"));
    }
} 