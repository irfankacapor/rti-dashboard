package io.dashboard.controller;

import io.dashboard.dto.CsvIndicatorData;
import io.dashboard.dto.IndicatorBatchRequest;
import io.dashboard.dto.SubareaDataResponse;
import io.dashboard.model.Area;
import io.dashboard.model.Direction;
import io.dashboard.model.Subarea;
import io.dashboard.repository.AreaRepository;
import io.dashboard.repository.SubareaRepository;
import io.dashboard.service.IndicatorBatchService;
import io.dashboard.service.SubareaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class SubareaDataDirectionTest {

    @Autowired
    private IndicatorBatchService indicatorBatchService;

    @Autowired
    private SubareaService subareaService;

    @Autowired
    private SubareaRepository subareaRepository;

    @Autowired
    private AreaRepository areaRepository;

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
    void testSubareaDataEndpoint_ReturnsCorrectDirections() {
        // Given - Create indicators with different directions in different subareas
        CsvIndicatorData csvIndicator1 = CsvIndicatorData.builder()
            .name("Direction Test Indicator")
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
            .name("Direction Test Indicator")
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

        // Process CSV data
        indicatorBatchService.createFromCsvData(request);

        // When - Get subarea data for each subarea
        SubareaDataResponse subarea1Data = subareaService.getSubareaData(testSubarea1.getId());
        SubareaDataResponse subarea2Data = subareaService.getSubareaData(testSubarea2.getId());

        // Then - Verify directions are correct for each subarea
        assertThat(subarea1Data.getIndicators()).hasSize(1);
        assertThat(subarea1Data.getIndicators().get(0).getDirection()).isEqualTo("input");
        assertThat(subarea1Data.getIndicators().get(0).getValueCount()).isEqualTo(2L);

        assertThat(subarea2Data.getIndicators()).hasSize(1);
        assertThat(subarea2Data.getIndicators().get(0).getDirection()).isEqualTo("output");
        assertThat(subarea2Data.getIndicators().get(0).getValueCount()).isEqualTo(2L);
    }

    @Test
    void testSubareaDataEndpoint_WithNullDirections() {
        // Given - Create indicator with null direction
        CsvIndicatorData csvIndicator = CsvIndicatorData.builder()
            .name("Null Direction Indicator")
            .description("Test Description")
            .unit("Test Unit")
            .subareaId(testSubarea1.getId())
            .direction(null)
            .values(Arrays.asList(
                createIndicatorValue("2023", "Location1", 100.0),
                createIndicatorValue("2023", "Location2", 200.0)
            ))
            .build();

        IndicatorBatchRequest request = new IndicatorBatchRequest();
        request.setIndicators(Arrays.asList(csvIndicator));

        // Process CSV data
        indicatorBatchService.createFromCsvData(request);

        // When - Get subarea data
        SubareaDataResponse subareaData = subareaService.getSubareaData(testSubarea1.getId());

        // Then - Verify null direction is handled correctly
        assertThat(subareaData.getIndicators()).hasSize(1);
        assertThat(subareaData.getIndicators().get(0).getDirection()).isNull();
        assertThat(subareaData.getIndicators().get(0).getValueCount()).isEqualTo(2L);
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