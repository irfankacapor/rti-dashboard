package io.dashboard.controller;

import io.dashboard.dto.CsvIndicatorData;
import io.dashboard.dto.IndicatorBatchRequest;
import io.dashboard.dto.IndicatorResponse;
import io.dashboard.model.Area;
import io.dashboard.model.Direction;
import io.dashboard.model.FactIndicatorValue;
import io.dashboard.model.Indicator;
import io.dashboard.model.Subarea;
import io.dashboard.repository.AreaRepository;
import io.dashboard.repository.FactIndicatorValueRepository;
import io.dashboard.repository.IndicatorRepository;
import io.dashboard.repository.SubareaRepository;
import io.dashboard.service.IndicatorBatchService;
import io.dashboard.service.IndicatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class IndicatorDirectionIntegrationTest {

    @Autowired
    private IndicatorBatchService indicatorBatchService;

    @Autowired
    private IndicatorService indicatorService;

    @Autowired
    private IndicatorRepository indicatorRepository;

    @Autowired
    private SubareaRepository subareaRepository;

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private FactIndicatorValueRepository factIndicatorValueRepository;

    private Subarea testSubarea1;
    private Subarea testSubarea2;

    @BeforeEach
    void setUp() {
        // Create test area first
        Area testArea = new Area();
        testArea.setName("Test Area");
        testArea.setCode("TEST_AREA");
        testArea.setDescription("Test area for direction tests");
        testArea = areaRepository.save(testArea);

        // Create test subareas with the area
        testSubarea1 = new Subarea();
        testSubarea1.setName("Test Subarea 1");
        testSubarea1.setCode("TEST_SUB1");
        testSubarea1.setArea(testArea);
        testSubarea1 = subareaRepository.save(testSubarea1);

        testSubarea2 = new Subarea();
        testSubarea2.setName("Test Subarea 2");
        testSubarea2.setCode("TEST_SUB2");
        testSubarea2.setArea(testArea);
        testSubarea2 = subareaRepository.save(testSubarea2);
    }

    @Test
    void testCompleteFlow_CSVProcessingToDirectionRetrieval() {
        // Given - Create CSV data with different directions for same indicator in different subareas
        CsvIndicatorData csvIndicator1 = CsvIndicatorData.builder()
            .name("Same Indicator")
            .description("Test Description")
            .unit("Test Unit")
            .subareaId(testSubarea1.getId())
            .direction(Direction.INPUT)
            .values(Arrays.asList(
                createIndicatorValue("2023", "Location1", 100.0),
                createIndicatorValue("2023", "Location2", 200.0)
            ))
            .build();

        CsvIndicatorData csvIndicator2 = CsvIndicatorData.builder()
            .name("Same Indicator")
            .description("Test Description")
            .unit("Test Unit")
            .subareaId(testSubarea2.getId())
            .direction(Direction.OUTPUT)
            .values(Arrays.asList(
                createIndicatorValue("2023", "Location1", 300.0),
                createIndicatorValue("2023", "Location2", 400.0)
            ))
            .build();

        IndicatorBatchRequest request = new IndicatorBatchRequest();
        request.setIndicators(Arrays.asList(csvIndicator1, csvIndicator2));

        // When - Process CSV data
        var batchResponse = indicatorBatchService.createFromCsvData(request);

        // Then - Verify CSV processing
        assertThat(batchResponse.getCreatedIndicators()).hasSize(1);
        assertThat(batchResponse.getTotalFactRecords()).isEqualTo(4);
        assertThat(batchResponse.getWarnings()).isEmpty();

        // Get the created indicator
        Indicator createdIndicator = indicatorRepository.findByName("Same Indicator").orElse(null);
        assertThat(createdIndicator).isNotNull();

        // Verify fact records were created with correct directions
        List<FactIndicatorValue> factsForSubarea1 = factIndicatorValueRepository
            .findByIndicatorIdAndSubareaId(createdIndicator.getId(), testSubarea1.getId());
        assertThat(factsForSubarea1).hasSize(2);
        assertThat(factsForSubarea1).allMatch(fact -> "input".equals(fact.getDirection()));

        List<FactIndicatorValue> factsForSubarea2 = factIndicatorValueRepository
            .findByIndicatorIdAndSubareaId(createdIndicator.getId(), testSubarea2.getId());
        assertThat(factsForSubarea2).hasSize(2);
        assertThat(factsForSubarea2).allMatch(fact -> "output".equals(fact.getDirection()));

        // When - Retrieve indicators for specific subareas
        List<IndicatorResponse> indicatorsForSubarea1 = indicatorService.findByFactSubareaId(testSubarea1.getId());
        List<IndicatorResponse> indicatorsForSubarea2 = indicatorService.findByFactSubareaId(testSubarea2.getId());

        // Then - Verify direction is correct for each subarea
        assertThat(indicatorsForSubarea1).hasSize(1);
        assertThat(indicatorsForSubarea1.get(0).getDirection()).isEqualTo("input");
        assertThat(indicatorsForSubarea1.get(0).getValueCount()).isEqualTo(2L);

        assertThat(indicatorsForSubarea2).hasSize(1);
        assertThat(indicatorsForSubarea2.get(0).getDirection()).isEqualTo("output");
        assertThat(indicatorsForSubarea2.get(0).getValueCount()).isEqualTo(2L);
    }

    @Test
    void testDirectionRetrieval_WithMixedDirectionsInSameSubarea() {
        // Given - Create an indicator with mixed directions in the same subarea
        CsvIndicatorData csvIndicator = CsvIndicatorData.builder()
            .name("Mixed Direction Indicator")
            .description("Test Description")
            .unit("Test Unit")
            .subareaId(testSubarea1.getId())
            .direction(Direction.INPUT) // This will be set on all facts initially
            .values(Arrays.asList(
                createIndicatorValue("2023", "Location1", 100.0),
                createIndicatorValue("2023", "Location2", 200.0),
                createIndicatorValue("2023", "Location3", 300.0)
            ))
            .build();

        IndicatorBatchRequest request = new IndicatorBatchRequest();
        request.setIndicators(Arrays.asList(csvIndicator));

        // Process CSV data
        indicatorBatchService.createFromCsvData(request);

        // Manually update some facts to have different directions
        Indicator indicator = indicatorRepository.findByName("Mixed Direction Indicator").orElse(null);
        assertThat(indicator).isNotNull();

        List<FactIndicatorValue> facts = factIndicatorValueRepository
            .findByIndicatorIdAndSubareaId(indicator.getId(), testSubarea1.getId());
        
        // Update first fact to output direction
        if (!facts.isEmpty()) {
            facts.get(0).setDirection("output");
            factIndicatorValueRepository.save(facts.get(0));
        }

        // When - Retrieve indicators for the subarea
        List<IndicatorResponse> indicators = indicatorService.findByFactSubareaId(testSubarea1.getId());

        // Then - Should return the most common direction (input has 2, output has 1)
        assertThat(indicators).hasSize(1);
        assertThat(indicators.get(0).getDirection()).isEqualTo("input");
        assertThat(indicators.get(0).getValueCount()).isEqualTo(3L);
    }

    @Test
    void testDirectionRetrieval_WithNullDirections() {
        // Given - Create an indicator with null directions
        CsvIndicatorData csvIndicator = CsvIndicatorData.builder()
            .name("Null Direction Indicator")
            .description("Test Description")
            .unit("Test Unit")
            .subareaId(testSubarea1.getId())
            .direction(null) // Null direction
            .values(Arrays.asList(
                createIndicatorValue("2023", "Location1", 100.0),
                createIndicatorValue("2023", "Location2", 200.0)
            ))
            .build();

        IndicatorBatchRequest request = new IndicatorBatchRequest();
        request.setIndicators(Arrays.asList(csvIndicator));

        // Process CSV data
        indicatorBatchService.createFromCsvData(request);

        // When - Retrieve indicators for the subarea
        List<IndicatorResponse> indicators = indicatorService.findByFactSubareaId(testSubarea1.getId());

        // Then - Should return null direction
        assertThat(indicators).hasSize(1);
        assertThat(indicators.get(0).getDirection()).isNull();
        assertThat(indicators.get(0).getValueCount()).isEqualTo(2L);
    }

    private io.dashboard.dto.IndicatorValue createIndicatorValue(String timeValue, String locationValue, Double value) {
        io.dashboard.dto.IndicatorValue indicatorValue = new io.dashboard.dto.IndicatorValue();
        indicatorValue.setTimeValue(timeValue);
        indicatorValue.setTimeType("year");
        indicatorValue.setLocationValue(locationValue);
        indicatorValue.setLocationType("state");
        indicatorValue.setValue(BigDecimal.valueOf(value));
        return indicatorValue;
    }
} 