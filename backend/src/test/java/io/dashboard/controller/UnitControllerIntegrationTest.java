package io.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dashboard.dto.UnitCreateRequest;
import io.dashboard.dto.UnitUpdateRequest;
import io.dashboard.model.Indicator;
import io.dashboard.model.Unit;
import io.dashboard.repository.IndicatorRepository;
import io.dashboard.repository.UnitRepository;
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
@Transactional
@WithMockAdmin
class UnitControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UnitRepository unitRepository;
    @Autowired
    private IndicatorRepository indicatorRepository;

    @BeforeEach
    void setup() {
        indicatorRepository.deleteAll();
        unitRepository.deleteAll();
    }

    @Test
    void createUnit_shouldSucceed() throws Exception {
        UnitCreateRequest req = new UnitCreateRequest();
        req.setCode("UNIT1");
        req.setDescription("Test Unit");
        
        mockMvc.perform(post("/api/v1/units")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("UNIT1"))
                .andExpect(jsonPath("$.description").value("Test Unit"));
        
        assertThat(unitRepository.findByCode("UNIT1")).isPresent();
    }

    @Test
    void createUnit_shouldFail_duplicateCode() throws Exception {
        Unit unit = new Unit();
        unit.setCode("DUP");
        unit.setDescription("Unit");
        unitRepository.save(unit);
        
        UnitCreateRequest req = new UnitCreateRequest();
        req.setCode("DUP");
        req.setDescription("Another");
        
        mockMvc.perform(post("/api/v1/units")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getAllUnits_shouldReturnList() throws Exception {
        Unit unit = new Unit();
        unit.setCode("UNIT1");
        unit.setDescription("Test Unit");
        unitRepository.save(unit);
        
        mockMvc.perform(get("/api/v1/units"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("UNIT1"));
    }

    @Test
    void getUnitById_shouldReturnUnit() throws Exception {
        Unit unit = new Unit();
        unit.setCode("UNIT1");
        unit.setDescription("Test Unit");
        unit = unitRepository.save(unit);
        
        mockMvc.perform(get("/api/v1/units/" + unit.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("UNIT1"));
    }

    @Test
    void getUnitById_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/units/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUnit_shouldUpdateFields() throws Exception {
        Unit unit = new Unit();
        unit.setCode("UNIT1");
        unit.setDescription("Old");
        unit = unitRepository.save(unit);
        
        UnitUpdateRequest req = new UnitUpdateRequest();
        req.setDescription("Updated");
        
        mockMvc.perform(put("/api/v1/units/" + unit.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated"));
    }

    @Test
    void updateUnit_shouldReturn404_whenNotFound() throws Exception {
        UnitUpdateRequest req = new UnitUpdateRequest();
        req.setDescription("desc");
        
        mockMvc.perform(put("/api/v1/units/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUnit_shouldDelete_whenNoIndicators() throws Exception {
        Unit unit = new Unit();
        unit.setCode("UNIT1");
        unit.setDescription("Test Unit");
        unit = unitRepository.save(unit);
        
        mockMvc.perform(delete("/api/v1/units/" + unit.getId()))
                .andExpect(status().isNoContent());
        
        assertThat(unitRepository.findById(unit.getId())).isEmpty();
    }
} 