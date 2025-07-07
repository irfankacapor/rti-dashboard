package io.dashboard.service;

import io.dashboard.dto.DataTypeResponse;
import io.dashboard.dto.IndicatorCreateRequest;
import io.dashboard.dto.IndicatorResponse;
import io.dashboard.dto.IndicatorUpdateRequest;
import io.dashboard.dto.SubareaIndicatorRequest;
import io.dashboard.dto.SubareaIndicatorResponse;
import io.dashboard.dto.UnitResponse;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.DataType;
import io.dashboard.model.Direction;
import io.dashboard.model.FactIndicatorValue;
import io.dashboard.model.Indicator;
import io.dashboard.model.Subarea;
import io.dashboard.model.SubareaIndicator;
import io.dashboard.model.Unit;
import io.dashboard.repository.DataTypeRepository;
import io.dashboard.repository.FactIndicatorValueRepository;
import io.dashboard.repository.IndicatorRepository;
import io.dashboard.repository.SubareaIndicatorRepository;
import io.dashboard.repository.SubareaRepository;
import io.dashboard.repository.UnitRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import io.dashboard.dto.IndicatorValuesResponse;
import io.dashboard.dto.IndicatorValueUpdate;
import io.dashboard.model.DimTime;
import io.dashboard.model.DimLocation;
import io.dashboard.model.DimGeneric;
import io.dashboard.dto.IndicatorValueRow;

