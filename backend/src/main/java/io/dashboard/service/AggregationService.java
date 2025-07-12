package io.dashboard.service;

import io.dashboard.model.FactIndicatorValue;
import io.dashboard.repository.FactIndicatorValueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import io.dashboard.model.DimTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AggregationService {
    
    private final FactIndicatorValueRepository factIndicatorValueRepository;
    
    /**
     * Calculate aggregated value for a specific indicator
     * If multiple values exist for the same time period, average them
     */
    public double calculateIndicatorAggregatedValue(Long indicatorId) {
        List<FactIndicatorValue> values = factIndicatorValueRepository.findByIndicatorIdWithEagerLoading(indicatorId);
        
        if (values.isEmpty()) {
            return 0.0;
        }
        
        // Group by time period and calculate average for each period
        Map<String, List<FactIndicatorValue>> groupedByTime = values.stream()
            .filter(v -> v.getTime() != null)
            .collect(Collectors.groupingBy(v -> v.getTime().getYear() + "-" + v.getTime().getMonth()));
        
        if (groupedByTime.isEmpty()) {
            // No time dimension, just average all values
            return values.stream()
                .mapToDouble(v -> v.getValue().doubleValue())
                .average()
                .orElse(0.0);
        }
        
        // Calculate average for each time period, then average those averages
        List<Double> periodAverages = groupedByTime.values().stream()
            .map(periodValues -> periodValues.stream()
                .mapToDouble(v -> v.getValue().doubleValue())
                .average()
                .orElse(0.0))
            .collect(Collectors.toList());
        
        return periodAverages.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
    }
    
    /**
     * Calculate aggregated value for a specific indicator in a specific subarea
     * If multiple values exist for the same time period, average them
     */
    public double calculateIndicatorAggregatedValue(Long indicatorId, Long subareaId) {
        List<FactIndicatorValue> values = factIndicatorValueRepository.findByIndicatorIdAndSubareaId(indicatorId, subareaId);
        
        if (values.isEmpty()) {
            return 0.0;
        }
        
        // Group by time period and calculate average for each period
        Map<String, List<FactIndicatorValue>> groupedByTime = values.stream()
            .filter(v -> v.getTime() != null)
            .collect(Collectors.groupingBy(v -> v.getTime().getYear() + "-" + v.getTime().getMonth()));
        
        if (groupedByTime.isEmpty()) {
            // No time dimension, just average all values
            return values.stream()
                .mapToDouble(v -> v.getValue().doubleValue())
                .average()
                .orElse(0.0);
        }
        
        // Calculate average for each time period, then average those averages
        List<Double> periodAverages = groupedByTime.values().stream()
            .map(periodValues -> periodValues.stream()
                .mapToDouble(v -> v.getValue().doubleValue())
                .average()
                .orElse(0.0))
            .collect(Collectors.toList());
        
        return periodAverages.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
    }
    
    /**
     * Calculate aggregated value for a subarea (sum of all indicator aggregated values)
     */
    public double calculateSubareaAggregatedValue(Long subareaId) {
        List<FactIndicatorValue> values = factIndicatorValueRepository.findBySubareaIdWithEagerLoading(subareaId);
        
        if (values.isEmpty()) {
            return 0.0;
        }
        
        // Group by indicator and calculate aggregated value for each
        Map<Long, List<FactIndicatorValue>> groupedByIndicator = values.stream()
            .collect(Collectors.groupingBy(v -> v.getIndicator().getId()));
        
        double totalAggregatedValue = 0.0;
        
        for (Map.Entry<Long, List<FactIndicatorValue>> entry : groupedByIndicator.entrySet()) {
            Long indicatorId = entry.getKey();
            List<FactIndicatorValue> indicatorValues = entry.getValue();
            
            // Calculate aggregated value for this indicator
            double indicatorAggregatedValue = calculateIndicatorAggregatedValue(indicatorId);
            totalAggregatedValue += indicatorAggregatedValue;
        }
        
        return totalAggregatedValue;
    }
    
    /**
     * Get aggregated data by time dimension for a subarea
     */
    public Map<String, Double> getSubareaAggregatedByTime(Long subareaId) {
        List<FactIndicatorValue> values = factIndicatorValueRepository.findBySubareaIdWithEagerLoading(subareaId);
        
        if (values.isEmpty()) {
            return new HashMap<>();
        }
        
        // Group by time period and calculate aggregated value for each period
        Map<String, List<FactIndicatorValue>> groupedByTime = values.stream()
            .filter(v -> v.getTime() != null)
            .collect(Collectors.groupingBy(v -> {
                DimTime time = v.getTime();
                // Use the time value directly, which should be properly formatted
                return time.getValue();
            }));
        
        Map<String, Double> result = new HashMap<>();
        
        for (Map.Entry<String, List<FactIndicatorValue>> entry : groupedByTime.entrySet()) {
            String timePeriod = entry.getKey();
            List<FactIndicatorValue> periodValues = entry.getValue();
            
            // Group by indicator for this time period
            Map<Long, List<FactIndicatorValue>> indicatorGroups = periodValues.stream()
                .collect(Collectors.groupingBy(v -> v.getIndicator().getId()));
            
            double periodAggregatedValue = 0.0;
            
            for (List<FactIndicatorValue> indicatorValues : indicatorGroups.values()) {
                // Average the values for this indicator in this time period
                double indicatorAverage = indicatorValues.stream()
                    .mapToDouble(v -> v.getValue().doubleValue())
                    .average()
                    .orElse(0.0);
                periodAggregatedValue += indicatorAverage;
            }
            
            result.put(timePeriod, periodAggregatedValue);
        }
        
        return result;
    }
    
    /**
     * Get aggregated data by location dimension for a subarea
     */
    public Map<String, Double> getSubareaAggregatedByLocation(Long subareaId) {
        List<FactIndicatorValue> values = factIndicatorValueRepository.findBySubareaIdWithEagerLoading(subareaId);
        
        if (values.isEmpty()) {
            return new HashMap<>();
        }
        
        // Group by location and calculate aggregated value for each location
        Map<String, List<FactIndicatorValue>> groupedByLocation = values.stream()
            .filter(v -> v.getLocation() != null)
            .collect(Collectors.groupingBy(v -> v.getLocation().getName()));
        
        Map<String, Double> result = new HashMap<>();
        
        for (Map.Entry<String, List<FactIndicatorValue>> entry : groupedByLocation.entrySet()) {
            String locationName = entry.getKey();
            List<FactIndicatorValue> locationValues = entry.getValue();
            
            // Group by indicator for this location
            Map<Long, List<FactIndicatorValue>> indicatorGroups = locationValues.stream()
                .collect(Collectors.groupingBy(v -> v.getIndicator().getId()));
            
            double locationAggregatedValue = 0.0;
            
            for (List<FactIndicatorValue> indicatorValues : indicatorGroups.values()) {
                // Average the values for this indicator in this location
                double indicatorAverage = indicatorValues.stream()
                    .mapToDouble(v -> v.getValue().doubleValue())
                    .average()
                    .orElse(0.0);
                locationAggregatedValue += indicatorAverage;
            }
            
            result.put(locationName, locationAggregatedValue);
        }
        
        return result;
    }
    
    public Map<String, Double> getSubareaAggregatedByDimension(Long subareaId, String dimension) {
        List<FactIndicatorValue> values = factIndicatorValueRepository.findBySubareaIdWithEagerLoadingGenerics(subareaId);

        if (values.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, List<FactIndicatorValue>> grouped;
        switch (dimension.toLowerCase()) {
            case "time":
                return getSubareaAggregatedByTime(subareaId);
            case "location":
                return getSubareaAggregatedByLocation(subareaId);
            default:
                // For custom dimensions, group by the value of the DimGeneric whose dimensionName matches the requested dimension
                grouped = values.stream()
                    .flatMap(v -> v.getGenerics().stream()
                        .filter(g -> g.getDimensionName() != null && g.getDimensionName().equalsIgnoreCase(dimension))
                        .map(g -> new AbstractMap.SimpleEntry<>(g.getValue(), v)))
                    .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
                break;
        }

        Map<String, Double> result = new HashMap<>();
        for (Map.Entry<String, List<FactIndicatorValue>> entry : grouped.entrySet()) {
            List<FactIndicatorValue> groupValues = entry.getValue();
            // Average the values for this group
            double aggregated = groupValues.stream()
                .mapToDouble(v -> v.getValue().doubleValue())
                .average()
                .orElse(0.0);
            result.put(entry.getKey(), aggregated);
        }
        return result;
    }

    public Map<String, Double> getIndicatorAggregatedByDimension(Long indicatorId, String dimension) {
        List<FactIndicatorValue> values = factIndicatorValueRepository.findByIndicatorIdWithGenerics(indicatorId);

        if (values.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, List<FactIndicatorValue>> grouped;
        switch (dimension.toLowerCase()) {
            case "time":
                grouped = values.stream()
                    .filter(v -> v.getTime() != null)
                    .collect(Collectors.groupingBy(v -> v.getTime().getValue()));
                break;
            case "location":
                grouped = values.stream()
                    .filter(v -> v.getLocation() != null)
                    .collect(Collectors.groupingBy(v -> v.getLocation().getName()));
                break;
            default:
                grouped = values.stream()
                    .flatMap(v -> v.getGenerics().stream()
                        .filter(g -> g.getDimensionName() != null && g.getDimensionName().equalsIgnoreCase(dimension))
                        .map(g -> new AbstractMap.SimpleEntry<>(g.getValue(), v)))
                    .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
                break;
        }

        Map<String, Double> result = new HashMap<>();
        for (Map.Entry<String, List<FactIndicatorValue>> entry : grouped.entrySet()) {
            double avg = entry.getValue().stream()
                .mapToDouble(v -> v.getValue().doubleValue())
                .average()
                .orElse(0.0);
            result.put(entry.getKey(), avg);
        }
        return result;
    }

    /**
     * Find the latest value by date for a specific indicator in a specific subarea
     * Returns null if no time dimension exists or no values found
     */
    public Double findLatestValueByDate(Long indicatorId, Long subareaId) {
        log.debug("findLatestValueByDate called with indicatorId: {}, subareaId: {}", indicatorId, subareaId);
        
        try {
            List<FactIndicatorValue> values = factIndicatorValueRepository.findByIndicatorIdAndSubareaId(indicatorId, subareaId);
            log.debug("Found {} values for indicator {} in subarea {}", values.size(), indicatorId, subareaId);
            
            if (values.isEmpty()) {
                log.debug("No values found, returning null");
                return null;
            }
            
            // Log some sample values to understand the data structure
            for (int i = 0; i < Math.min(3, values.size()); i++) {
                FactIndicatorValue val = values.get(i);
                log.debug("Sample value {}: id={}, value={}, time={}, timeValue={}, timeYear={}, timeMonth={}", 
                    i, val.getId(), val.getValue(), 
                    val.getTime() != null ? "present" : "null",
                    val.getTime() != null ? val.getTime().getValue() : "null",
                    val.getTime() != null ? val.getTime().getYear() : "null",
                    val.getTime() != null ? val.getTime().getMonth() : "null");
            }
            
            // Filter values that have time dimension
            List<FactIndicatorValue> valuesWithTime = values.stream()
                .filter(v -> v.getTime() != null)
                .collect(Collectors.toList());
            
            log.debug("Found {} values with time dimension", valuesWithTime.size());
            
            if (valuesWithTime.isEmpty()) {
                // No time dimension exists, return null
                log.debug("No time dimension exists, returning null");
                return null;
            }
            
            // Group by date (year-month-day) and find the latest date
            Map<String, List<FactIndicatorValue>> groupedByDate = valuesWithTime.stream()
                .collect(Collectors.groupingBy(v -> {
                    DimTime time = v.getTime();
                    // Use the time value directly if available, otherwise construct from components
                    if (time.getValue() != null && !time.getValue().trim().isEmpty()) {
                        return time.getValue();
                    }
                    
                    // Fallback to constructing from components
                    if (time.getYear() != null && time.getMonth() != null && time.getDay() != null) {
                        return String.format("%04d-%02d-%02d", time.getYear(), time.getMonth(), time.getDay());
                    } else if (time.getYear() != null && time.getMonth() != null) {
                        return String.format("%04d-%02d", time.getYear(), time.getMonth());
                    } else if (time.getYear() != null) {
                        return String.format("%04d", time.getYear());
                    } else {
                        return "unknown"; // fallback for completely invalid time
                    }
                }));
            
            log.debug("Grouped by date: {}", groupedByDate.keySet());
            
            if (groupedByDate.isEmpty()) {
                log.debug("No dates found after grouping, returning null");
                return null;
            }
            
            // Find the latest date
            String latestDate = groupedByDate.keySet().stream()
                .filter(date -> !"unknown".equals(date)) // Exclude unknown dates
                .sorted()
                .max(String::compareTo)
                .orElse(null);
            
            log.debug("Latest date found: {}", latestDate);
            
            if (latestDate == null) {
                log.debug("Latest date is null, returning null");
                return null;
            }
            
            // Get all values for the latest date
            List<FactIndicatorValue> latestDateValues = groupedByDate.get(latestDate);
            
            if (latestDateValues.isEmpty()) {
                log.debug("No values found for latest date, returning null");
                return null;
            }
            
            // If multiple entries for the same date, take the latest by database entry time (createdAt)
            FactIndicatorValue latestValue = latestDateValues.stream()
                .max((v1, v2) -> {
                    if (v1.getCreatedAt() == null && v2.getCreatedAt() == null) return 0;
                    if (v1.getCreatedAt() == null) return -1;
                    if (v2.getCreatedAt() == null) return 1;
                    return v1.getCreatedAt().compareTo(v2.getCreatedAt());
                })
                .orElse(latestDateValues.get(0));
            
            log.debug("Selected latest value: {} with createdAt: {}", latestValue.getValue(), latestValue.getCreatedAt());
            
            return latestValue.getValue().doubleValue();
        } catch (Exception e) {
            log.error("Exception in findLatestValueByDate for indicator {} in subarea {}: {}", indicatorId, subareaId, e.getMessage(), e);
            throw e;
        }
    }
} 