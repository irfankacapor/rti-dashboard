package io.dashboard.service;

import io.dashboard.dto.DataTypeCreateRequest;
import io.dashboard.dto.DataTypeResponse;
import io.dashboard.dto.DataTypeUpdateRequest;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.DataType;
import io.dashboard.repository.DataTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DataTypeServiceTest {
    @Mock
    private DataTypeRepository dataTypeRepository;
    @InjectMocks
    private DataTypeService dataTypeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findAll_returnsAllDataTypes() {
        DataType dataType = new DataType();
        dataType.setId(1L);
        dataType.setName("TYPE1");
        when(dataTypeRepository.findAll()).thenReturn(List.of(dataType));
        List<DataTypeResponse> result = dataTypeService.findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("TYPE1");
    }

    @Test
    void findById_found() {
        DataType dataType = new DataType();
        dataType.setId(1L);
        dataType.setName("TYPE1");
        when(dataTypeRepository.findById(1L)).thenReturn(Optional.of(dataType));
        DataTypeResponse resp = dataTypeService.findById(1L);
        assertThat(resp.getName()).isEqualTo("TYPE1");
    }

    @Test
    void findById_notFound() {
        when(dataTypeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> dataTypeService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_success() {
        DataTypeCreateRequest req = new DataTypeCreateRequest();
        req.setName("TYPE1");
        when(dataTypeRepository.save(any(DataType.class))).thenAnswer(inv -> {
            DataType dt = inv.getArgument(0);
            dt.setId(11L);
            return dt;
        });
        DataTypeResponse resp = dataTypeService.create(req);
        assertThat(resp.getId()).isEqualTo(11L);
        assertThat(resp.getName()).isEqualTo("TYPE1");
    }

    @Test
    void update_success() {
        DataTypeUpdateRequest req = new DataTypeUpdateRequest();
        req.setName("Updated");
        DataType dataType = new DataType();
        dataType.setId(1L);
        dataType.setName("TYPE1");
        when(dataTypeRepository.findById(1L)).thenReturn(Optional.of(dataType));
        when(dataTypeRepository.save(any(DataType.class))).thenAnswer(inv -> inv.getArgument(0));
        DataTypeResponse resp = dataTypeService.update(1L, req);
        assertThat(resp.getName()).isEqualTo("Updated");
    }

    @Test
    void update_notFound() {
        DataTypeUpdateRequest req = new DataTypeUpdateRequest();
        req.setName("Name");
        when(dataTypeRepository.findById(77L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> dataTypeService.update(77L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_success() {
        DataType dataType = new DataType();
        dataType.setId(1L);
        when(dataTypeRepository.findById(1L)).thenReturn(Optional.of(dataType));
        when(dataTypeRepository.hasIndicators(1L)).thenReturn(false);
        dataTypeService.delete(1L);
        verify(dataTypeRepository).delete(dataType);
    }

    @Test
    void delete_withIndicators() {
        DataType dataType = new DataType();
        dataType.setId(1L);
        when(dataTypeRepository.findById(1L)).thenReturn(Optional.of(dataType));
        when(dataTypeRepository.hasIndicators(1L)).thenReturn(true);
        assertThatThrownBy(() -> dataTypeService.delete(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("indicators");
    }

    @Test
    void delete_notFound() {
        when(dataTypeRepository.findById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> dataTypeService.delete(404L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
} 