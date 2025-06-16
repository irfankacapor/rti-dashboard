package io.dashboard.service;

import io.dashboard.dto.UnitCreateRequest;
import io.dashboard.dto.UnitResponse;
import io.dashboard.dto.UnitUpdateRequest;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.Indicator;
import io.dashboard.model.Unit;
import io.dashboard.repository.UnitRepository;
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

class UnitServiceTest {
    @Mock
    private UnitRepository unitRepository;
    @InjectMocks
    private UnitService unitService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findAll_returnsAllUnits() {
        Unit unit = new Unit();
        unit.setId(1L);
        unit.setCode("UNIT1");
        when(unitRepository.findAll()).thenReturn(List.of(unit));
        List<UnitResponse> result = unitService.findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("UNIT1");
    }

    @Test
    void findById_found() {
        Unit unit = new Unit();
        unit.setId(1L);
        unit.setCode("UNIT1");
        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
        UnitResponse resp = unitService.findById(1L);
        assertThat(resp.getCode()).isEqualTo("UNIT1");
    }

    @Test
    void findById_notFound() {
        when(unitRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> unitService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_success() {
        UnitCreateRequest req = new UnitCreateRequest();
        req.setCode("UNIT1");
        req.setDescription("Unit 1");
        when(unitRepository.existsByCode("UNIT1")).thenReturn(false);
        when(unitRepository.save(any(Unit.class))).thenAnswer(inv -> {
            Unit u = inv.getArgument(0);
            u.setId(11L);
            return u;
        });
        UnitResponse resp = unitService.create(req);
        assertThat(resp.getId()).isEqualTo(11L);
        assertThat(resp.getCode()).isEqualTo("UNIT1");
    }

    @Test
    void create_duplicateCode() {
        UnitCreateRequest req = new UnitCreateRequest();
        req.setCode("DUP");
        when(unitRepository.existsByCode("DUP")).thenReturn(true);
        assertThatThrownBy(() -> unitService.create(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("unique");
    }

    @Test
    void update_success() {
        UnitUpdateRequest req = new UnitUpdateRequest();
        req.setDescription("Updated");
        Unit unit = new Unit();
        unit.setId(1L);
        unit.setCode("UNIT1");
        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(unitRepository.save(any(Unit.class))).thenAnswer(inv -> inv.getArgument(0));
        UnitResponse resp = unitService.update(1L, req);
        assertThat(resp.getDescription()).isEqualTo("Updated");
    }

    @Test
    void update_notFound() {
        UnitUpdateRequest req = new UnitUpdateRequest();
        req.setDescription("desc");
        when(unitRepository.findById(77L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> unitService.update(77L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_success() {
        Unit unit = new Unit();
        unit.setId(1L);
        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(unitRepository.hasIndicators(1L)).thenReturn(false);
        unitService.delete(1L);
        verify(unitRepository).delete(unit);
    }

    @Test
    void delete_withIndicators() {
        Unit unit = new Unit();
        unit.setId(1L);
        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(unitRepository.hasIndicators(1L)).thenReturn(true);
        assertThatThrownBy(() -> unitService.delete(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("indicators");
    }

    @Test
    void delete_notFound() {
        when(unitRepository.findById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> unitService.delete(404L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
} 