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
import io.dashboard.model.DimTime;
import io.dashboard.model.DimLocation;
import io.dashboard.model.DimGeneric;
import io.dashboard.model.FactIndicatorValue;
import io.dashboard.repository.DataTypeRepository;
import io.dashboard.repository.IndicatorRepository;
import io.dashboard.repository.SubareaIndicatorRepository;
import io.dashboard.repository.SubareaRepository;
import io.dashboard.repository.UnitRepository;
import io.dashboard.repository.AreaRepository;
import io.dashboard.repository.FactIndicatorValueRepository;
import io.dashboard.repository.DimTimeRepository;
import io.dashboard.repository.DimLocationRepository;
import io.dashboard.repository.DimGenericRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

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
    @Autowired
    private FactIndicatorValueRepository factIndicatorValueRepository;
    @Autowired
    private DimTimeRepository dimTimeRepository;
    @Autowired
    private DimLocationRepository dimLocationRepository;
    @Autowired
    private DimGenericRepository dimGenericRepository;

    private Unit unit;
    private DataType dataType;
    private Subarea subarea;
    private Area area;
    private DimTime dimTime;
    private DimLocation dimLocation;
    private DimGeneric dimGeneric;
    
    // Use atomic counter to ensure unique codes across tests
    private static final AtomicLong testCounter = new AtomicLong(1);

    @BeforeEach
    void setup() {
        // Clean up all data in reverse dependency order
        factIndicatorValueRepository.deleteAll();
        subareaIndicatorRepository.deleteAll();
        indicatorRepository.deleteAll();
        subareaRepository.deleteAll();
        areaRepository.deleteAll();
        unitRepository.deleteAll();
        dataTypeRepository.deleteAll();
        dimTimeRepository.deleteAll();
        dimLocationRepository.deleteAll();
        dimGenericRepository.deleteAll();
        
        // Create test data with unique codes
        long counter = testCounter.getAndIncrement();
        
        unit = new Unit();
        unit.setCode("UNIT" + counter);
        unit.setDescription("Test Unit " + counter);
        unit = unitRepository.save(unit);
        
        dataType = new DataType();
        dataType.setName("TYPE" + counter);
        dataType = dataTypeRepository.save(dataType);
        
        // Create dimension tables to ensure they exist
        dimTime = new DimTime();
        dimTime.setYear(2024);
        dimTime.setMonth(1);
        dimTime.setDay(1);
        dimTime.setQuarter(1);
        dimTime.setValue("2024-01");
        dimTime = dimTimeRepository.save(dimTime);
        
        dimLocation = new DimLocation();
        dimLocation.setName("Test Location");
        dimLocation.setValue("Test Value");
        dimLocation.setType(DimLocation.LocationType.CITY);
        dimLocation.setLevel(2);
        dimLocation = dimLocationRepository.save(dimLocation);
        
        dimGeneric = new DimGeneric();
        dimGeneric.setName("Test Generic");
        dimGeneric.setDimensionName("Test Dimension");
        dimGeneric.setValue("Test Value");
        dimGeneric.setCategory("Test Category");
        dimGeneric = dimGenericRepository.save(dimGeneric);
        
        // Create a subarea for testing relationships
        area = new Area();
        area.setCode("AREA" + counter);
        area.setName("Test Area " + counter);
        area = areaRepository.save(area);
        
        subarea = new Subarea();
        subarea.setCode("SUB" + counter);
        subarea.setName("Test Subarea " + counter);
        subarea.setArea(area);
        subarea = subareaRepository.save(subarea);
    }

    @Test
    void createIndicator_shouldSucceed() throws Exception {
        long counter = testCounter.getAndIncrement();
        IndicatorCreateRequest req = new IndicatorCreateRequest();
        req.setCode("IND" + counter);
        req.setName("Indicator " + counter);
        req.setIsComposite(false);
        req.setUnitId(unit.getId());
        req.setDataTypeId(dataType.getId());
        
        mockMvc.perform(post("/api/v1/indicators")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("IND" + counter))
                .andExpect(jsonPath("$.unit.id").value(unit.getId()))
                .andExpect(jsonPath("$.dataType.id").value(dataType.getId()));
        
        assertThat(indicatorRepository.findByCode("IND" + counter)).isPresent();
    }

    @Test
    void createIndicator_shouldFail_duplicateCode() throws Exception {
        long counter = testCounter.getAndIncrement();
        Indicator indicator = new Indicator();
        indicator.setCode("DUP" + counter);
        indicator.setName("Indicator");
        indicator.setIsComposite(false);
        indicatorRepository.save(indicator);
        
        IndicatorCreateRequest req = new IndicatorCreateRequest();
        req.setCode("DUP" + counter);
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
        long counter = testCounter.getAndIncrement();
        IndicatorCreateRequest req = new IndicatorCreateRequest();
        req.setCode("IND" + counter);
        req.setName("Indicator " + counter);
        req.setUnitId(9999L);
        
        mockMvc.perform(post("/api/v1/indicators")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getAllIndicators_shouldReturnList() throws Exception {
        long counter = testCounter.getAndIncrement();
        Indicator indicator = new Indicator();
        indicator.setCode("IND" + counter);
        indicator.setName("Indicator " + counter);
        indicator.setIsComposite(false);
        indicatorRepository.save(indicator);
        
        mockMvc.perform(get("/api/v1/indicators"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("IND" + counter));
    }

    @Test
    void getIndicatorById_shouldReturnIndicator() throws Exception {
        long counter = testCounter.getAndIncrement();
        Indicator indicator = new Indicator();
        indicator.setCode("IND" + counter);
        indicator.setName("Indicator " + counter);
        indicator.setIsComposite(false);
        indicator = indicatorRepository.save(indicator);
        
        mockMvc.perform(get("/api/v1/indicators/" + indicator.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("IND" + counter));
    }

    @Test
    void getIndicatorById_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/indicators/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getIndicatorsBySubarea_shouldReturnList() throws Exception {
        long counter = testCounter.getAndIncrement();
        Indicator indicator = new Indicator();
        indicator.setCode("IND" + counter);
        indicator.setName("Indicator " + counter);
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
                .andExpect(jsonPath("$[0].code").value("IND" + counter));
    }

    @Test
    void updateIndicator_shouldUpdateFields() throws Exception {
        long counter = testCounter.getAndIncrement();
        Indicator indicator = new Indicator();
        indicator.setCode("IND" + counter);
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
        long counter = testCounter.getAndIncrement();
        Indicator indicator = new Indicator();
        indicator.setCode("IND" + counter);
        indicator.setName("Indicator " + counter);
        indicator.setIsComposite(false);
        indicator = indicatorRepository.save(indicator);
        
        mockMvc.perform(delete("/api/v1/indicators/" + indicator.getId()))
                .andExpect(status().isNoContent());
        
        assertThat(indicatorRepository.findById(indicator.getId())).isEmpty();
    }

    @Test
    void assignIndicatorToSubarea_shouldSucceed() throws Exception {
        long counter = testCounter.getAndIncrement();
        Indicator indicator = new Indicator();
        indicator.setCode("IND" + counter);
        indicator.setName("Indicator " + counter);
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
        long counter = testCounter.getAndIncrement();
        Indicator indicator = new Indicator();
        indicator.setCode("IND" + counter);
        indicator.setName("Indicator " + counter);
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
        long counter = testCounter.getAndIncrement();
        Indicator indicator = new Indicator();
        indicator.setCode("IND" + counter);
        indicator.setName("Indicator " + counter);
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
        long counter = testCounter.getAndIncrement();
        Indicator indicator = new Indicator();
        indicator.setCode("IND" + counter);
        indicator.setName("Indicator " + counter);
        indicator.setIsComposite(false);
        indicator = indicatorRepository.save(indicator);
        
        mockMvc.perform(delete("/api/v1/indicators/" + indicator.getId() + "/subareas/" + subarea.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getIndicatorChart_shouldReturnAggregatedData() throws Exception {
        Indicator indicator = new Indicator();
        indicator.setCode("CHART1");
        indicator.setName("Chart Indicator");
        indicator.setIsComposite(false);
        indicator.setUnit(unit);
        indicator = indicatorRepository.save(indicator);
        // Add some fact values
        FactIndicatorValue value1 = FactIndicatorValue.builder()
            .indicator(indicator)
            .value(BigDecimal.valueOf(10))
            .time(dimTime)
            .sourceRowHash("chart-1")
            .build();
        factIndicatorValueRepository.save(value1);
        FactIndicatorValue value2 = FactIndicatorValue.builder()
            .indicator(indicator)
            .value(BigDecimal.valueOf(20))
            .time(dimTime)
            .sourceRowHash("chart-2")
            .build();
        factIndicatorValueRepository.save(value2);
        // Capture and print response for debugging
        var result = mockMvc.perform(get("/api/v1/indicators/" + indicator.getId() + "/chart?aggregateBy=time"))
            .andReturn();
        int status = result.getResponse().getStatus();
        if (status != 200) {
            System.out.println("Response body: " + result.getResponse().getContentAsString());
        }
        // Now assert as before
        mockMvc.perform(get("/api/v1/indicators/" + indicator.getId() + "/chart?aggregateBy=time"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.indicatorId").value(String.valueOf(indicator.getId())))
            .andExpect(jsonPath("$.dataPoints").isArray());
    }

    @Test
    void getIndicatorDimensions_shouldReturnAvailableDimensions() throws Exception {
        Indicator indicator = new Indicator();
        indicator.setCode("DIM1");
        indicator.setName("Dim Indicator");
        indicator.setIsComposite(false);
        indicator = indicatorRepository.save(indicator);
        FactIndicatorValue value = FactIndicatorValue.builder()
            .indicator(indicator)
            .value(BigDecimal.valueOf(5))
            .time(dimTime)
            .sourceRowHash("dim-1")
            .build();
        factIndicatorValueRepository.save(value);
        mockMvc.perform(get("/api/v1/indicators/" + indicator.getId() + "/dimensions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.indicatorId").value(String.valueOf(indicator.getId())))
            .andExpect(jsonPath("$.availableDimensions").isArray());
    }

    @Test
    void getIndicatorHistorical_shouldReturnHistoricalData() throws Exception {
        Indicator indicator = new Indicator();
        indicator.setCode("HIST1");
        indicator.setName("Hist Indicator");
        indicator.setIsComposite(false);
        indicator = indicatorRepository.save(indicator);
        FactIndicatorValue value = FactIndicatorValue.builder()
            .indicator(indicator)
            .value(BigDecimal.valueOf(15))
            .time(dimTime)
            .sourceRowHash("hist-1")
            .build();
        factIndicatorValueRepository.save(value);
        mockMvc.perform(get("/api/v1/indicators/" + indicator.getId() + "/historical"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.indicatorId").value(String.valueOf(indicator.getId())))
            .andExpect(jsonPath("$.dataPoints").isArray());
    }

    @Test
    void getIndicatorValidation_shouldReturnValidationResult() throws Exception {
        Indicator indicator = new Indicator();
        indicator.setCode("VAL1");
        indicator.setName("Val Indicator");
        indicator.setIsComposite(false);
        indicator = indicatorRepository.save(indicator);
        FactIndicatorValue value = FactIndicatorValue.builder()
            .indicator(indicator)
            .value(BigDecimal.valueOf(25))
            .time(dimTime)
            .sourceRowHash("val-1")
            .build();
        factIndicatorValueRepository.save(value);
        mockMvc.perform(get("/api/v1/indicators/" + indicator.getId() + "/validation"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.indicatorId").value(indicator.getId().intValue()))
            .andExpect(jsonPath("$.isValid").value(true));
    }

    @Test
    void createSampleHistoricalData_shouldCreateAndReturnData() throws Exception {
        Indicator indicator = new Indicator();
        indicator.setCode("SAMPLE1");
        indicator.setName("Sample Indicator");
        indicator.setIsComposite(false);
        indicator = indicatorRepository.save(indicator);
        mockMvc.perform(post("/api/v1/indicators/" + indicator.getId() + "/sample-data"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.indicatorId").value(String.valueOf(indicator.getId())))
            .andExpect(jsonPath("$.dataPoints").isArray());
    }
} 