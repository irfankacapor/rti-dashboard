package io.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dashboard.dto.CsvIndicatorData;
import io.dashboard.dto.IndicatorBatchRequest;
import io.dashboard.dto.IndicatorBatchResponse;
import io.dashboard.dto.IndicatorValue;
import io.dashboard.model.Direction;
import io.dashboard.model.Subarea;
import io.dashboard.model.Area;
import io.dashboard.repository.SubareaRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class IndicatorBatchControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private SubareaRepository subareaRepository;
    
    @Autowired
    private AreaRepository areaRepository;
    
    private Long testSubareaId;
    
    @BeforeEach
    void setUp() {
        // Create a test area and subarea
        Area area = new Area();
        area.setCode("TEST_AREA");
        area.setName("Test Area");
        area = areaRepository.save(area);
        
        Subarea subarea = new Subarea();
        subarea.setCode("TEST_SUBAREA");
        subarea.setName("Test Subarea");
        subarea.setDescription("Test subarea for integration tests");
        subarea.setArea(area);
        subarea = subareaRepository.save(subarea);
        testSubareaId = subarea.getId();
    }
    
    @Test
    void shouldCreateIndicatorsFromCsvData() throws Exception {
        // Test the simplified endpoint with sample Austrian data
        IndicatorBatchRequest request = IndicatorBatchRequest.builder()
            .indicators(List.of(
                CsvIndicatorData.builder()
                    .name("KMU mit grundlegender Digitalisierungsintensitaet")
                    .description("Small and medium enterprises with at least basic digitalization intensity")
                    .unit("Percent")
                    .source("Austrian Statistical Office")
                    .subareaId(testSubareaId)
                    .direction(Direction.INPUT)
                    .aggregationWeight(1.0)
                    .values(List.of(
                        IndicatorValue.builder()
                            .value(new BigDecimal("49"))
                            .timeValue("2023")
                            .timeType("year")
                            .locationValue("Burgenland")
                            .locationType("state")
                            .customDimensions(Map.of("Sector", "Private"))
                            .build(),
                        IndicatorValue.builder()
                            .value(new BigDecimal("52"))
                            .timeValue("2023")
                            .timeType("year")
                            .locationValue("Vienna")
                            .locationType("state")
                            .customDimensions(Map.of("Sector", "Private"))
                            .build()
                    ))
                    .build()
            ))
            .build();
            
        // Test the endpoint
        String response = mockMvc.perform(post("/api/v1/indicators/create-from-csv")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdIndicators").isArray())
                .andExpect(jsonPath("$.totalFactRecords").value(2))
                .andExpect(jsonPath("$.message").value("Successfully processed 1 indicators"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        // Parse response and verify details
        IndicatorBatchResponse batchResponse = objectMapper.readValue(response, IndicatorBatchResponse.class);
        assertThat(batchResponse.getCreatedIndicators()).hasSize(1);
        assertThat(batchResponse.getWarnings()).isEmpty();
        
        // Verify the created indicator
        var createdIndicator = batchResponse.getCreatedIndicators().get(0);
        assertThat(createdIndicator.getName()).isEqualTo("KMU mit grundlegender Digitalisierungsintensitaet");
        assertThat(createdIndicator.getDescription()).isEqualTo("Small and medium enterprises with at least basic digitalization intensity");
        assertThat(createdIndicator.getIsComposite()).isFalse();
    }
    
    @Test
    void shouldHandleMultipleIndicators() throws Exception {
        IndicatorBatchRequest request = IndicatorBatchRequest.builder()
            .indicators(List.of(
                CsvIndicatorData.builder()
                    .name("Digitalization Index")
                    .subareaId(testSubareaId)
                    .direction(Direction.INPUT)
                    .values(List.of(
                        IndicatorValue.builder()
                            .value(new BigDecimal("75.5"))
                            .timeValue("2023")
                            .timeType("year")
                            .locationValue("Austria")
                            .locationType("country")
                            .build()
                    ))
                    .build(),
                CsvIndicatorData.builder()
                    .name("E-Government Usage")
                    .subareaId(testSubareaId)
                    .direction(Direction.OUTPUT)
                    .values(List.of(
                        IndicatorValue.builder()
                            .value(new BigDecimal("68.2"))
                            .timeValue("2023")
                            .timeType("year")
                            .locationValue("Austria")
                            .locationType("country")
                            .build()
                    ))
                    .build()
            ))
            .build();
            
        mockMvc.perform(post("/api/v1/indicators/create-from-csv")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdIndicators").isArray())
                .andExpect(jsonPath("$.createdIndicators.length()").value(2))
                .andExpect(jsonPath("$.totalFactRecords").value(2));
    }
    
    @Test
    void shouldHandleDuplicateIndicators() throws Exception {
        // Create the same indicator twice
        IndicatorBatchRequest request = IndicatorBatchRequest.builder()
            .indicators(List.of(
                CsvIndicatorData.builder()
                    .name("Test Indicator")
                    .subareaId(testSubareaId)
                    .direction(Direction.INPUT)
                    .values(List.of(
                        IndicatorValue.builder()
                            .value(new BigDecimal("100"))
                            .timeValue("2023")
                            .timeType("year")
                            .locationValue("Test")
                            .locationType("state")
                            .build()
                    ))
                    .build(),
                CsvIndicatorData.builder()
                    .name("Test Indicator") // Same name
                    .subareaId(testSubareaId)
                    .direction(Direction.OUTPUT)
                    .values(List.of(
                        IndicatorValue.builder()
                            .value(new BigDecimal("200"))
                            .timeValue("2023")
                            .timeType("year")
                            .locationValue("Test")
                            .locationType("state")
                            .build()
                    ))
                    .build()
            ))
            .build();
            
        mockMvc.perform(post("/api/v1/indicators/create-from-csv")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdIndicators.length()").value(1)) // Should reuse existing indicator
                .andExpect(jsonPath("$.totalFactRecords").value(2)); // But create both fact records
    }
} 