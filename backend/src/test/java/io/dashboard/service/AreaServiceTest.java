package io.dashboard.service;

import io.dashboard.dto.AreaCreateRequest;
import io.dashboard.dto.AreaResponse;
import io.dashboard.dto.AreaUpdateRequest;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.Area;
import io.dashboard.repository.AreaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class AreaServiceTest {
    @Mock
    private AreaRepository areaRepository;

    @InjectMocks
    private AreaService areaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_shouldThrowException_whenCodeExists() {
        AreaCreateRequest req = new AreaCreateRequest();
        req.setCode("A1");
        req.setName("Area 1");
        when(areaRepository.existsByCode("A1")).thenReturn(true);
        assertThatThrownBy(() -> areaService.create(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("unique");
    }

    @Test
    void create_shouldSaveArea_whenCodeIsUnique() {
        AreaCreateRequest req = new AreaCreateRequest();
        req.setCode("A2");
        req.setName("Area 2");
        req.setDescription("desc");
        when(areaRepository.existsByCode("A2")).thenReturn(false);
        Area area = new Area();
        area.setId(1L);
        area.setCode("A2");
        area.setName("Area 2");
        area.setDescription("desc");
        when(areaRepository.save(any(Area.class))).thenReturn(area);
        AreaResponse resp = areaService.create(req);
        assertThat(resp.getId()).isEqualTo(1L);
        assertThat(resp.getCode()).isEqualTo("A2");
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(areaRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> areaService.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_shouldUpdateFields() {
        Area area = new Area();
        area.setId(1L);
        area.setCode("A3");
        area.setName("Old");
        area.setDescription("old");
        when(areaRepository.findById(1L)).thenReturn(Optional.of(area));
        when(areaRepository.save(any(Area.class))).thenAnswer(i -> i.getArgument(0));
        AreaUpdateRequest req = new AreaUpdateRequest();
        req.setName("New");
        req.setDescription("new");
        AreaResponse resp = areaService.update(1L, req);
        assertThat(resp.getName()).isEqualTo("New");
        assertThat(resp.getDescription()).isEqualTo("new");
    }

    @Test
    void delete_shouldThrow_whenAreaHasSubareas() {
        Area area = new Area();
        area.setId(1L);
        area.setCode("A4");
        area.setName("Area 4");
        area.setDescription("desc");
        area.setSubareas(Collections.singletonList(mock(io.dashboard.model.Subarea.class)));
        when(areaRepository.findByIdWithSubareas(1L)).thenReturn(Optional.of(area));
        assertThatThrownBy(() -> areaService.delete(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("subareas");
    }

    @Test
    void delete_shouldDelete_whenNoSubareas() {
        Area area = new Area();
        area.setId(1L);
        area.setCode("A5");
        area.setName("Area 5");
        area.setDescription("desc");
        area.setSubareas(Collections.emptyList());
        when(areaRepository.findByIdWithSubareas(1L)).thenReturn(Optional.of(area));
        areaService.delete(1L);
        verify(areaRepository).delete(area);
    }
} 