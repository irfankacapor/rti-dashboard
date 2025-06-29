package io.dashboard.service;

import io.dashboard.dto.SubareaCreateRequest;
import io.dashboard.dto.SubareaResponse;
import io.dashboard.dto.SubareaUpdateRequest;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.Area;
import io.dashboard.model.Subarea;
import io.dashboard.model.SubareaIndicator;
import io.dashboard.repository.AreaRepository;
import io.dashboard.repository.SubareaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SubareaServiceTest {
    @Mock
    private SubareaRepository subareaRepository;
    @Mock
    private AreaRepository areaRepository;
    @InjectMocks
    private SubareaService subareaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Subarea subarea = new Subarea();
        subarea.setId(1L);
        subarea.setName("Test Subarea");
        subarea.setCode("S1" + System.nanoTime()); // ensure unique code
        List<Subarea> subareas = List.of(subarea);
        when(subareaRepository.findAllWithAreaAndIndicators()).thenReturn(subareas);
    }

    @Test
    void findAll_returnsAllSubareas() {
        Subarea subarea = new Subarea();
        subarea.setId(1L);
        subarea.setName("Test Subarea");
        subarea.setCode("S1" + System.nanoTime()); // ensure unique code
        when(subareaRepository.findAllWithAreaAndIndicators()).thenReturn(List.of(subarea));
        List<SubareaResponse> result = subareaService.findAll();
        assertEquals(1, result.size());
        assertEquals("Test Subarea", result.get(0).getName());
    }

    @Test
    void findByAreaId_returnsSubareasForArea() {
        Subarea sub = new Subarea();
        sub.setId(2L);
        sub.setCode("S2");
        when(subareaRepository.findByAreaId(10L)).thenReturn(List.of(sub));
        List<SubareaResponse> result = subareaService.findByAreaId(10L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(2L);
    }

    @Test
    void findById_found() {
        Subarea sub = new Subarea();
        sub.setId(3L);
        sub.setCode("S3");
        when(subareaRepository.findById(3L)).thenReturn(Optional.of(sub));
        SubareaResponse resp = subareaService.findById(3L);
        assertThat(resp.getCode()).isEqualTo("S3");
    }

    @Test
    void findById_notFound() {
        when(subareaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> subareaService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_success() {
        SubareaCreateRequest req = new SubareaCreateRequest();
        req.setCode("S4");
        req.setName("Subarea 4");
        req.setAreaId(1L);
        Area area = new Area();
        area.setId(1L);
        when(subareaRepository.existsByCode("S4")).thenReturn(false);
        when(areaRepository.findById(1L)).thenReturn(Optional.of(area));
        when(subareaRepository.save(any(Subarea.class))).thenAnswer(inv -> {
            Subarea s = inv.getArgument(0);
            s.setId(44L);
            return s;
        });
        SubareaResponse resp = subareaService.create(req);
        assertThat(resp.getId()).isEqualTo(44L);
        assertThat(resp.getCode()).isEqualTo("S4");
    }

    @Test
    void create_duplicateCode() {
        SubareaCreateRequest req = new SubareaCreateRequest();
        req.setCode("DUP");
        when(subareaRepository.existsByCode("DUP")).thenReturn(true);
        assertThatThrownBy(() -> subareaService.create(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("unique");
    }

    @Test
    void create_invalidArea() {
        SubareaCreateRequest req = new SubareaCreateRequest();
        req.setCode("S5");
        req.setAreaId(123L);
        when(subareaRepository.existsByCode("S5")).thenReturn(false);
        when(areaRepository.findById(123L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> subareaService.create(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Area does not exist");
    }

    @Test
    void update_success() {
        SubareaUpdateRequest req = new SubareaUpdateRequest();
        req.setName("Updated");
        req.setAreaId(2L);
        Area area = new Area();
        area.setId(2L);
        Subarea sub = new Subarea();
        sub.setId(5L);
        when(subareaRepository.findById(5L)).thenReturn(Optional.of(sub));
        when(areaRepository.findById(2L)).thenReturn(Optional.of(area));
        when(subareaRepository.save(any(Subarea.class))).thenAnswer(inv -> inv.getArgument(0));
        SubareaResponse resp = subareaService.update(5L, req);
        assertThat(resp.getName()).isEqualTo("Updated");
        assertThat(resp.getAreaId()).isEqualTo(2L);
    }

    @Test
    void update_notFound() {
        SubareaUpdateRequest req = new SubareaUpdateRequest();
        req.setAreaId(1L);
        when(subareaRepository.findById(77L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> subareaService.update(77L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_invalidArea() {
        SubareaUpdateRequest req = new SubareaUpdateRequest();
        req.setAreaId(8L);
        Subarea sub = new Subarea();
        sub.setId(8L);
        when(subareaRepository.findById(8L)).thenReturn(Optional.of(sub));
        when(areaRepository.findById(8L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> subareaService.update(8L, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Area does not exist");
    }

    @Test
    void delete_success() {
        Subarea sub = new Subarea();
        sub.setId(9L);
        sub.setSubareaIndicators(Collections.emptyList());
        when(subareaRepository.findById(9L)).thenReturn(Optional.of(sub));
        subareaService.delete(9L);
        verify(subareaRepository).delete(sub);
    }

    @Test
    void delete_withIndicators() {
        Subarea sub = new Subarea();
        sub.setId(10L);
        SubareaIndicator indicator = new SubareaIndicator();
        sub.setSubareaIndicators(List.of(indicator));
        when(subareaRepository.findById(10L)).thenReturn(Optional.of(sub));
        assertThatThrownBy(() -> subareaService.delete(10L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("indicators");
    }

    @Test
    void delete_notFound() {
        when(subareaRepository.findById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> subareaService.delete(404L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
} 