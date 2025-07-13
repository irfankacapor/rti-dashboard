package io.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dashboard.dto.IndicatorCreateRequest;
import io.dashboard.dto.IndicatorUpdateRequest;
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
import io.dashboard.repository.SubareaRepository;
import io.dashboard.repository.UnitRepository;
import io.dashboard.test.security.WithMockAdmin;
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
import org.springframework.security.test.context.support.WithMockUser;
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
    @WithMockAdmin
    void createIndicator_shouldSucceed() throws Exception {
        long counter = testCounter.getAndIncrement();
        // Create a unit with unique code
        Unit eurUnit = new Unit();
        eurUnit.setCode("EUR" + counter);
        eurUnit.setDescription("Euro");
        eurUnit = unitRepository.save(eurUnit);

        IndicatorCreateRequest req = new IndicatorCreateRequest();
        req.setCode("IND" + counter);
        req.setName("Indicator " + counter);
        req.setIsComposite(false);
        req.setUnitId(eurUnit.getId());
        req.setUnitPrefix("€");
        req.setUnitSuffix("M");
        req.setDataTypeId(dataType.getId());
        
        mockMvc.perform(post("/api/v1/indicators")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("IND" + counter))
                .andExpect(jsonPath("$.unit").value("EUR" + counter))
                .andExpect(jsonPath("$.dataType.id").value(dataType.getId()));
        
        assertThat(indicatorRepository.findByCode("IND" + counter)).isPresent();
    }

    @Test
    @WithMockAdmin
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
    @WithMockAdmin
    void createIndicator_shouldSucceed_withCustomUnit() throws Exception {
        long counter = testCounter.getAndIncrement();
        // Create a unit with code 'CUSTOM_UNIT'
        Unit customUnit = new Unit();
        customUnit.setCode("CUSTOM_UNIT");
        customUnit.setDescription("Custom Unit");
        customUnit = unitRepository.save(customUnit);

        IndicatorCreateRequest req = new IndicatorCreateRequest();
        req.setCode("IND" + counter);
        req.setName("Indicator " + counter);
        req.setIsComposite(false);
        req.setUnitId(customUnit.getId());
        req.setUnitPrefix("$");
        req.setUnitSuffix("B");
        req.setDataTypeId(dataType.getId());
        
        mockMvc.perform(post("/api/v1/indicators")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("IND" + counter))
                .andExpect(jsonPath("$.unit").value("CUSTOM_UNIT"))
                .andExpect(jsonPath("$.dataType.id").value(dataType.getId()));
        
        assertThat(indicatorRepository.findByCode("IND" + counter)).isPresent();
    }

    @Test
    @WithMockUser
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
    @WithMockUser
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
    @WithMockUser
    void getIndicatorsBySubarea_shouldReturnList() throws Exception {
        long counter = testCounter.getAndIncrement();
        Indicator indicator = new Indicator();
        indicator.setCode("IND" + counter);
        indicator.setName("Indicator " + counter);
        indicator.setIsComposite(false);
        indicator = indicatorRepository.save(indicator);
        
        // Create a fact to link indicator to subarea
        FactIndicatorValue fact = new FactIndicatorValue();
        fact.setIndicator(indicator);
        fact.setSubarea(subarea);
        fact.setTime(dimTime);
        fact.setLocation(dimLocation);
        fact.getGenerics().add(dimGeneric);
        fact.setValue(new BigDecimal("100.0"));
        fact.setSourceRowHash("test-hash-" + counter);
        factIndicatorValueRepository.save(fact);
        
        mockMvc.perform(get("/api/v1/subareas/" + subarea.getId() + "/indicators"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("IND" + counter));
    }

    @Test
    @WithMockAdmin
    void updateIndicator_shouldUpdateFields() throws Exception {
        long counter = testCounter.getAndIncrement();
        Indicator indicator = new Indicator();
        indicator.setCode("IND" + counter);
        indicator.setName("Old Name");
        indicator.setIsComposite(false);
        indicator = indicatorRepository.save(indicator);
        
        IndicatorUpdateRequest req = new IndicatorUpdateRequest();
        req.setName("Updated Name");
        req.setDescription("New description");
        req.setIsComposite(false); // Add required field
        
        mockMvc.perform(put("/api/v1/indicators/" + indicator.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    @WithMockAdmin
    void updateIndicator_shouldReturn404_whenNotFound() throws Exception {
        IndicatorUpdateRequest req = new IndicatorUpdateRequest();
        req.setName("Name");
        req.setIsComposite(false); // Add required field
        
        mockMvc.perform(put("/api/v1/indicators/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockAdmin
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
    @WithMockUser
    void getIndicatorChart_shouldReturnAggregatedData() throws Exception {
        long counter = testCounter.getAndIncrement();
        Indicator indicator = new Indicator();
        indicator.setCode("IND" + counter);
        indicator.setName("Indicator " + counter);
        indicator.setIsComposite(false);
        indicator = indicatorRepository.save(indicator);
        
        // Create facts for the indicator
        FactIndicatorValue fact1 = new FactIndicatorValue();
        fact1.setIndicator(indicator);
        fact1.setSubarea(subarea);
        fact1.setTime(dimTime);
        fact1.setLocation(dimLocation);
        fact1.getGenerics().add(dimGeneric);
        fact1.setValue(new BigDecimal("100.0"));
        fact1.setSourceRowHash("test-hash-" + counter);
        factIndicatorValueRepository.save(fact1);
        
        mockMvc.perform(get("/api/v1/indicators/" + indicator.getId() + "/chart")
                .param("aggregationType", "SUM")
                .param("timeRange", "1Y"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataPoints").exists());
    }

    @Test
    @WithMockUser
    void getIndicatorDimensions_shouldReturnAvailableDimensions() throws Exception {
        long counter = testCounter.getAndIncrement();
        Indicator indicator = new Indicator();
        indicator.setCode("IND" + counter);
        indicator.setName("Indicator " + counter);
        indicator.setIsComposite(false);
        indicator = indicatorRepository.save(indicator);
        
        mockMvc.perform(get("/api/v1/indicators/" + indicator.getId() + "/dimensions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableDimensions").exists());
    }

    @Test
    @WithMockUser
    void getIndicatorHistorical_shouldReturnHistoricalData() throws Exception {
        long counter = testCounter.getAndIncrement();
        Indicator indicator = new Indicator();
        indicator.setCode("IND" + counter);
        indicator.setName("Indicator " + counter);
        indicator.setIsComposite(false);
        indicator = indicatorRepository.save(indicator);
        
        mockMvc.perform(get("/api/v1/indicators/" + indicator.getId() + "/historical")
                .param("timeRange", "1Y"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataPoints").exists());
    }

    @Test
    @WithMockUser
    void getIndicatorValidation_shouldReturnValidationResult() throws Exception {
        long counter = testCounter.getAndIncrement();
        Indicator indicator = new Indicator();
        indicator.setCode("IND" + counter);
        indicator.setName("Indicator " + counter);
        indicator.setIsComposite(false);
        indicator = indicatorRepository.save(indicator);
        
        mockMvc.perform(get("/api/v1/indicators/" + indicator.getId() + "/validation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isValid").exists());
    }

    @Test
    @WithMockAdmin
    void createSampleHistoricalData_shouldCreateAndReturnData() throws Exception {
        long counter = testCounter.getAndIncrement();
        Indicator indicator = new Indicator();
        indicator.setCode("IND" + counter);
        indicator.setName("Indicator " + counter);
        indicator.setIsComposite(false);
        indicator = indicatorRepository.save(indicator);
        
        mockMvc.perform(post("/api/v1/indicators/" + indicator.getId() + "/sample-data")
                .param("months", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataPoints").exists());
    }
} 