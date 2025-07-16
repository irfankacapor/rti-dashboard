package io.dashboard.service;

import io.dashboard.dto.DataTypeCreateRequest;
import io.dashboard.dto.DataTypeResponse;
import io.dashboard.dto.DataTypeUpdateRequest;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.DataType;
import io.dashboard.repository.DataTypeRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataTypeService {
    private final DataTypeRepository dataTypeRepository;

    @Transactional(readOnly = true)
    public List<DataTypeResponse> findAll() {
        return dataTypeRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DataTypeResponse findById(Long id) {
        DataType dataType = dataTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DataType", "id", id));
        return toResponse(dataType);
    }

    @Transactional
    public DataTypeResponse create(DataTypeCreateRequest request) {
        DataType dataType = new DataType();
        dataType.setName(request.getName());
        DataType saved = dataTypeRepository.save(dataType);
        return toResponse(saved);
    }

    @Transactional
    public DataTypeResponse update(Long id, DataTypeUpdateRequest request) {
        DataType dataType = dataTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DataType", "id", id));
        dataType.setName(request.getName());
        DataType saved = dataTypeRepository.save(dataType);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        DataType dataType = dataTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DataType", "id", id));
        if (dataTypeRepository.hasIndicators(id)) {
            throw new BadRequestException("Cannot delete data type with indicators");
        }
        dataTypeRepository.delete(dataType);
    }

    private DataTypeResponse toResponse(DataType dataType) {
        DataTypeResponse resp = new DataTypeResponse();
        resp.setId(dataType.getId());
        resp.setCode(dataType.getName());
        resp.setName(dataType.getName());
        resp.setDescription(null);
        resp.setCreatedAt(dataType.getCreatedAt());
        return resp;
    }
} 