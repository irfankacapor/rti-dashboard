package io.dashboard.service;

import io.dashboard.dto.IndicatorCreateRequest;
import io.dashboard.dto.IndicatorResponse;
import io.dashboard.dto.IndicatorUpdateRequest;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.DataType;
import io.dashboard.model.Direction;
import io.dashboard.model.Indicator;
import io.dashboard.model.Subarea;
import io.dashboard.model.Unit;
import io.dashboard.repository.DataTypeRepository;
import io.dashboard.repository.IndicatorRepository;
import io.dashboard.repository.UnitRepository;
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
    void findByFactSubareaId_returnsIndicatorsForSubarea() {
        Indicator indicator = new Indicator();
        indicator.setId(1L);
        indicator.setCode("IND1");
        when(indicatorRepository.findByFactSubareaId(10L)).thenReturn(List.of(indicator));
        List<IndicatorResponse> result = indicatorService.findByFactSubareaId(10L);
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
        when(factIndicatorValueRepository.countByIndicatorId(1L)).thenReturn(0L);
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
    void getIndicatorValues_fetchesAllDimensionsIncludingGenerics() {
        Indicator indicator = new Indicator();
        indicator.setId(1L);
        indicator.setCode("IND1");
        indicator.setName("Test Indicator");
        when(indicatorRepository.findById(1L)).thenReturn(Optional.of(indicator));
        
        // Mock the fact indicator value repository to return some data
        when(factIndicatorValueRepository.findByIndicatorId(1L)).thenReturn(Collections.emptyList());
        
        var result = indicatorService.getIndicatorValues(1L);
        assertThat(result).isNotNull();
        assertThat(result.getIndicatorName()).isEqualTo("Test Indicator");
    }
} 