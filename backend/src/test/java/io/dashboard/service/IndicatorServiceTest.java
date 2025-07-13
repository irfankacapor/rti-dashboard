package io.dashboard.service;

import io.dashboard.dto.IndicatorCreateRequest;
import io.dashboard.dto.IndicatorResponse;
import io.dashboard.dto.IndicatorUpdateRequest;
import io.dashboard.dto.SubareaIndicatorRequest;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.DataType;
import io.dashboard.model.Direction;
import io.dashboard.model.Indicator;
import io.dashboard.model.Subarea;
import io.dashboard.model.SubareaIndicator;
import io.dashboard.model.Unit;
import io.dashboard.repository.DataTypeRepository;
import io.dashboard.repository.IndicatorRepository;
import io.dashboard.repository.UnitRepository;
import io.dashboard.repository.SubareaIndicatorRepository;
import io.dashboard.repository.SubareaRepository;

import io.dashboard.repository.FactIndicatorValueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IndicatorServiceTest {
    @Mock
    private IndicatorRepository indicatorRepository;
    @Mock
    private UnitRepository unitRepository;
    @Mock
    private DataTypeRepository dataTypeRepository;
    @Mock
    private SubareaRepository subareaRepository;
    @Mock
    private SubareaIndicatorRepository subareaIndicatorRepository;
    @Mock
    private FactIndicatorValueRepository factIndicatorValueRepository;
    @Mock
    private AggregationService aggregationService;
    @InjectMocks
    private IndicatorService indicatorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(factIndicatorValueRepository.countByIndicatorId(anyLong())).thenReturn(0L);
        when(aggregationService.calculateIndicatorAggregatedValue(anyLong())).thenReturn(0.0);
    }

    @Test
    void findAll_returnsAllIndicators() {
        Indicator indicator = new Indicator();
        indicator.setId(1L);
        indicator.setCode("IND1");
        when(indicatorRepository.findAll()).thenReturn(List.of(indicator));
        List<IndicatorResponse> result = indicatorService.findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("IND1");
    }

    @Test
    void findById_found() {
        Indicator indicator = new Indicator();
        indicator.setId(1L);
        indicator.setCode("IND1");
        when(indicatorRepository.findById(1L)).thenReturn(Optional.of(indicator));
        IndicatorResponse resp = indicatorService.findById(1L);
        assertThat(resp.getCode()).isEqualTo("IND1");
    }

    @Test
    void findById_notFound() {
        when(indicatorRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> indicatorService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findBySubareaId_returnsIndicatorsForSubarea() {
        Indicator indicator = new Indicator();
        indicator.setId(1L);
        indicator.setCode("IND1");
        when(indicatorRepository.findBySubareaId(10L)).thenReturn(List.of(indicator));
        List<IndicatorResponse> result = indicatorService.findBySubareaId(10L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void create_success() {
        IndicatorCreateRequest req = new IndicatorCreateRequest();
        req.setCode("IND1");
        req.setName("Indicator 1");
        req.setIsComposite(false);
        when(indicatorRepository.existsByCode("IND1")).thenReturn(false);
        when(indicatorRepository.save(any(Indicator.class))).thenAnswer(inv -> {
            Indicator i = inv.getArgument(0);
            i.setId(11L);
            return i;
        });
        IndicatorResponse resp = indicatorService.create(req);
        assertThat(resp.getId()).isEqualTo(11L);
        assertThat(resp.getCode()).isEqualTo("IND1");
    }

    @Test
    void create_duplicateCode() {
        IndicatorCreateRequest req = new IndicatorCreateRequest();
        req.setCode("DUP");
        when(indicatorRepository.existsByCode("DUP")).thenReturn(true);
        assertThatThrownBy(() -> indicatorService.create(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("unique");
    }

    @Test
    void create_withValidUnit() {
        IndicatorCreateRequest req = new IndicatorCreateRequest();
        req.setCode("IND1");
        req.setName("Indicator 1");
        req.setUnitId(1L);
        req.setUnitPrefix("€");
        req.setUnitSuffix("M");
        when(indicatorRepository.existsByCode("IND1")).thenReturn(false);
        Unit unit = new Unit();
        unit.setId(1L);
        unit.setCode("EUR");
        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(indicatorRepository.save(any(Indicator.class))).thenAnswer(inv -> {
            Indicator i = inv.getArgument(0);
            i.setId(11L);
            return i;
        });
        IndicatorResponse resp = indicatorService.create(req);
        assertThat(resp.getId()).isEqualTo(11L);
    }



    @Test
    void create_withValidDataType() {
        IndicatorCreateRequest req = new IndicatorCreateRequest();
        req.setCode("IND1");
        req.setName("Indicator 1");
        req.setDataTypeId(1L);
        DataType dataType = new DataType();
        dataType.setId(1L);
        when(indicatorRepository.existsByCode("IND1")).thenReturn(false);
        when(dataTypeRepository.findById(1L)).thenReturn(Optional.of(dataType));
        when(indicatorRepository.save(any(Indicator.class))).thenAnswer(inv -> {
            Indicator i = inv.getArgument(0);
            i.setId(11L);
            return i;
        });
        IndicatorResponse resp = indicatorService.create(req);
        assertThat(resp.getId()).isEqualTo(11L);
    }

    @Test
    void create_invalidDataType() {
        IndicatorCreateRequest req = new IndicatorCreateRequest();
        req.setCode("IND1");
        req.setDataTypeId(999L);
        when(indicatorRepository.existsByCode("IND1")).thenReturn(false);
        when(dataTypeRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> indicatorService.create(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("DataType does not exist");
    }

    @Test
    void update_success() {
        IndicatorUpdateRequest req = new IndicatorUpdateRequest();
        req.setName("Updated");
        req.setIsComposite(true);
        Indicator indicator = new Indicator();
        indicator.setId(1L);
        indicator.setCode("IND1");
        when(indicatorRepository.findById(1L)).thenReturn(Optional.of(indicator));
        when(indicatorRepository.save(any(Indicator.class))).thenAnswer(inv -> inv.getArgument(0));
        IndicatorResponse resp = indicatorService.update(1L, req);
        assertThat(resp.getName()).isEqualTo("Updated");
        assertThat(resp.getIsComposite()).isTrue();
    }

    @Test
    void update_notFound() {
        IndicatorUpdateRequest req = new IndicatorUpdateRequest();
        req.setName("Name");
        req.setIsComposite(false);
        when(indicatorRepository.findById(77L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> indicatorService.update(77L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_success() {
        Indicator indicator = new Indicator();
        indicator.setId(1L);
        when(indicatorRepository.findById(1L)).thenReturn(Optional.of(indicator));
        when(subareaIndicatorRepository.findByIndicatorId(1L)).thenReturn(Collections.emptyList());
        indicatorService.delete(1L);
        verify(indicatorRepository).delete(indicator);
    }

    @Test
    void delete_notFound() {
        when(indicatorRepository.findById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> indicatorService.delete(404L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void assignToSubarea_success() {
        Indicator indicator = new Indicator();
        indicator.setId(1L);
        Subarea subarea = new Subarea();
        subarea.setId(10L);
        when(indicatorRepository.findById(1L)).thenReturn(Optional.of(indicator));
        when(subareaRepository.findById(10L)).thenReturn(Optional.of(subarea));
        when(subareaIndicatorRepository.existsBySubareaIdAndIndicatorId(10L, 1L)).thenReturn(false);
        when(subareaIndicatorRepository.save(any(SubareaIndicator.class))).thenAnswer(inv -> inv.getArgument(0));
        
        SubareaIndicatorRequest req = new SubareaIndicatorRequest();
        req.setDirection(Direction.INPUT);
        req.setAggregationWeight(0.5);
        
        indicatorService.assignToSubarea(1L, 10L, req);
        verify(subareaIndicatorRepository).save(any(SubareaIndicator.class));
    }

    @Test
    void assignToSubarea_alreadyAssigned() {
        Indicator indicator = new Indicator();
        indicator.setId(1L);
        Subarea subarea = new Subarea();
        subarea.setId(10L);
        when(indicatorRepository.findById(1L)).thenReturn(Optional.of(indicator));
        when(subareaRepository.findById(10L)).thenReturn(Optional.of(subarea));
        when(subareaIndicatorRepository.existsBySubareaIdAndIndicatorId(10L, 1L)).thenReturn(true);
        
        SubareaIndicatorRequest req = new SubareaIndicatorRequest();
        req.setDirection(Direction.INPUT);
        
        assertThatThrownBy(() -> indicatorService.assignToSubarea(1L, 10L, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already assigned");
    }

    @Test
    void removeFromSubarea_success() {
        when(subareaIndicatorRepository.existsBySubareaIdAndIndicatorId(10L, 1L)).thenReturn(true);
        indicatorService.removeFromSubarea(1L, 10L);
        verify(subareaIndicatorRepository).deleteById(any(SubareaIndicator.SubareaIndicatorId.class));
    }

    @Test
    void removeFromSubarea_notFound() {
        when(subareaIndicatorRepository.existsBySubareaIdAndIndicatorId(10L, 1L)).thenReturn(false);
        assertThatThrownBy(() -> indicatorService.removeFromSubarea(1L, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getIndicatorValues_fetchesAllDimensionsIncludingGenerics() {
        // Arrange
        Indicator indicator = new Indicator();
        indicator.setId(1L);
        indicator.setName("Test Indicator");
        DataType dataType = new DataType();
        dataType.setId(1L);
        dataType.setName("number");
        indicator.setDataType(dataType);
        when(indicatorRepository.findById(1L)).thenReturn(Optional.of(indicator));

        // FactIndicatorValue with time, location, and generics
        var time = new io.dashboard.model.DimTime();
        time.setId(1L);
        time.setValue("2020");
        var location = new io.dashboard.model.DimLocation();
        location.setId(1L);
        location.setName("Berlin");
        var generic = new io.dashboard.model.DimGeneric();
        generic.setId(1L);
        generic.setDimensionName("customDim");
        generic.setValue("customValue");
        var fact = new io.dashboard.model.FactIndicatorValue();
        fact.setId(100L);
        fact.setTime(time);
        fact.setLocation(location);
        fact.setGenerics(List.of(generic));
        fact.setValue(java.math.BigDecimal.valueOf(42.0));
        when(factIndicatorValueRepository.findByIndicatorIdWithGenerics(1L)).thenReturn(List.of(fact));

        // Act
        var response = indicatorService.getIndicatorValues(1L);

        // Assert
        assertThat(response.getRows()).hasSize(1);
        assertThat(response.getRows().get(0).getDimensions().get("time")).isEqualTo("2020");
        assertThat(response.getRows().get(0).getDimensions().get("location")).isEqualTo("Berlin");
        assertThat(response.getRows().get(0).getDimensions().get("customDim")).isEqualTo("customValue");
        assertThat(response.getDimensionColumns()).containsExactlyInAnyOrder("time", "location", "customDim");
        assertThat(response.getIndicatorName()).isEqualTo("Test Indicator");
        assertThat(response.getDataType()).isEqualTo("number");
    }
} 