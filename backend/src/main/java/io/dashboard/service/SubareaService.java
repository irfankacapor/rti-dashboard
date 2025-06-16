package io.dashboard.service;

import io.dashboard.dto.SubareaCreateRequest;
import io.dashboard.dto.SubareaResponse;
import io.dashboard.dto.SubareaUpdateRequest;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.Area;
import io.dashboard.model.Subarea;
import io.dashboard.repository.AreaRepository;
import io.dashboard.repository.SubareaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubareaService {
    private final SubareaRepository subareaRepository;
    private final AreaRepository areaRepository;

    public List<SubareaResponse> findAll() {
        return subareaRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<SubareaResponse> findByAreaId(Long areaId) {
        return subareaRepository.findByAreaId(areaId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public SubareaResponse findById(Long id) {
        Subarea subarea = subareaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subarea", "id", id));
        return toResponse(subarea);
    }

    @Transactional
    public SubareaResponse create(SubareaCreateRequest request) {
        if (subareaRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Subarea code must be unique");
        }
        Area area = areaRepository.findById(request.getAreaId())
                .orElseThrow(() -> new BadRequestException("Area does not exist"));
        Subarea subarea = new Subarea();
        subarea.setCode(request.getCode());
        subarea.setName(request.getName());
        subarea.setDescription(request.getDescription());
        subarea.setArea(area);
        Subarea saved = subareaRepository.save(subarea);
        return toResponse(saved);
    }

    @Transactional
    public SubareaResponse update(Long id, SubareaUpdateRequest request) {
        Subarea subarea = subareaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subarea", "id", id));
        Area area = areaRepository.findById(request.getAreaId())
                .orElseThrow(() -> new BadRequestException("Area does not exist"));
        subarea.setName(request.getName());
        subarea.setDescription(request.getDescription());
        subarea.setArea(area);
        Subarea saved = subareaRepository.save(subarea);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Subarea subarea = subareaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subarea", "id", id));
        if (subarea.getSubareaIndicators() != null && !subarea.getSubareaIndicators().isEmpty()) {
            throw new BadRequestException("Cannot delete subarea with indicators");
        }
        subareaRepository.delete(subarea);
    }

    private SubareaResponse toResponse(Subarea subarea) {
        SubareaResponse resp = new SubareaResponse();
        resp.setId(subarea.getId());
        resp.setCode(subarea.getCode());
        resp.setName(subarea.getName());
        resp.setDescription(subarea.getDescription());
        resp.setCreatedAt(subarea.getCreatedAt());
        resp.setAreaId(subarea.getArea() != null ? subarea.getArea().getId() : null);
        resp.setAreaName(subarea.getArea() != null ? subarea.getArea().getName() : null);
        resp.setIndicatorCount(subarea.getSubareaIndicators() != null ? subarea.getSubareaIndicators().size() : 0);
        return resp;
    }
} 