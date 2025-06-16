package io.dashboard.service;

import io.dashboard.dto.UnitCreateRequest;
import io.dashboard.dto.UnitResponse;
import io.dashboard.dto.UnitUpdateRequest;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.Unit;
import io.dashboard.repository.UnitRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UnitService {
    private final UnitRepository unitRepository;

    public List<UnitResponse> findAll() {
        return unitRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public UnitResponse findById(Long id) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", "id", id));
        return toResponse(unit);
    }

    @Transactional
    public UnitResponse create(UnitCreateRequest request) {
        if (unitRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Unit code must be unique");
        }
        Unit unit = new Unit();
        unit.setCode(request.getCode());
        unit.setDescription(request.getDescription());
        Unit saved = unitRepository.save(unit);
        return toResponse(saved);
    }

    @Transactional
    public UnitResponse update(Long id, UnitUpdateRequest request) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", "id", id));
        unit.setDescription(request.getDescription());
        Unit saved = unitRepository.save(unit);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", "id", id));
        if (unitRepository.hasIndicators(id)) {
            throw new BadRequestException("Cannot delete unit with indicators");
        }
        unitRepository.delete(unit);
    }

    private UnitResponse toResponse(Unit unit) {
        UnitResponse resp = new UnitResponse();
        resp.setId(unit.getId());
        resp.setCode(unit.getCode());
        resp.setDescription(unit.getDescription());
        resp.setCreatedAt(unit.getCreatedAt());
        return resp;
    }
} 