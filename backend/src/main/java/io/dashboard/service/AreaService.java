package io.dashboard.service;

import io.dashboard.dto.AreaCreateRequest;
import io.dashboard.dto.AreaResponse;
import io.dashboard.dto.AreaUpdateRequest;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.Area;
import io.dashboard.repository.AreaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AreaService {
    private final AreaRepository areaRepository;

    public List<AreaResponse> findAll() {
        return areaRepository.findAllWithSubareas().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public AreaResponse findById(Long id) {
        Area area = areaRepository.findByIdWithSubareas(id)
                .orElseThrow(() -> new ResourceNotFoundException("Area", "id", id));
        return toResponse(area);
    }

    @Transactional
    public AreaResponse create(AreaCreateRequest request) {
        if (areaRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Area code must be unique");
        }
        Area area = new Area();
        area.setCode(request.getCode());
        area.setName(request.getName());
        area.setDescription(request.getDescription());
        Area saved = areaRepository.save(area);
        return toResponse(saved);
    }

    @Transactional
    public AreaResponse update(Long id, AreaUpdateRequest request) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Area", "id", id));
        area.setName(request.getName());
        area.setDescription(request.getDescription());
        Area saved = areaRepository.save(area);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Area area = areaRepository.findByIdWithSubareas(id)
                .orElseThrow(() -> new ResourceNotFoundException("Area", "id", id));
        if (area.getSubareas() != null && !area.getSubareas().isEmpty()) {
            throw new BadRequestException("Cannot delete area with subareas");
        }
        areaRepository.delete(area);
    }

    private AreaResponse toResponse(Area area) {
        AreaResponse resp = new AreaResponse();
        resp.setId(area.getId());
        resp.setCode(area.getCode());
        resp.setName(area.getName());
        resp.setDescription(area.getDescription());
        resp.setCreatedAt(area.getCreatedAt());
        resp.setSubareaCount(area.getSubareas() != null ? area.getSubareas().size() : 0);
        return resp;
    }
} 