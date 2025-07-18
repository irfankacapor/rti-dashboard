package io.dashboard.service;

import io.dashboard.dto.SubareaCreateRequest;
import io.dashboard.dto.SubareaResponse;
import io.dashboard.dto.SubareaUpdateRequest;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.Area;
import io.dashboard.model.FactIndicatorValue;
import io.dashboard.model.Subarea;
import io.dashboard.repository.AreaRepository;
import io.dashboard.repository.FactIndicatorValueRepository;
import io.dashboard.repository.SubareaRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import io.dashboard.dto.IndicatorValuesResponse;
import io.dashboard.dto.IndicatorValueRow;
import io.dashboard.dto.IndicatorResponse;
import io.dashboard.dto.IndicatorDimensionsResponse;
import io.dashboard.dto.SubareaDataResponse;
import io.dashboard.model.DimGeneric;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.Objects;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubareaService {
    private final SubareaRepository subareaRepository;
    private final AreaRepository areaRepository;
    private final FactIndicatorValueRepository factIndicatorValueRepository;
    private final AggregationService aggregationService;
    private final IndicatorService indicatorService;

    @Transactional(readOnly = true)
    public List<SubareaResponse> findAll() {
        try {
            return subareaRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch subareas: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<SubareaResponse> findByAreaId(Long areaId) {
        return subareaRepository.findByAreaId(areaId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SubareaResponse findById(Long id) {
        Subarea subarea = subareaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subarea", "id", id));
        return toResponse(subarea);
    }

    public boolean existsById(Long id) {
        return subareaRepository.existsById(id);
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
        
        // Check if subarea has associated fact values
        List<FactIndicatorValue> factValues = factIndicatorValueRepository.findBySubareaId(id);
        if (!factValues.isEmpty()) {
            throw new BadRequestException("Cannot delete subarea that has associated data");
        }
        
        subareaRepository.delete(subarea);
    }

    @Transactional
    public void deleteWithData(Long id) {
        Subarea subarea = subareaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subarea", "id", id));
        // Delete all fact values for this subarea
        List<FactIndicatorValue> factValues = factIndicatorValueRepository.findBySubareaId(id);
        factIndicatorValueRepository.deleteAll(factValues);
        // Finally delete the subarea
        subareaRepository.delete(subarea);
        log.info("Deleted subarea {} and all associated fact values", id);
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
        return resp;
    }

    public double calculateAggregatedValue(Long subareaId) {
        try {
            log.debug("Calculating aggregated value for subarea ID: {}", subareaId);
            
            // First check if subarea exists
            if (!subareaRepository.existsById(subareaId)) {
                log.warn("Subarea with ID {} not found", subareaId);
                throw new ResourceNotFoundException("Subarea", "id", subareaId);
            }
            
            // Return 0 for now as requested
            log.debug("Returning 0 for aggregated value as requested");
            return 0.0;
        } catch (ResourceNotFoundException e) {
            // Re-throw ResourceNotFoundException as-is
            throw e;
        } catch (Exception e) {
            log.error("Error calculating aggregated value for subarea ID {}: {}", subareaId, e.getMessage(), e);
            throw new RuntimeException("Failed to calculate aggregated value for subarea: " + e.getMessage(), e);
        }
    }
    
    public Map<String, Double> getAggregatedByTime(Long subareaId) {
        try {
            log.debug("Getting aggregated data by time for subarea ID: {}", subareaId);
            
            if (!subareaRepository.existsById(subareaId)) {
                log.warn("Subarea with ID {} not found", subareaId);
                throw new ResourceNotFoundException("Subarea", "id", subareaId);
            }
            
            return aggregationService.getSubareaAggregatedByTime(subareaId);
        } catch (ResourceNotFoundException e) {
            // Re-throw ResourceNotFoundException as-is
            throw e;
        } catch (Exception e) {
            log.error("Error getting aggregated data by time for subarea ID {}: {}", subareaId, e.getMessage(), e);
            throw new RuntimeException("Failed to get aggregated data by time for subarea: " + e.getMessage(), e);
        }
    }
    
    public Map<String, Double> getAggregatedByLocation(Long subareaId) {
        try {
            log.debug("Getting aggregated data by location for subarea ID: {}", subareaId);
            
            if (!subareaRepository.existsById(subareaId)) {
                log.warn("Subarea with ID {} not found", subareaId);
                throw new ResourceNotFoundException("Subarea", "id", subareaId);
            }
            
            return aggregationService.getSubareaAggregatedByLocation(subareaId);
        } catch (ResourceNotFoundException e) {
            // Re-throw ResourceNotFoundException as-is
            throw e;
        } catch (Exception e) {
            log.error("Error getting aggregated data by location for subarea ID {}: {}", subareaId, e.getMessage(), e);
            throw new RuntimeException("Failed to get aggregated data by location for subarea: " + e.getMessage(), e);
        }
    }

    public Map<String, Double> getAggregatedByDimension(Long subareaId, String dimension) {
        try {
            log.debug("Getting aggregated data by {} for subarea ID: {}", dimension, subareaId);

            if (!subareaRepository.existsById(subareaId)) {
                log.warn("Subarea with ID {} not found", subareaId);
                throw new ResourceNotFoundException("Subarea", "id", subareaId);
            }

            return aggregationService.getSubareaAggregatedByDimension(subareaId, dimension);
        } catch (ResourceNotFoundException e) {
            // Re-throw ResourceNotFoundException as-is
            throw e;
        } catch (Exception e) {
            log.error("Error getting aggregated data by {} for subarea ID {}: {}", dimension, subareaId, e.getMessage(), e);
            throw new RuntimeException("Failed to get aggregated data by " + dimension + " for subarea: " + e.getMessage(), e);
        }
    }

    public List<FactIndicatorValue> getIndicatorValuesForSubarea(Long indicatorId, Long subareaId) {
        try {
            log.debug("Getting values for indicator {} and subarea {}", indicatorId, subareaId);
            List<FactIndicatorValue> values = factIndicatorValueRepository.findByIndicatorIdAndSubareaId(indicatorId, subareaId);
            log.debug("Found {} fact values for indicator {} and subarea {}", values.size(), indicatorId, subareaId);
            return values;
        } catch (Exception e) {
            log.error("Error getting values for indicator {} and subarea {}: {}", indicatorId, subareaId, e.getMessage(), e);
            throw new RuntimeException("Failed to get values for indicator and subarea: " + e.getMessage(), e);
        }
    }

    public double getIndicatorAggregatedValueForSubarea(Long indicatorId, Long subareaId) {
        try {
            log.debug("Getting aggregated value for indicator {} and subarea {}", indicatorId, subareaId);
            List<FactIndicatorValue> values = factIndicatorValueRepository.findByIndicatorIdAndSubareaId(indicatorId, subareaId);
            log.debug("Found {} fact values for aggregation", values.size());
            if (values.isEmpty()) return 0.0;
            double result = values.stream().mapToDouble(v -> v.getValue().doubleValue()).average().orElse(0.0);
            log.debug("Calculated aggregated value: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Error getting aggregated value for indicator {} and subarea {}: {}", indicatorId, subareaId, e.getMessage(), e);
            throw new RuntimeException("Failed to get aggregated value for indicator and subarea: " + e.getMessage(), e);
        }
    }

    public List<String> getIndicatorDimensionsForSubarea(Long indicatorId, Long subareaId) {
        try {
            log.debug("Getting dimensions for indicator {} and subarea {}", indicatorId, subareaId);
            List<FactIndicatorValue> values = factIndicatorValueRepository.findByIndicatorIdAndSubareaId(indicatorId, subareaId);
            log.debug("Found {} fact values for indicator {} and subarea {}", values.size(), indicatorId, subareaId);
            
            // Collect all dimension names present in the values
            java.util.Set<String> dimensions = new java.util.HashSet<>();
            if (values.stream().anyMatch(f -> f.getTime() != null)) dimensions.add("time");
            if (values.stream().anyMatch(f -> f.getLocation() != null)) dimensions.add("location");
            for (FactIndicatorValue fact : values) {
                if (fact.getGenerics() != null) {
                    for (var g : fact.getGenerics()) {
                        if (g.getDimensionName() != null) dimensions.add(g.getDimensionName());
                    }
                }
            }
            log.debug("Found dimensions: {}", dimensions);
            return new java.util.ArrayList<>(dimensions);
        } catch (Exception e) {
            log.error("Error getting dimensions for indicator {} and subarea {}: {}", indicatorId, subareaId, e.getMessage(), e);
            throw new RuntimeException("Failed to get dimensions for indicator and subarea: " + e.getMessage(), e);
        }
    }

    public IndicatorValuesResponse getIndicatorValuesResponseForSubarea(Long indicatorId, Long subareaId) {
        List<FactIndicatorValue> facts = getIndicatorValuesForSubarea(indicatorId, subareaId);
        List<String> dimensionColumns = getIndicatorDimensionsForSubarea(indicatorId, subareaId);
        List<IndicatorValueRow> rows = facts.stream().map(this::toIndicatorValueRow).collect(java.util.stream.Collectors.toList());
        String indicatorName = facts.isEmpty() ? null : facts.get(0).getIndicator().getName();
        String dataType = facts.isEmpty() || facts.get(0).getIndicator().getDataType() == null ? null : facts.get(0).getIndicator().getDataType().getName();
        return IndicatorValuesResponse.builder()
            .rows(rows)
            .dimensionColumns(dimensionColumns)
            .indicatorName(indicatorName)
            .dataType(dataType)
            .build();
    }

    private IndicatorValueRow toIndicatorValueRow(FactIndicatorValue fact) {
        java.util.HashMap<String, String> dims = new java.util.HashMap<>();
        if (fact.getTime() != null) dims.put("time", fact.getTime().getValue());
        if (fact.getLocation() != null) dims.put("location", fact.getLocation().getName());
        if (fact.getGenerics() != null) {
            for (var g : fact.getGenerics()) {
                if (g.getDimensionName() != null) dims.put(g.getDimensionName(), g.getValue());
            }
        }
        return IndicatorValueRow.builder()
            .factId(fact.getId())
            .dimensions(dims)
            .value(fact.getValue())
            .isEmpty(fact.getValue() == null)
            .build();
    }

    /**
     * Get comprehensive subarea data including all indicators, aggregated data, and dimension metadata
     * Returns partial data if some parts fail
     */
    @Transactional(readOnly = true)
    public SubareaDataResponse getSubareaData(Long subareaId) {
        SubareaDataResponse.SubareaDataResponseBuilder builder = SubareaDataResponse.builder();
        Map<String, String> errors = new HashMap<>();
        
        // First check if subarea exists - this will throw ResourceNotFoundException if not found
        SubareaResponse subarea = findById(subareaId);
        builder.subarea(subarea);
        
        try {
            // Get indicators list
            List<IndicatorResponse> indicators = indicatorService.findByFactSubareaId(subareaId);
            builder.indicators(indicators);
        } catch (Exception e) {
            log.error("Error fetching indicators for subarea {}: {}", subareaId, e.getMessage());
            errors.put("indicators", e.getMessage());
        }
        
        try {
            // Get aggregated data for all available dimensions
            Map<String, Map<String, Double>> aggregatedData = new HashMap<>();
            List<String> availableDimensions = getAvailableDimensionsForSubarea(subareaId);
            
            for (String dimension : availableDimensions) {
                try {
                    Map<String, Double> dimensionData = aggregationService.getSubareaAggregatedByDimension(subareaId, dimension);
                    aggregatedData.put(dimension, dimensionData);
                } catch (Exception e) {
                    log.error("Error fetching aggregated data for dimension {} in subarea {}: {}", dimension, subareaId, e.getMessage());
                    errors.put("aggregatedData." + dimension, e.getMessage());
                }
            }
            builder.aggregatedData(aggregatedData);
        } catch (Exception e) {
            log.error("Error fetching aggregated data for subarea {}: {}", subareaId, e.getMessage());
            errors.put("aggregatedData", e.getMessage());
        }
        
        try {
            // Get total aggregated value - set to 0 for now as requested
            builder.totalAggregatedValue(0.0);
        } catch (Exception e) {
            log.error("Error calculating total aggregated value for subarea {}: {}", subareaId, e.getMessage());
            errors.put("totalAggregatedValue", e.getMessage());
        }
        
        try {
            // Get dimension metadata for all indicators
            Map<String, IndicatorDimensionsResponse> dimensionMetadata = new HashMap<>();
            List<IndicatorResponse> indicators = indicatorService.findByFactSubareaId(subareaId);
            
            for (IndicatorResponse indicator : indicators) {
                try {
                    IndicatorDimensionsResponse metadata = indicatorService.getIndicatorDimensions(indicator.getId());
                    dimensionMetadata.put(indicator.getId().toString(), metadata);
                } catch (Exception e) {
                    log.error("Error fetching dimension metadata for indicator {} in subarea {}: {}", indicator.getId(), subareaId, e.getMessage());
                    errors.put("dimensionMetadata." + indicator.getId(), e.getMessage());
                }
            }
            builder.dimensionMetadata(dimensionMetadata);
        } catch (Exception e) {
            log.error("Error fetching dimension metadata for subarea {}: {}", subareaId, e.getMessage());
            errors.put("dimensionMetadata", e.getMessage());
        }
        
        try {
            // Get time series data for all indicators
            List<Map<String, Object>> timeSeriesData = getSubareaTimeSeriesData(subareaId, true);
            builder.timeSeriesData(timeSeriesData);
        } catch (Exception e) {
            log.error("Error fetching time series data for subarea {}: {}", subareaId, e.getMessage());
            errors.put("timeSeriesData", e.getMessage());
        }
        
        try {
            // Get individual indicator time series data
            Map<String, List<Map<String, Object>>> indicatorTimeSeriesData = getIndividualIndicatorTimeSeriesData(subareaId, true);
            builder.indicatorTimeSeriesData(indicatorTimeSeriesData);
        } catch (Exception e) {
            log.error("Error fetching individual indicator time series data for subarea {}: {}", subareaId, e.getMessage());
            errors.put("indicatorTimeSeriesData", e.getMessage());
        }
        
        try {
            // Get individual indicator dimension data for all dimensions
            Map<String, Map<String, List<Map<String, Object>>>> indicatorDimensionData = getIndividualIndicatorDimensionData(subareaId, true);
            builder.indicatorDimensionData(indicatorDimensionData);
        } catch (Exception e) {
            log.error("Error fetching individual indicator dimension data for subarea {}: {}", subareaId, e.getMessage());
            errors.put("indicatorDimensionData", e.getMessage());
        }
        
        builder.errors(errors);
        return builder.build();
    }
    
    /**
     * Get time series data for all indicators in a subarea
     * Returns data in format: [{ year: string, indicators: { [indicatorName]: value } }]
     */
    private List<Map<String, Object>> getSubareaTimeSeriesData(Long subareaId, boolean includeTime) {
        List<IndicatorResponse> indicators = indicatorService.findByFactSubareaId(subareaId);
        Map<String, Map<String, Double>> indicatorTimeData = new HashMap<>();
        Set<String> allYears = new HashSet<>();
        
        // Collect time data for each indicator
        for (IndicatorResponse indicator : indicators) {
            if (includeTime && indicator.getDimensions() != null && indicator.getDimensions().contains("time")) {
                List<FactIndicatorValue> values = factIndicatorValueRepository.findByIndicatorIdAndSubareaId(indicator.getId(), subareaId);
                Map<String, Double> timeData = new HashMap<>();
                
                for (FactIndicatorValue value : values) {
                    if (value.getTime() != null && value.getTime().getValue() != null) {
                        String year = value.getTime().getValue();
                        allYears.add(year);
                        timeData.put(year, value.getValue().doubleValue());
                    }
                }
                
                if (!timeData.isEmpty()) {
                    indicatorTimeData.put(indicator.getName(), timeData);
                }
            }
        }
        
        // If no indicators have time data, return empty list
        if (indicatorTimeData.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Create the combined data structure
        List<Map<String, Object>> result = new ArrayList<>();
        List<String> sortedYears = allYears.stream().sorted().collect(Collectors.toList());
        
        for (String year : sortedYears) {
            Map<String, Object> yearData = new HashMap<>();
            yearData.put("year", year);
            
            Map<String, Double> indicatorsData = new HashMap<>();
            for (Map.Entry<String, Map<String, Double>> entry : indicatorTimeData.entrySet()) {
                String indicatorName = entry.getKey();
                Map<String, Double> timeData = entry.getValue();
                if (timeData.containsKey(year)) {
                    indicatorsData.put(indicatorName, timeData.get(year));
                }
            }
            
            yearData.put("indicators", indicatorsData);
            result.add(yearData);
        }
        
        return result;
    }
    
    /**
     * Get individual time series data for each indicator in a subarea
     * Returns data in format: { [indicatorId]: [{ year: string, value: number }] }
     */
    private Map<String, List<Map<String, Object>>> getIndividualIndicatorTimeSeriesData(Long subareaId, boolean includeTime) {
        List<IndicatorResponse> indicators = indicatorService.findByFactSubareaId(subareaId);
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        
        for (IndicatorResponse indicator : indicators) {
            if (includeTime && indicator.getDimensions() != null && indicator.getDimensions().contains("time")) {
                List<FactIndicatorValue> values = factIndicatorValueRepository.findByIndicatorIdAndSubareaId(indicator.getId(), subareaId);
                List<Map<String, Object>> timeSeries = new ArrayList<>();
                
                for (FactIndicatorValue value : values) {
                    if (value.getTime() != null && value.getTime().getValue() != null) {
                        Map<String, Object> dataPoint = new HashMap<>();
                        dataPoint.put("year", value.getTime().getValue());
                        dataPoint.put("value", value.getValue().doubleValue());
                        timeSeries.add(dataPoint);
                    }
                }
                
                // Sort by year
                timeSeries.sort((a, b) -> {
                    String yearA = (String) a.get("year");
                    String yearB = (String) b.get("year");
                    return yearA.compareTo(yearB);
                });
                
                result.put(indicator.getId().toString(), timeSeries);
            }
        }
        
        return result;
    }

    /**
     * Get individual dimension data for each indicator in a subarea
     * Returns data in format: { [indicatorId]: { [dimension]: [{ dimensionValue: string, allDimensions: {...}, value: number }] } }
     */
    private Map<String, Map<String, List<Map<String, Object>>>> getIndividualIndicatorDimensionData(Long subareaId, boolean includeTime) {
        List<IndicatorResponse> indicators = indicatorService.findByFactSubareaId(subareaId);
        Map<String, Map<String, List<Map<String, Object>>>> result = new HashMap<>();
        
        for (IndicatorResponse indicator : indicators) {
            if (indicator.getDimensions() != null && !indicator.getDimensions().isEmpty()) {
                List<FactIndicatorValue> values = factIndicatorValueRepository.findByIndicatorIdAndSubareaId(indicator.getId(), subareaId);
                Map<String, List<Map<String, Object>>> dimensionData = new HashMap<>();
                
                // Process each dimension
                for (String dimension : indicator.getDimensions()) {
                    List<Map<String, Object>> dimensionSeries = new ArrayList<>();
                    
                    for (FactIndicatorValue value : values) {
                        String dimensionValue = getDimensionValue(value, dimension);
                        if (dimensionValue != null) {
                            Map<String, Object> dataPoint = new HashMap<>();
                            dataPoint.put("dimensionValue", dimensionValue);
                            dataPoint.put("value", value.getValue().doubleValue());
                            
                            // Add all dimension values for this data point
                            Map<String, String> allDimensions = new HashMap<>();
                            for (String dim : indicator.getDimensions()) {
                                String dimValue = getDimensionValue(value, dim);
                                if (dimValue != null) {
                                    allDimensions.put(dim, dimValue);
                                }
                            }
                            dataPoint.put("allDimensions", allDimensions);
                            
                            dimensionSeries.add(dataPoint);
                        }
                    }
                    
                    // Sort dimension values
                    dimensionSeries.sort((a, b) -> {
                        String valueA = (String) a.get("dimensionValue");
                        String valueB = (String) b.get("dimensionValue");
                        // Try numeric sorting first, then string
                        try {
                            Double numA = Double.parseDouble(valueA);
                            Double numB = Double.parseDouble(valueB);
                            return numA.compareTo(numB);
                        } catch (NumberFormatException e) {
                            return valueA.compareTo(valueB);
                        }
                    });
                    
                    if (!dimensionSeries.isEmpty()) {
                        dimensionData.put(dimension, dimensionSeries);
                    }
                }
                
                if (!dimensionData.isEmpty()) {
                    result.put(indicator.getId().toString(), dimensionData);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Extract dimension value from a fact indicator value
     */
    private String getDimensionValue(FactIndicatorValue value, String dimension) {
        switch (dimension.toLowerCase()) {
            case "time":
                return value.getTime() != null ? value.getTime().getValue() : null;
            case "location":
                return value.getLocation() != null ? value.getLocation().getName() : null;
            default:
                // For custom dimensions, look in generics
                if (value.getGenerics() != null) {
                    return value.getGenerics().stream()
                        .filter(g -> dimension.equals(g.getDimensionName()))
                        .map(DimGeneric::getValue)
                        .findFirst()
                        .orElse(null);
                }
                return null;
        }
    }
    
    /**
     * Get available dimensions for a subarea
     */
    private List<String> getAvailableDimensionsForSubarea(Long subareaId) {
        List<String> dimensions = new ArrayList<>();
        
        // Always include time and location if they exist
        try {
            List<FactIndicatorValue> values = factIndicatorValueRepository.findBySubareaIdWithEagerLoading(subareaId);
            if (values.stream().anyMatch(v -> v.getTime() != null)) {
                dimensions.add("time");
            }
            if (values.stream().anyMatch(v -> v.getLocation() != null)) {
                dimensions.add("location");
            }
            
            // Add custom dimensions
            Set<String> customDimensions = values.stream()
                .flatMap(v -> v.getGenerics().stream())
                .map(DimGeneric::getDimensionName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
            dimensions.addAll(customDimensions);
        } catch (Exception e) {
            log.error("Error determining available dimensions for subarea {}: {}", subareaId, e.getMessage());
        }
        
        return dimensions;
    }
} 