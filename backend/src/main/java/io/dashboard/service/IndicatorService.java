package io.dashboard.service;

import io.dashboard.dto.DataTypeResponse;
import io.dashboard.dto.IndicatorCreateRequest;
import io.dashboard.dto.IndicatorResponse;
import io.dashboard.dto.IndicatorUpdateRequest;
import io.dashboard.dto.SubareaIndicatorRequest;
import io.dashboard.dto.SubareaIndicatorResponse;
import io.dashboard.dto.UnitResponse;
import io.dashboard.dto.BatchIndicatorsRequest;
import io.dashboard.dto.ProcessedIndicatorRequest;
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
import io.dashboard.repository.SubareaIndicatorRepository;
import io.dashboard.repository.SubareaRepository;
import io.dashboard.repository.UnitRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IndicatorService {
    private final IndicatorRepository indicatorRepository;
    private final UnitRepository unitRepository;
    private final DataTypeRepository dataTypeRepository;
    private final SubareaRepository subareaRepository;
    private final SubareaIndicatorRepository subareaIndicatorRepository;

    public List<IndicatorResponse> findAll() {
        return indicatorRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public IndicatorResponse findById(Long id) {
        Indicator indicator = indicatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Indicator", "id", id));
        return toResponse(indicator);
    }

    public List<IndicatorResponse> findBySubareaId(Long subareaId) {
        return indicatorRepository.findBySubareaId(subareaId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public IndicatorResponse create(IndicatorCreateRequest request) {
        if (indicatorRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Indicator code must be unique");
        }
        
        Indicator indicator = new Indicator();
        indicator.setCode(request.getCode());
        indicator.setName(request.getName());
        indicator.setDescription(request.getDescription());
        indicator.setIsComposite(request.getIsComposite());
        
        if (request.getUnitId() != null) {
            Unit unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new BadRequestException("Unit does not exist"));
            indicator.setUnit(unit);
        }
        
        if (request.getDataTypeId() != null) {
            DataType dataType = dataTypeRepository.findById(request.getDataTypeId())
                    .orElseThrow(() -> new BadRequestException("DataType does not exist"));
            indicator.setDataType(dataType);
        }
        
        Indicator saved = indicatorRepository.save(indicator);
        return toResponse(saved);
    }

    @Transactional
    public List<IndicatorResponse> createBatch(BatchIndicatorsRequest request) {
        return request.getIndicators().stream()
                .map(this::createFromProcessedIndicator)
                .collect(Collectors.toList());
    }

    @Transactional
    public IndicatorResponse createFromProcessedIndicator(ProcessedIndicatorRequest request) {
        // Generate a unique code from the name
        String code = generateCodeFromName(request.getName());
        
        // Check if code already exists, if so, make it unique
        int counter = 1;
        String uniqueCode = code;
        while (indicatorRepository.existsByCode(uniqueCode)) {
            uniqueCode = code + "_" + counter++;
        }
        
        Indicator indicator = new Indicator();
        indicator.setCode(uniqueCode);
        indicator.setName(request.getName());
        indicator.setDescription("Generated from CSV processing");
        indicator.setIsComposite(false);
        
        // If unit is provided, try to find it
        if (request.getUnit() != null && !request.getUnit().trim().isEmpty()) {
            Optional<Unit> unit = unitRepository.findByCode(request.getUnit());
            unit.ifPresent(indicator::setUnit);
        }
        
        Indicator saved = indicatorRepository.save(indicator);
        
        // If subareaId is provided, assign the indicator to the subarea
        if (request.getSubareaId() != null && !request.getSubareaId().trim().isEmpty()) {
            try {
                Long subareaId = Long.parseLong(request.getSubareaId());
                Subarea subarea = subareaRepository.findById(subareaId)
                        .orElse(null);
                
                if (subarea != null) {
                    SubareaIndicator subareaIndicator = new SubareaIndicator();
                    SubareaIndicator.SubareaIndicatorId id = new SubareaIndicator.SubareaIndicatorId();
                    id.setSubareaId(subareaId);
                    id.setIndicatorId(saved.getId());
                    subareaIndicator.setId(id);
                    subareaIndicator.setSubarea(subarea);
                    subareaIndicator.setIndicator(saved);
                    
                    // Set direction based on request
                    if ("input".equals(request.getDirection())) {
                        subareaIndicator.setDirection(Direction.INPUT);
                    } else if ("output".equals(request.getDirection())) {
                        subareaIndicator.setDirection(Direction.OUTPUT);
                    } else {
                        subareaIndicator.setDirection(Direction.INPUT); // Default
                    }
                    
                    subareaIndicator.setAggregationWeight(1.0); // Default weight
                    subareaIndicatorRepository.save(subareaIndicator);
                }
            } catch (NumberFormatException e) {
                // Ignore invalid subareaId
            }
        }
        
        return toResponse(saved);
    }

    private String generateCodeFromName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "IND_" + System.currentTimeMillis();
        }
        
        // Convert to uppercase, replace spaces and special chars with underscores
        String code = name.trim()
                .toUpperCase()
                .replaceAll("[^A-Z0-9\\s]", "")
                .replaceAll("\\s+", "_");
        
        // Limit length and ensure it starts with a letter
        if (code.length() > 50) {
            code = code.substring(0, 50);
        }
        
        if (!code.matches("^[A-Z].*")) {
            code = "IND_" + code;
        }
        
        return code;
    }

    @Transactional
    public IndicatorResponse update(Long id, IndicatorUpdateRequest request) {
        Indicator indicator = indicatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Indicator", "id", id));
        
        indicator.setName(request.getName());
        indicator.setDescription(request.getDescription());
        indicator.setIsComposite(request.getIsComposite());
        
        if (request.getUnitId() != null) {
            Unit unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new BadRequestException("Unit does not exist"));
            indicator.setUnit(unit);
        } else {
            indicator.setUnit(null);
        }
        
        if (request.getDataTypeId() != null) {
            DataType dataType = dataTypeRepository.findById(request.getDataTypeId())
                    .orElseThrow(() -> new BadRequestException("DataType does not exist"));
            indicator.setDataType(dataType);
        } else {
            indicator.setDataType(null);
        }
        
        Indicator saved = indicatorRepository.save(indicator);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Indicator indicator = indicatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Indicator", "id", id));
        
        // Remove all SubareaIndicator relationships first
        List<SubareaIndicator> relationships = subareaIndicatorRepository.findByIndicatorId(id);
        subareaIndicatorRepository.deleteAll(relationships);
        
        indicatorRepository.delete(indicator);
    }

    @Transactional
    public void assignToSubarea(Long indicatorId, Long subareaId, SubareaIndicatorRequest request) {
        Indicator indicator = indicatorRepository.findById(indicatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Indicator", "id", indicatorId));
        
        Subarea subarea = subareaRepository.findById(subareaId)
                .orElseThrow(() -> new ResourceNotFoundException("Subarea", "id", subareaId));
        
        if (subareaIndicatorRepository.existsBySubareaIdAndIndicatorId(subareaId, indicatorId)) {
            throw new BadRequestException("Indicator is already assigned to this subarea");
        }
        
        SubareaIndicator subareaIndicator = new SubareaIndicator();
        SubareaIndicator.SubareaIndicatorId id = new SubareaIndicator.SubareaIndicatorId();
        id.setSubareaId(subareaId);
        id.setIndicatorId(indicatorId);
        subareaIndicator.setId(id);
        subareaIndicator.setSubarea(subarea);
        subareaIndicator.setIndicator(indicator);
        subareaIndicator.setDirection(request.getDirection());
        subareaIndicator.setAggregationWeight(request.getAggregationWeight());
        
        subareaIndicatorRepository.save(subareaIndicator);
    }

    @Transactional
    public void removeFromSubarea(Long indicatorId, Long subareaId) {
        if (!subareaIndicatorRepository.existsBySubareaIdAndIndicatorId(subareaId, indicatorId)) {
            throw new ResourceNotFoundException("SubareaIndicator", "subareaId and indicatorId", 
                    subareaId + " and " + indicatorId);
        }
        
        SubareaIndicator.SubareaIndicatorId id = new SubareaIndicator.SubareaIndicatorId();
        id.setSubareaId(subareaId);
        id.setIndicatorId(indicatorId);
        subareaIndicatorRepository.deleteById(id);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Indicator findIndicatorWithGoals(Long indicatorId) {
        Indicator indicator = indicatorRepository.findByIdWithGoals(indicatorId);
        if (indicator == null) {
            throw new ResourceNotFoundException("Indicator", "id", indicatorId);
        }
        return indicator;
    }

    private IndicatorResponse toResponse(Indicator indicator) {
        IndicatorResponse resp = new IndicatorResponse();
        resp.setId(indicator.getId());
        resp.setCode(indicator.getCode());
        resp.setName(indicator.getName());
        resp.setDescription(indicator.getDescription());
        resp.setIsComposite(indicator.getIsComposite());
        resp.setCreatedAt(indicator.getCreatedAt());
        
        if (indicator.getUnit() != null) {
            UnitResponse unitResp = new UnitResponse();
            unitResp.setId(indicator.getUnit().getId());
            unitResp.setCode(indicator.getUnit().getCode());
            unitResp.setDescription(indicator.getUnit().getDescription());
            unitResp.setCreatedAt(indicator.getUnit().getCreatedAt());
            resp.setUnit(unitResp);
        }
        
        if (indicator.getDataType() != null) {
            DataTypeResponse dataTypeResp = new DataTypeResponse();
            dataTypeResp.setId(indicator.getDataType().getId());
            dataTypeResp.setCode(indicator.getDataType().getName());
            dataTypeResp.setName(indicator.getDataType().getName());
            dataTypeResp.setDescription(null);
            dataTypeResp.setCreatedAt(indicator.getDataType().getCreatedAt());
            resp.setDataType(dataTypeResp);
        }
        
        if (indicator.getSubareaIndicators() != null) {
            List<SubareaIndicatorResponse> subareaIndicators = indicator.getSubareaIndicators().stream()
                    .map(this::toSubareaIndicatorResponse)
                    .collect(Collectors.toList());
            resp.setSubareaIndicators(subareaIndicators);
        }
        
        return resp;
    }

    private SubareaIndicatorResponse toSubareaIndicatorResponse(SubareaIndicator subareaIndicator) {
        SubareaIndicatorResponse resp = new SubareaIndicatorResponse();
        resp.setSubareaId(subareaIndicator.getSubarea().getId());
        resp.setSubareaCode(subareaIndicator.getSubarea().getCode());
        resp.setSubareaName(subareaIndicator.getSubarea().getName());
        resp.setDirection(subareaIndicator.getDirection());
        resp.setAggregationWeight(subareaIndicator.getAggregationWeight());
        resp.setCreatedAt(subareaIndicator.getCreatedAt());
        return resp;
    }
} 