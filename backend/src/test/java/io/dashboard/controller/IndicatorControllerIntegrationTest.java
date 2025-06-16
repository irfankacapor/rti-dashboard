package io.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dashboard.dto.IndicatorCreateRequest;
import io.dashboard.dto.IndicatorUpdateRequest;
import io.dashboard.dto.SubareaIndicatorRequest;
import io.dashboard.model.DataType;
import io.dashboard.model.Direction;
import io.dashboard.model.Indicator;
import io.dashboard.model.Subarea;
import io.dashboard.model.Unit;
import io.dashboard.model.Area;
import io.dashboard.repository.DataTypeRepository;
import io.dashboard.repository.IndicatorRepository;
import io.dashboard.repository.SubareaIndicatorRepository;
import io.dashboard.repository.SubareaRepository;
import io.dashboard.repository.UnitRepository;
import io.dashboard.repository.AreaRepository;
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
class IndicatorControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private IndicatorRepository indicatorRepository;
    @Autowired
    private UnitRepository unitRepository;
    @Autowired
    private DataTypeRepository dataTypeRepository;
    @Autowired
    private SubareaRepository subareaRepository;
    @Autowired
    private SubareaIndicatorRepository subareaIndicatorRepository;
    @Autowired
    private AreaRepository areaRepository;

    private Unit unit;
    private DataType dataType;
    private Subarea subarea;

    @BeforeEach
    void setup() {
        subareaIndicatorRepository.deleteAll();
        indicatorRepository.deleteAll();
        unitRepository.deleteAll();
        dataTypeRepository.deleteAll();
        subareaRepository.deleteAll();
        
        // Create test data
        unit = new Unit();
        unit.setCode("UNIT1");
        unit.setDescription("Test Unit");
        unit = unitRepository.save(unit);
        
        dataType = new DataType();
        dataType.setName("TYPE1");
        dataType = dataTypeRepository.save(dataType);
        
        // Create a subarea for testing relationships
        Area area = new Area();
        area.setCode("AREA1");
        area.setName("Test Area");
        area = areaRepository.save(area);
        
        subarea = new Subarea();
        subarea.setCode("SUB1");
        subarea.setName("Test Subarea");
        subarea.setArea(area);
        subarea = subareaRepository.save(subarea);
    }

    @Test
    void createIndicator_shouldSucceed() throws Exception {
        IndicatorCreateRequest req = new IndicatorCreateRequest();
        req.setCode("IND1");
        req.setName("Indicator 1");
        req.setIsComposite(false);
        req.setUnitId(unit.getId());
        req.setDataTypeId(dataType.getId());
        
        mockMvc.perform(post("/api/v1/indicators")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("IND1"))
                .andExpect(jsonPath("$.unit.id").value(unit.getId()))
                .andExpect(jsonPath("$.dataType.id").value(dataType.getId()));
        
        assertThat(indicatorRepository.findByCode("IND1")).isPresent();
    }

    @Test
    void createIndicator_shouldFail_duplicateCode() throws Exception {
        Indicator indicator = new Indicator();
        indicator.setCode("DUP");
        indicator.setName("Indicator");
        indicator.setIsComposite(false);
        indicatorRepository.save(indicator);
        
        IndicatorCreateRequest req = new IndicatorCreateRequest();
        req.setCode("DUP");
        req.setName("Another");
        req.setIsComposite(false);
        
        mockMvc.perform(post("/api/v1/indicators")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void createIndicator_shouldFail_invalidUnit() throws Exception {
        IndicatorCreateRequest req = new IndicatorCreateRequest();
        req.setCode("IND1");
        req.setName("Indicator 1");
        req.setUnitId(9999L);
        
        mockMvc.perform(post("/api/v1/indicators")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getAllIndicators_shouldReturnList() throws Exception {
        Indicator indicator = new Indicator();
        indicator.setCode("IND1");
        indicator.setName("Indicator 1");
        indicator.setIsComposite(false);
        indicatorRepository.save(indicator);
        
        mockMvc.perform(get("/api/v1/indicators"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("IND1"));
    }

    @Test
    void getIndicatorById_shouldReturnIndicator() throws Exception {
        Indicator indicator = new Indicator();
        indicator.setCode("IND1");
        indicator.setName("Indicator 1");
        indicator.setIsComposite(false);
        indicator = indicatorRepository.save(indicator);
        
        mockMvc.perform(get("/api/v1/indicators/" + indicator.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("IND1"));
    }

    @Test
    void getIndicatorById_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/indicators/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getIndicatorsBySubarea_shouldReturnList() throws Exception {
        Indicator indicator = new Indicator();
        indicator.setCode("IND1");
        indicator.setName("Indicator 1");
        indicator.setIsComposite(false);
        indicator = indicatorRepository.save(indicator);
        
        // Create SubareaIndicator relationship
        io.dashboard.model.SubareaIndicator si = new io.dashboard.model.SubareaIndicator();
        io.dashboard.model.SubareaIndicator.SubareaIndicatorId id = new io.dashboard.model.SubareaIndicator.SubareaIndicatorId();
        id.setSubareaId(subarea.getId());
        id.setIndicatorId(indicator.getId());
        si.setId(id);
        si.setSubarea(subarea);
        si.setIndicator(indicator);
        si.setDirection(Direction.INPUT);
        subareaIndicatorRepository.save(si);
        
        mockMvc.perform(get("/api/v1/subareas/" + subarea.getId() + "/indicators"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("IND1"));
    }

    @Test
    void updateIndicator_shouldUpdateFields() throws Exception {
        Indicator indicator = new Indicator();
        indicator.setCode("IND1");
        indicator.setName("Old");
        indicator.setIsComposite(false);
        indicator = indicatorRepository.save(indicator);
        
        IndicatorUpdateRequest req = new IndicatorUpdateRequest();
        req.setName("Updated");
        req.setIsComposite(true);
        req.setUnitId(unit.getId());
        
        mockMvc.perform(put("/api/v1/indicators/" + indicator.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.isComposite").value(true));
    }

    @Test
    void updateIndicator_shouldReturn404_whenNotFound() throws Exception {
        IndicatorUpdateRequest req = new IndicatorUpdateRequest();
        req.setName("Name");
        req.setIsComposite(false);
        
        mockMvc.perform(put("/api/v1/indicators/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteIndicator_shouldDelete() throws Exception {
        Indicator indicator = new Indicator();
        indicator.setCode("IND1");
        indicator.setName("Indicator 1");
        indicator.setIsComposite(false);
        indicator = indicatorRepository.save(indicator);
        
        mockMvc.perform(delete("/api/v1/indicators/" + indicator.getId()))
                .andExpect(status().isNoContent());
        
        assertThat(indicatorRepository.findById(indicator.getId())).isEmpty();
    }

    @Test
    void assignIndicatorToSubarea_shouldSucceed() throws Exception {
        Indicator indicator = new Indicator();
        indicator.setCode("IND1");
        indicator.setName("Indicator 1");
        indicator.setIsComposite(false);
        indicator = indicatorRepository.save(indicator);
        
        SubareaIndicatorRequest req = new SubareaIndicatorRequest();
        req.setDirection(Direction.INPUT);
        req.setAggregationWeight(0.5);
        
        mockMvc.perform(post("/api/v1/indicators/" + indicator.getId() + "/subareas/" + subarea.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
        
        assertThat(subareaIndicatorRepository.existsBySubareaIdAndIndicatorId(subarea.getId(), indicator.getId())).isTrue();
    }

    @Test
    void assignIndicatorToSubarea_shouldFail_alreadyAssigned() throws Exception {
        Indicator indicator = new Indicator();
        indicator.setCode("IND1");
        indicator.setName("Indicator 1");
        indicator.setIsComposite(false);
        indicator = indicatorRepository.save(indicator);
        
        // Create initial assignment
        io.dashboard.model.SubareaIndicator si = new io.dashboard.model.SubareaIndicator();
        io.dashboard.model.SubareaIndicator.SubareaIndicatorId id = new io.dashboard.model.SubareaIndicator.SubareaIndicatorId();
        id.setSubareaId(subarea.getId());
        id.setIndicatorId(indicator.getId());
        si.setId(id);
        si.setSubarea(subarea);
        si.setIndicator(indicator);
        si.setDirection(Direction.INPUT);
        subareaIndicatorRepository.save(si);
        
        SubareaIndicatorRequest req = new SubareaIndicatorRequest();
        req.setDirection(Direction.OUTPUT);
        
        mockMvc.perform(post("/api/v1/indicators/" + indicator.getId() + "/subareas/" + subarea.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void removeIndicatorFromSubarea_shouldSucceed() throws Exception {
        Indicator indicator = new Indicator();
        indicator.setCode("IND1");
        indicator.setName("Indicator 1");
        indicator.setIsComposite(false);
        indicator = indicatorRepository.save(indicator);
        
        // Create assignment
        io.dashboard.model.SubareaIndicator si = new io.dashboard.model.SubareaIndicator();
        io.dashboard.model.SubareaIndicator.SubareaIndicatorId id = new io.dashboard.model.SubareaIndicator.SubareaIndicatorId();
        id.setSubareaId(subarea.getId());
        id.setIndicatorId(indicator.getId());
        si.setId(id);
        si.setSubarea(subarea);
        si.setIndicator(indicator);
        si.setDirection(Direction.INPUT);
        subareaIndicatorRepository.save(si);
        
        mockMvc.perform(delete("/api/v1/indicators/" + indicator.getId() + "/subareas/" + subarea.getId()))
                .andExpect(status().isNoContent());
        
        assertThat(subareaIndicatorRepository.existsBySubareaIdAndIndicatorId(subarea.getId(), indicator.getId())).isFalse();
    }

    @Test
    void removeIndicatorFromSubarea_shouldReturn404_whenNotAssigned() throws Exception {
        Indicator indicator = new Indicator();
        indicator.setCode("IND1");
        indicator.setName("Indicator 1");
        indicator.setIsComposite(false);
        indicator = indicatorRepository.save(indicator);
        
        mockMvc.perform(delete("/api/v1/indicators/" + indicator.getId() + "/subareas/" + subarea.getId()))
                .andExpect(status().isNotFound());
    }
} 