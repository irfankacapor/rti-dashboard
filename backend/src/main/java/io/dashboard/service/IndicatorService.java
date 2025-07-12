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
import io.dashboard.dto.HistoricalDataResponse;
import io.dashboard.dto.HistoricalDataPoint;
import io.dashboard.dto.DataValidationResponse;
import io.dashboard.repository.DimTimeRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Random;

import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;
import io.dashboard.dto.IndicatorChartResponse;
import io.dashboard.dto.IndicatorDimensionsResponse;
import java.math.BigDecimal;
import io.dashboard.dto.IndicatorValueCreate;
import io.dashboard.model.DimensionType;
import java.util.Optional;

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
    private final DimTimeRepository dimTimeRepository;
    private final io.dashboard.repository.DimLocationRepository dimLocationRepository;

    public List<IndicatorResponse> findAll() {
        return indicatorRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public IndicatorResponse findById(Long id) {
        Indicator indicator = indicatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Indicator", "id", id));
        return toResponse(indicator);
    }

    public List<IndicatorResponse> findBySubareaId(Long subareaId) {
        return indicatorRepository.findBySubareaId(subareaId).stream()
            .map(indicator -> toResponse(indicator, subareaId))
            .collect(Collectors.toList());
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
        return toResponse(indicator, null);
    }

    private IndicatorResponse toResponse(Indicator indicator, Long subareaId) {
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
        
        // Set valueCount and dimensions - filter by subarea if provided
        if (subareaId != null) {
            // ✅ CORRECT: Count values only for this indicator in this subarea
            long valueCount = factIndicatorValueRepository.countByIndicatorIdAndSubareaId(indicator.getId(), subareaId);
            resp.setValueCount(valueCount);
            
            // ✅ CORRECT: Discover dimensions only from facts for this indicator in this subarea
            List<String> dimensions = factIndicatorValueRepository.findDimensionsByIndicatorIdAndSubareaId(indicator.getId(), subareaId);
            resp.setDimensions(dimensions);
            
            // ✅ CORRECT: Calculate latest value only for this subarea
            calculateAndSetLatestValueForSubarea(resp, indicator.getId(), subareaId);
        } else {
            // Original behavior for non-subarea context
            long valueCount = factIndicatorValueRepository.countByIndicatorId(indicator.getId());
            resp.setValueCount(valueCount);
            List<String> dimensions = factIndicatorValueRepository.findDimensionsByIndicatorId(indicator.getId());
            resp.setDimensions(dimensions);
            calculateAndSetLatestValue(resp, indicator.getId());
        }
        
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

    private void calculateAndSetLatestValueForSubarea(IndicatorResponse resp, Long indicatorId, Long subareaId) {
        try {
            // Use the aggregation service to calculate the proper aggregated value for the subarea
            double aggregatedValue = aggregationService.calculateIndicatorAggregatedValue(indicatorId, subareaId);
            
            if (aggregatedValue == 0.0) {
                resp.setLatestValue(null);
                resp.setLatestValueUnit(null);
                resp.setAggregationMethod("NO_DATA");
                return;
            }
            
            resp.setLatestValue(aggregatedValue);
            
            // Get the unit from the first fact value or indicator
            List<FactIndicatorValue> values = factIndicatorValueRepository.findByIndicatorIdAndSubareaId(indicatorId, subareaId);
            if (!values.isEmpty() && values.get(0).getUnit() != null) {
                resp.setLatestValueUnit(values.get(0).getUnit().getCode());
            } else if (resp.getUnit() != null) {
                resp.setLatestValueUnit(resp.getUnit().getCode());
            }
            
            resp.setAggregationMethod("AGGREGATED");
        } catch (Exception e) {
            log.warn("Error calculating latest value for indicator {} in subarea {}: {}", indicatorId, subareaId, e.getMessage());
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

    public void createIndicatorValues(Long indicatorId, List<IndicatorValueCreate> newValues) {
        Indicator indicator = indicatorRepository.findById(indicatorId)
            .orElseThrow(() -> new ResourceNotFoundException("Indicator", "id", indicatorId));
        
        for (IndicatorValueCreate newValue : newValues) {
            FactIndicatorValue fact = new FactIndicatorValue();
            fact.setIndicator(indicator);
            fact.setValue(newValue.getValue());
            
            // Handle time dimension (reuse if exists)
            if (newValue.getDimensions().containsKey("time")) {
                String timeValue = newValue.getDimensions().get("time");
                DimTime time = parseTimeValue(timeValue);
                fact.setTime(time);
            }
            
            // Handle location dimension (reuse if exists)
            if (newValue.getDimensions().containsKey("location")) {
                String locationName = newValue.getDimensions().get("location");
                DimLocation location = dimLocationRepository.findByName(locationName).orElse(null);
                if (location == null) {
                    location = DimLocation.builder().name(locationName).build();
                    location = dimLocationRepository.save(location);
                }
                fact.setLocation(location);
            }
            
            // Handle generic dimensions (not yet implemented)
            // ...
            
            fact.setSourceRowHash("manual-" + System.currentTimeMillis() + "-" + Math.random());
            factIndicatorValueRepository.save(fact);
        }
    }

    private Integer extractYear(String timeValue) {
        try {
            return Integer.parseInt(timeValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private DimTime parseTimeValue(String timeValue) {
        if (timeValue == null || timeValue.trim().isEmpty()) {
            return null;
        }
        
        // Try to find existing time record first
        Optional<DimTime> existing = dimTimeRepository.findByValue(timeValue);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        // Parse the time value
        Integer year = null;
        Integer month = null;
        Integer day = null;
        DimensionType timeType = DimensionType.TIME;
        
        // Handle different time formats
        if (timeValue.matches("^\\d{4}$")) {
            // Year only: "2024"
            year = Integer.parseInt(timeValue);
        } else if (timeValue.matches("^\\d{4}-\\d{1,2}$")) {
            // Year-Month: "2024-01" or "2024-1"
            String[] parts = timeValue.split("-");
            year = Integer.parseInt(parts[0]);
            month = Integer.parseInt(parts[1]);
        } else if (timeValue.matches("^\\d{4}-\\d{1,2}-\\d{1,2}$")) {
            // Year-Month-Day: "2024-01-15" or "2024-1-5"
            String[] parts = timeValue.split("-");
            year = Integer.parseInt(parts[0]);
            month = Integer.parseInt(parts[1]);
            day = Integer.parseInt(parts[2]);
        } else {
            // Try to extract year from any format
            try {
                year = Integer.parseInt(timeValue);
            } catch (NumberFormatException e) {
                // If we can't parse it, just store the original value
                return DimTime.builder()
                    .value(timeValue)
                    .timeType(DimensionType.TIME)
                    .build();
            }
        }
        
        // Validate parsed values
        if (year != null && (year < 1900 || year > 2100)) {
            throw new BadRequestException("Year must be between 1900 and 2100");
        }
        if (month != null && (month < 1 || month > 12)) {
            throw new BadRequestException("Month must be between 1 and 12");
        }
        if (day != null && (day < 1 || day > 31)) {
            throw new BadRequestException("Day must be between 1 and 31");
        }
        
        // Create new time record
        DimTime dimTime = DimTime.builder()
            .value(timeValue)
            .timeType(timeType)
            .year(year)
            .month(month)
            .day(day)
            .build();
        
        return dimTimeRepository.save(dimTime);
    }

    public Map<String, Double> getAggregatedByDimension(Long indicatorId, String dimension) {
        return aggregationService.getIndicatorAggregatedByDimension(indicatorId, dimension);
    }

    public HistoricalDataResponse getHistoricalData(Long indicatorId, int months, String range, String dimension) {
        // Convert range to months if provided
        int monthsToFetch = months;
        if (range != null && !range.isEmpty()) {
            monthsToFetch = convertRangeToMonths(range);
        }
        return getHistoricalData(indicatorId, monthsToFetch, dimension);
    }

    private int convertRangeToMonths(String range) {
        if (range == null || range.isEmpty()) {
            return 12; // default
        }
        String unit = range.substring(range.length() - 1).toUpperCase();
        int value = Integer.parseInt(range.substring(0, range.length() - 1));
        switch (unit) {
            case "Y":
                return value * 12;
            case "M":
                return value;
            case "W":
                return (int) Math.ceil(value / 4.0); // approximate weeks to months
            case "D":
                return (int) Math.ceil(value / 30.0); // approximate days to months
            default:
                return 12; // default fallback
        }
    }

    public HistoricalDataResponse getHistoricalData(Long indicatorId, int months, String dimension) {
        log.debug("Fetching historical data for indicator ID: {} for {} months, dimension: {}", indicatorId, months, dimension);
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(months);
        if (!indicatorRepository.existsById(indicatorId)) {
            log.warn("Indicator with ID {} not found", indicatorId);
            HistoricalDataResponse response = new HistoricalDataResponse();
            response.setIndicatorId(indicatorId);
            response.setDataPoints(new ArrayList<>());
            response.setStartDate(startDate);
            response.setEndDate(endDate);
            return response;
        }
        List<FactIndicatorValue> values;
        List<String> availableDimensions = new ArrayList<>();
        if (dimension != null && !dimension.isEmpty()) {
            switch (dimension.toLowerCase()) {
                case "time":
                    values = factIndicatorValueRepository.findByIndicatorIdWithTime(indicatorId);
                    availableDimensions.add("time");
                    break;
                case "location":
                    values = factIndicatorValueRepository.findByIndicatorIdWithEagerLoading(indicatorId);
                    availableDimensions.add("location");
                    break;
                default:
                    values = factIndicatorValueRepository.findByIndicatorIdWithGenerics(indicatorId);
                    availableDimensions.add(dimension);
                    break;
            }
        } else {
            values = factIndicatorValueRepository.findByIndicatorIdWithGenerics(indicatorId);
            if (values.stream().anyMatch(v -> v.getTime() != null)) {
                availableDimensions.add("time");
            }
            if (values.stream().anyMatch(v -> v.getLocation() != null)) {
                availableDimensions.add("location");
            }
            Set<String> customDims = values.stream()
                .filter(v -> v.getGenerics() != null)
                .flatMap(v -> v.getGenerics().stream())
                .filter(g -> g.getDimensionName() != null)
                .map(DimGeneric::getDimensionName)
                .collect(Collectors.toSet());
            availableDimensions.addAll(customDims);
        }
        List<HistoricalDataPoint> dataPoints = values.stream()
                .map(v -> {
                    HistoricalDataPoint point = new HistoricalDataPoint();
                    Map<String, String> dimensions = new HashMap<>();
                    if (v.getTime() != null) {
                        dimensions.put("time", v.getTime().getValue());
                    }
                    if (v.getLocation() != null) {
                        dimensions.put("location", v.getLocation().getName());
                    }
                    if (v.getGenerics() != null) {
                        for (DimGeneric generic : v.getGenerics()) {
                            if (generic.getDimensionName() != null) {
                                dimensions.put(generic.getDimensionName(), generic.getValue());
                            }
                        }
                    }
                    if (dimension != null && !dimension.isEmpty()) {
                        switch (dimension.toLowerCase()) {
                            case "time":
                                point.setTimestamp(v.getTime() != null ? v.getTime().getValue() : "Unknown");
                                break;
                            case "location":
                                point.setTimestamp(v.getLocation() != null ? v.getLocation().getName() : "Unknown");
                                break;
                            default:
                                String dimensionValue = v.getGenerics() != null ? 
                                    v.getGenerics().stream()
                                        .filter(g -> dimension.equals(g.getDimensionName()))
                                        .map(DimGeneric::getValue)
                                        .findFirst()
                                        .orElse("Unknown") : "Unknown";
                                point.setTimestamp(dimensionValue);
                                break;
                        }
                    } else {
                        point.setTimestamp(v.getTime() != null ? v.getTime().getValue() : "Unknown");
                    }
                    point.setValue(v.getValue().doubleValue());
                    point.setDimensions(dimensions);
                    return point;
                })
                .collect(Collectors.toList());
        HistoricalDataResponse response = new HistoricalDataResponse();
        response.setIndicatorId(indicatorId);
        response.setDataPoints(dataPoints);
        response.setStartDate(startDate);
        response.setEndDate(endDate);
        response.setDimensions(availableDimensions);
        return response;
    }

    public DataValidationResponse getDataValidation(Long indicatorId) {
        log.debug("Validating data for indicator ID: {}", indicatorId);
        indicatorRepository.findById(indicatorId)
                .orElseThrow(() -> new RuntimeException("Indicator not found"));
        List<FactIndicatorValue> values = factIndicatorValueRepository.findByIndicatorId(indicatorId);
        long totalRecords = values.size();
        long validRecords = values.stream()
                .filter(v -> v.getValue() != null && v.getValue().doubleValue() >= 0)
                .count();
        long invalidRecords = totalRecords - validRecords;
        List<String> validationErrors = new ArrayList<>();
        if (invalidRecords > 0) {
            validationErrors.add("Found " + invalidRecords + " invalid records");
        }
        DataValidationResponse response = new DataValidationResponse();
        response.setIndicatorId(indicatorId);
        response.setTotalRecords(totalRecords);
        response.setValidRecords(validRecords);
        response.setInvalidRecords(invalidRecords);
        response.setValidationErrors(validationErrors);
        response.setIsValid(invalidRecords == 0);
        return response;
    }

    public DataValidationResponse validateIndicatorData(Long indicatorId) {
        return getDataValidation(indicatorId);
    }

    public IndicatorChartResponse getIndicatorChart(Long indicatorId) {
        // Default implementation - use time as aggregation
        return getIndicatorChart(indicatorId, "time", null);
    }

    public HistoricalDataResponse createSampleHistoricalData(Long indicatorId) {
        log.info("Creating sample historical data for indicator ID: {}", indicatorId);
        if (!indicatorRepository.existsById(indicatorId)) {
            throw new BadRequestException("Indicator not found with ID: " + indicatorId);
        }
        LocalDateTime now = LocalDateTime.now();
        List<FactIndicatorValue> sampleValues = new ArrayList<>();
        Random random = new Random();
        for (int i = 11; i >= 0; i--) {
            LocalDateTime date = now.minusMonths(i);
            DimTime time = DimTime.builder()
                .value(date.getYear() + "-" + String.format("%02d", date.getMonthValue()))
                .year(date.getYear())
                .month(date.getMonthValue())
                .day(1)
                .build();
            time = dimTimeRepository.save(time);
            double randomValue = 50 + random.nextDouble() * 50;
            FactIndicatorValue fact = FactIndicatorValue.builder()
                .indicator(indicatorRepository.findById(indicatorId).get())
                .value(java.math.BigDecimal.valueOf(randomValue))
                .time(time)
                .sourceRowHash("sample-" + indicatorId + "-" + i)
                .build();
            sampleValues.add(factIndicatorValueRepository.save(fact));
        }
        return getHistoricalData(indicatorId, 12, null);
    }

    public IndicatorChartResponse getIndicatorChart(Long indicatorId, String aggregateBy, Long subareaId) {
        try {
            // Validate indicator
            Indicator indicator = indicatorRepository.findById(indicatorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Indicator", "id", indicatorId));
            List<FactIndicatorValue> values;
            if (subareaId != null) {
                values = factIndicatorValueRepository.findBySubareaIdWithEagerLoading(subareaId);
                values = values.stream().filter(v -> v.getIndicator().getId().equals(indicatorId)).collect(Collectors.toList());
            } else {
                values = factIndicatorValueRepository.findByIndicatorIdWithGenerics(indicatorId);
            }
            Map<String, List<FactIndicatorValue>> grouped;
            String aggregationType = aggregateBy.toLowerCase();
            switch (aggregationType) {
                case "time":
                    grouped = values.stream().filter(v -> v.getTime() != null)
                            .collect(Collectors.groupingBy(v -> v.getTime().getValue()));
                    break;
                case "location":
                    grouped = values.stream().filter(v -> v.getLocation() != null)
                            .collect(Collectors.groupingBy(v -> v.getLocation().getName()));
                    break;
                default:
                    // Custom dimension
                    grouped = values.stream().filter(v -> v.getGenerics() != null && v.getGenerics().stream().anyMatch(g -> g.getDimensionName() != null && g.getDimensionName().equals(aggregateBy)))
                            .collect(Collectors.groupingBy(v -> v.getGenerics().stream().filter(g -> g.getDimensionName() != null && g.getDimensionName().equals(aggregateBy)).map(DimGeneric::getValue).findFirst().orElse("Unknown")));
                    break;
            }
            List<IndicatorChartResponse.ChartDataPoint> dataPoints = grouped.entrySet().stream()
                    .map(entry -> IndicatorChartResponse.ChartDataPoint.builder()
                            .label(entry.getKey())
                            .value(BigDecimal.valueOf(entry.getValue().stream().mapToDouble(v -> v.getValue().doubleValue()).average().orElse(0.0)))
                            .dimensionValue(entry.getKey())
                            .build())
                    .sorted((a, b) -> a.getLabel().compareToIgnoreCase(b.getLabel()))
                    .collect(Collectors.toList());
            // Get available dimensions
            List<String> availableDimensions = factIndicatorValueRepository.findDimensionsByIndicatorId(indicatorId);
            return IndicatorChartResponse.builder()
                    .indicatorId(String.valueOf(indicatorId))
                    .indicatorName(indicator.getName())
                    .unit(indicator.getUnit() != null ? indicator.getUnit().getCode() : null)
                    .aggregationType(aggregateBy)
                    .dataPoints(dataPoints)
                    .availableDimensions(availableDimensions)
                    .build();
        } catch (Exception e) {
            log.error("Error in getIndicatorChart: {}", e.getMessage(), e);
            throw new BadRequestException("Chart aggregation failed: " + e.getMessage());
        }
    }

    public IndicatorDimensionsResponse getIndicatorDimensions(Long indicatorId) {
        Indicator indicator = indicatorRepository.findById(indicatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Indicator", "id", indicatorId));
        List<String> dims = factIndicatorValueRepository.findDimensionsByIndicatorId(indicatorId);
        List<IndicatorDimensionsResponse.DimensionInfo> dimensionInfos = new ArrayList<>();
        for (String dim : dims) {
            List<String> values = new ArrayList<>();
            switch (dim) {
                case "time":
                    List<DimTime> times = dimTimeRepository.findByYear(indicator.getCreatedAt().getYear());
                    for (DimTime t : times) {
                        values.add(t.getValue());
                    }
                    dimensionInfos.add(IndicatorDimensionsResponse.DimensionInfo.builder()
                            .type("time").displayName("Time").values(values).build());
                    break;
                case "location":
                    // Not implemented: would need DimLocationRepository
                    dimensionInfos.add(IndicatorDimensionsResponse.DimensionInfo.builder()
                            .type("location").displayName("Location").values(new ArrayList<>()).build());
                    break;
                default:
                    // Custom dimension: get all values from FactIndicatorValue generics
                    List<FactIndicatorValue> facts = factIndicatorValueRepository.findByIndicatorIdWithGenerics(indicatorId);
                    Set<String> customValues = new HashSet<>();
                    for (FactIndicatorValue f : facts) {
                        if (f.getGenerics() != null) {
                            for (DimGeneric g : f.getGenerics()) {
                                if (dim.equals(g.getDimensionName())) {
                                    customValues.add(g.getValue());
                                }
                            }
                        }
                    }
                    dimensionInfos.add(IndicatorDimensionsResponse.DimensionInfo.builder()
                            .type(dim).displayName(dim.substring(0, 1).toUpperCase() + dim.substring(1)).values(new ArrayList<>(customValues)).build());
                    break;
            }
        }
        return IndicatorDimensionsResponse.builder()
                .indicatorId(String.valueOf(indicatorId))
                .availableDimensions(dimensionInfos)
                .build();
    }
} 