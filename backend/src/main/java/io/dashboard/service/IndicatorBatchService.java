package io.dashboard.service;

import io.dashboard.dto.CsvIndicatorData;
import io.dashboard.dto.IndicatorBatchRequest;
import io.dashboard.dto.IndicatorBatchResponse;
import io.dashboard.dto.IndicatorResponse;
import io.dashboard.dto.IndicatorValue;
import io.dashboard.model.DimGeneric;
import io.dashboard.model.DimLocation;
import io.dashboard.model.DimTime;
import io.dashboard.model.Direction;
import io.dashboard.model.FactIndicatorValue;
import io.dashboard.model.Indicator;
import io.dashboard.model.Subarea;
import io.dashboard.model.SubareaIndicator;
import io.dashboard.repository.DimGenericRepository;
import io.dashboard.repository.DimLocationRepository;
import io.dashboard.repository.DimTimeRepository;
import io.dashboard.repository.FactIndicatorValueRepository;
import io.dashboard.repository.IndicatorRepository;
import io.dashboard.repository.SubareaIndicatorRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class IndicatorBatchService {
    
    private final IndicatorRepository indicatorRepository;
    private final DimTimeRepository dimTimeRepository;
    private final DimLocationRepository dimLocationRepository;
    private final DimGenericRepository dimGenericRepository;
    private final FactIndicatorValueRepository factRepository;
    private final SubareaIndicatorRepository subareaIndicatorRepository;
    
    public IndicatorBatchResponse createFromCsvData(IndicatorBatchRequest request) {
        List<IndicatorResponse> createdIndicators = new ArrayList<>();
        int totalFactRecords = 0;
        List<String> warnings = new ArrayList<>();
        
        for (CsvIndicatorData csvIndicator : request.getIndicators()) {
            try {
                // 1. Create or find indicator
                Indicator indicator = createOrFindIndicator(csvIndicator);
                
                // 2. Create subarea relationship
                createSubareaRelationship(indicator, csvIndicator);
                
                // 3. Process all values and create fact records
                int factCount = processIndicatorValues(indicator, csvIndicator.getValues());
                totalFactRecords += factCount;
                
                // 4. Add to response only if not already added (for duplicates)
                boolean alreadyAdded = createdIndicators.stream()
                    .anyMatch(response -> response.getId().equals(indicator.getId()));
                if (!alreadyAdded) {
                    createdIndicators.add(mapToResponse(indicator, factCount));
                }
                
            } catch (Exception e) {
                log.error("Failed to process indicator: {}", csvIndicator.getName(), e);
                warnings.add("Failed to process indicator '" + csvIndicator.getName() + "': " + e.getMessage());
            }
        }
        
        return IndicatorBatchResponse.builder()
            .createdIndicators(createdIndicators)
            .totalFactRecords(totalFactRecords)
            .warnings(warnings)
            .message("Successfully processed " + createdIndicators.size() + " indicators")
            .build();
    }
    
    private Indicator createOrFindIndicator(CsvIndicatorData csvIndicator) {
        // Check if indicator already exists
        Optional<Indicator> existing = indicatorRepository.findByName(csvIndicator.getName());
        
        if (existing.isPresent()) {
            return existing.get();
        }
        
        // Create new indicator
        Indicator indicator = new Indicator();
        indicator.setCode(generateCodeFromName(csvIndicator.getName()));
        indicator.setName(csvIndicator.getName());
        indicator.setDescription(csvIndicator.getDescription());
        indicator.setIsComposite(false);
            
        return indicatorRepository.save(indicator);
    }
    
    private void createSubareaRelationship(Indicator indicator, CsvIndicatorData csvIndicator) {
        // Check if relationship already exists
        boolean exists = subareaIndicatorRepository.existsBySubareaIdAndIndicatorId(
            csvIndicator.getSubareaId(), indicator.getId());
            
        if (!exists) {
            SubareaIndicator relationship = new SubareaIndicator();
            SubareaIndicator.SubareaIndicatorId id = new SubareaIndicator.SubareaIndicatorId();
            id.setSubareaId(csvIndicator.getSubareaId());
            id.setIndicatorId(indicator.getId());
            relationship.setId(id);
            relationship.setDirection(csvIndicator.getDirection());
            relationship.setAggregationWeight(csvIndicator.getAggregationWeight());
            
            // Set the relationships properly - these are required for @MapsId to work
            relationship.setIndicator(indicator);
            // We need to fetch the subarea to set the relationship
            // For now, we'll create a proxy or fetch it properly
            // Since we're in a transaction, we can create a simple reference
            Subarea subarea = new Subarea();
            subarea.setId(csvIndicator.getSubareaId());
            relationship.setSubarea(subarea);
                
            subareaIndicatorRepository.save(relationship);
        }
    }
    
    private int processIndicatorValues(Indicator indicator, List<IndicatorValue> values) {
        int count = 0;
        
        for (IndicatorValue value : values) {
            try {
                // Create dimension records
                DimTime timeId = value.getTimeValue() != null ? 
                    createOrFindTimeValue(value.getTimeValue(), value.getTimeType()) : null;
                    
                DimLocation locationId = value.getLocationValue() != null ?
                    createOrFindLocationValue(value.getLocationValue(), value.getLocationType()) : null;
                
                DimGeneric genericId = value.getCustomDimensions() != null && !value.getCustomDimensions().isEmpty() ?
                    createOrFindGenericDimension(value.getCustomDimensions()) : null;
                
                // Create fact record
                FactIndicatorValue fact = FactIndicatorValue.builder()
                    .indicator(indicator)
                    .value(value.getValue())
                    .time(timeId)
                    .location(locationId)
                    .generic(genericId)
                    .sourceRowHash(generateHash(value))
                    .build();
                    
                factRepository.save(fact);
                count++;
                
            } catch (Exception e) {
                log.warn("Failed to create fact record for indicator {} and value {}", 
                    indicator.getName(), value.getValue(), e);
            }
        }
        
        return count;
    }
    
    private DimTime createOrFindTimeValue(String timeValue, String timeType) {
        // Create or find time dimension record
        return dimTimeRepository.findByValue(timeValue)
            .orElseGet(() -> {
                DimTime dimTime = DimTime.builder()
                    .value(timeValue)
                    .timeType(io.dashboard.model.DimensionType.TIME)
                    .year(extractYear(timeValue))
                    .build();
                return dimTimeRepository.save(dimTime);
            });
    }
    
    private DimLocation createOrFindLocationValue(String locationValue, String locationType) {
        // Create or find location dimension record
        return dimLocationRepository.findByName(locationValue)
            .orElseGet(() -> {
                DimLocation dimLocation = DimLocation.builder()
                    .name(locationValue)
                    .value(locationValue)
                    .type(parseLocationType(locationType))
                    .build();
                return dimLocationRepository.save(dimLocation);
            });
    }
    
    private DimGeneric createOrFindGenericDimension(Map<String, String> customDimensions) {
        // For simplicity, create one generic dimension per custom dimension
        // In practice, you might want to handle this differently
        String dimensionName = customDimensions.keySet().iterator().next();
        String dimensionValue = customDimensions.values().iterator().next();
        
        Optional<DimGeneric> existing = dimGenericRepository.findByDimensionNameAndValue(dimensionName, dimensionValue);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        DimGeneric dimGeneric = DimGeneric.builder()
            .name(dimensionName)
            .value(dimensionValue)
            .dimensionName(dimensionName)
            .build();
        return dimGenericRepository.save(dimGeneric);
    }
    
    private String generateCodeFromName(String name) {
        // Slugify: lowercase, replace non-alphanumeric with _, trim, collapse _, truncate to 45 chars
        String slug = name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "")
                .replaceAll("_+", "_");
        if (slug.length() > 45) {
            slug = slug.substring(0, 45);
        }
        // Add a short hash for uniqueness
        String hash = Integer.toHexString(name.hashCode());
        String code = slug + "_" + hash;
        if (code.length() > 50) {
            code = code.substring(0, 50);
        }
        return code.toUpperCase();
    }
    
    private String generateHash(IndicatorValue value) {
        return value.getTimeValue() + "_" + value.getLocationValue() + "_" + value.getValue();
    }
    
    private DimLocation.LocationType parseLocationType(String locationType) {
        if (locationType == null) return DimLocation.LocationType.STATE;
        
        try {
            return DimLocation.LocationType.valueOf(locationType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DimLocation.LocationType.STATE;
        }
    }
    
    private Integer extractYear(String timeValue) {
        try {
            return Integer.parseInt(timeValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private IndicatorResponse mapToResponse(Indicator indicator, int factCount) {
        IndicatorResponse response = new IndicatorResponse();
        response.setId(indicator.getId());
        response.setCode(indicator.getCode());
        response.setName(indicator.getName());
        response.setDescription(indicator.getDescription());
        response.setIsComposite(indicator.getIsComposite());
        response.setCreatedAt(indicator.getCreatedAt());
        return response;
    }
} 