import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndicatorService {
    private final IndicatorRepository indicatorRepository;
    private final UnitRepository unitRepository;
    private final DataTypeRepository dataTypeRepository;
    private final SubareaRepository subareaRepository;
    private final SubareaIndicatorRepository subareaIndicatorRepository;
    private final FactIndicatorValueRepository factIndicatorValueRepository;
    private final AggregationService aggregationService;

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
    public void deleteWithData(Long id) {
        Indicator indicator = indicatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Indicator", "id", id));
        
        // Remove all FactIndicatorValue data first
        List<FactIndicatorValue> factValues = factIndicatorValueRepository.findByIndicatorId(id);
        factIndicatorValueRepository.deleteAll(factValues);
        
        // Remove all SubareaIndicator relationships
        List<SubareaIndicator> relationships = subareaIndicatorRepository.findByIndicatorId(id);
        subareaIndicatorRepository.deleteAll(relationships);
        
        // Finally delete the indicator
        indicatorRepository.delete(indicator);
        
        log.info("Deleted indicator {} with {} associated data values", id, factValues.size());
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
        
        // Get subarea indicators
        List<SubareaIndicator> subareaIndicators = subareaIndicatorRepository.findByIndicatorIdWithSubarea(indicator.getId());
        List<SubareaIndicatorResponse> subareaIndicatorResponses = subareaIndicators.stream()
                .map(this::toSubareaIndicatorResponse)
                .collect(Collectors.toList());
        resp.setSubareaIndicators(subareaIndicatorResponses);
        
        // Set subareaId, subareaName, direction from first subareaIndicator if present
        if (!subareaIndicators.isEmpty()) {
            SubareaIndicator si = subareaIndicators.get(0);
            resp.setSubareaId(si.getSubarea().getId());
            resp.setSubareaName(si.getSubarea().getName());
            resp.setDirection(si.getDirection() != null ? si.getDirection().name() : null);
        }
        
        // Set valueCount and dimensions
        long valueCount = factIndicatorValueRepository.countByIndicatorId(indicator.getId());
        resp.setValueCount(valueCount);
        List<String> dimensions = factIndicatorValueRepository.findDimensionsByIndicatorId(indicator.getId());
        resp.setDimensions(dimensions);
        
        // Calculate and set latest value
        calculateAndSetLatestValue(resp, indicator.getId());
        
        return resp;
    }
    
    private void calculateAndSetLatestValue(IndicatorResponse resp, Long indicatorId) {
        try {
            // Use the aggregation service to calculate the proper aggregated value
            double aggregatedValue = aggregationService.calculateIndicatorAggregatedValue(indicatorId);
            
            if (aggregatedValue == 0.0) {
                resp.setLatestValue(null);
                resp.setLatestValueUnit(null);
                resp.setAggregationMethod("NO_DATA");
                return;
            }
            
            resp.setLatestValue(aggregatedValue);
            
            // Get the unit from the first fact value or indicator
            List<FactIndicatorValue> values = factIndicatorValueRepository.findByIndicatorId(indicatorId);
            if (!values.isEmpty() && values.get(0).getUnit() != null) {
                resp.setLatestValueUnit(values.get(0).getUnit().getCode());
            } else if (resp.getUnit() != null) {
                resp.setLatestValueUnit(resp.getUnit().getCode());
            }
            
            resp.setAggregationMethod("AGGREGATED");
        } catch (Exception e) {
            log.warn("Error calculating latest value for indicator {}: {}", indicatorId, e.getMessage());
            resp.setLatestValue(null);
            resp.setLatestValueUnit(null);
            resp.setAggregationMethod("ERROR");
        }
    }

    private SubareaIndicatorResponse toSubareaIndicatorResponse(SubareaIndicator subareaIndicator) {
        SubareaIndicatorResponse resp = new SubareaIndicatorResponse();
        resp.setSubareaId(subareaIndicator.getId().getSubareaId());
        resp.setDirection(subareaIndicator.getDirection());
        resp.setAggregationWeight(subareaIndicator.getAggregationWeight());
        resp.setCreatedAt(subareaIndicator.getCreatedAt());
        return resp;
    }

    public IndicatorValuesResponse getIndicatorValues(Long indicatorId) {
        Indicator indicator = indicatorRepository.findById(indicatorId)
            .orElseThrow(() -> new ResourceNotFoundException("Indicator", "id", indicatorId));
        List<FactIndicatorValue> facts = factIndicatorValueRepository.findByIndicatorIdWithGenerics(indicatorId);
        List<String> dimensionColumns = new java.util.ArrayList<>();
        // Always include time and location if present
        if (facts.stream().anyMatch(f -> f.getTime() != null)) dimensionColumns.add("time");
        if (facts.stream().anyMatch(f -> f.getLocation() != null)) dimensionColumns.add("location");
        // Collect all custom dimension names
        java.util.Set<String> customDims = new java.util.HashSet<>();
        for (FactIndicatorValue fact : facts) {
            if (fact.getGenerics() != null) {
                for (DimGeneric g : fact.getGenerics()) {
                    if (g.getDimensionName() != null) customDims.add(g.getDimensionName());
                }
            }
        }
        dimensionColumns.addAll(customDims);
        List<IndicatorValueRow> rows = facts.stream().map(fact -> {
            HashMap<String, String> dims = new HashMap<>();
            if (fact.getTime() != null) dims.put("time", fact.getTime().getValue());
            if (fact.getLocation() != null) dims.put("location", fact.getLocation().getName());
            if (fact.getGenerics() != null) {
                for (DimGeneric g : fact.getGenerics()) {
                    if (g.getDimensionName() != null) dims.put(g.getDimensionName(), g.getValue());
                }
            }
            return IndicatorValueRow.builder()
                .factId(fact.getId())
                .dimensions(dims)
                .value(fact.getValue())
                .isEmpty(fact.getValue() == null)
                .build();
        }).collect(Collectors.toList());
        return IndicatorValuesResponse.builder()
            .rows(rows)
            .dimensionColumns(dimensionColumns)
            .indicatorName(indicator.getName())
            .dataType(indicator.getDataType() != null ? indicator.getDataType().getName() : null)
            .build();
    }

    public void updateIndicatorValues(Long indicatorId, List<IndicatorValueUpdate> updates) {
        for (IndicatorValueUpdate update : updates) {
            FactIndicatorValue fact = factIndicatorValueRepository.findById(update.getFactId())
                .orElseThrow(() -> new ResourceNotFoundException("FactIndicatorValue", "id", update.getFactId()));
            // Optionally: validate indicatorId matches
            if (!fact.getIndicator().getId().equals(indicatorId)) {
                throw new BadRequestException("Fact value does not belong to the specified indicator");
            }
            fact.setValue(update.getNewValue());
            factIndicatorValueRepository.save(fact);
        }
    }

    public Map<String, Double> getAggregatedByDimension(Long indicatorId, String dimension) {
        return aggregationService.getIndicatorAggregatedByDimension(indicatorId, dimension);
    }
} 