package io.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dashboard.dto.DataTypeCreateRequest;
import io.dashboard.dto.DataTypeUpdateRequest;
import io.dashboard.model.DataType;
import io.dashboard.model.Indicator;
import io.dashboard.repository.DataTypeRepository;
import io.dashboard.repository.IndicatorRepository;
import io.dashboard.test.security.WithMockAdmin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockAdmin
@Transactional
class DataTypeControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DataTypeRepository dataTypeRepository;
    @Autowired
    private IndicatorRepository indicatorRepository;

    @BeforeEach
    void setup() {
        indicatorRepository.deleteAll();
        dataTypeRepository.deleteAll();
    }

    @Test
    void createDataType_shouldSucceed() throws Exception {
        DataTypeCreateRequest req = new DataTypeCreateRequest();
        req.setName("TYPE1");
        
        mockMvc.perform(post("/api/v1/data-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("TYPE1"));
        
        assertThat(dataTypeRepository.findAll().stream().anyMatch(dt -> dt.getName().equals("TYPE1"))).isTrue();
    }

    @Test
    void getAllDataTypes_shouldReturnList() throws Exception {
        DataType dataType = new DataType();
        dataType.setName("TYPE1");
        dataTypeRepository.save(dataType);
        
        mockMvc.perform(get("/api/v1/data-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("TYPE1"));
    }

    @Test
    void getDataTypeById_shouldReturnDataType() throws Exception {
        DataType dataType = new DataType();
        dataType.setName("TYPE1");
        dataType = dataTypeRepository.save(dataType);
        
        mockMvc.perform(get("/api/v1/data-types/" + dataType.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("TYPE1"));
    }

    @Test
    void getDataTypeById_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/data-types/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateDataType_shouldUpdateFields() throws Exception {
        DataType dataType = new DataType();
        dataType.setName("Old");
        dataType = dataTypeRepository.save(dataType);
        
        DataTypeUpdateRequest req = new DataTypeUpdateRequest();
        req.setName("Updated");
        
        mockMvc.perform(put("/api/v1/data-types/" + dataType.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void updateDataType_shouldReturn404_whenNotFound() throws Exception {
        DataTypeUpdateRequest req = new DataTypeUpdateRequest();
        req.setName("Name");
        
        mockMvc.perform(put("/api/v1/data-types/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteDataType_shouldDelete_whenNoIndicators() throws Exception {
        DataType dataType = new DataType();
        dataType.setName("TYPE1");
        dataType = dataTypeRepository.save(dataType);
        
        mockMvc.perform(delete("/api/v1/data-types/" + dataType.getId()))
                .andExpect(status().isNoContent());
        
        assertThat(dataTypeRepository.findById(dataType.getId())).isEmpty();
    }

    @Test
    void deleteDataType_shouldReturn400_whenHasIndicators() throws Exception {
        DataType dataType = new DataType();
        dataType.setName("TYPE1");
        dataType = dataTypeRepository.save(dataType);
        
        // Add an indicator
        Indicator indicator = new Indicator();
        indicator.setCode("IND1");
        indicator.setName("Indicator 1");
        indicator.setIsComposite(false);
        indicator.setDataType(dataType);
        indicatorRepository.save(indicator);
        
        mockMvc.perform(delete("/api/v1/data-types/" + dataType.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
} 