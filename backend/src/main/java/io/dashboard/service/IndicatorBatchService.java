package io.dashboard.service;

import io.dashboard.dto.CsvIndicatorData;
import io.dashboard.dto.IndicatorBatchRequest;
import io.dashboard.dto.IndicatorBatchResponse;
import io.dashboard.dto.IndicatorResponse;
import io.dashboard.dto.IndicatorValue;
import io.dashboard.model.DimGeneric;
import io.dashboard.model.DimLocation;
import io.dashboard.model.DimTime;
import io.dashboard.model.FactIndicatorValue;
import io.dashboard.model.Indicator;
import io.dashboard.model.Subarea;
import io.dashboard.model.Unit;
import io.dashboard.repository.DimGenericRepository;
import io.dashboard.repository.UnitRepository;
import io.dashboard.repository.DimLocationRepository;
import io.dashboard.repository.DimTimeRepository;
import io.dashboard.repository.FactIndicatorValueRepository;
import io.dashboard.repository.IndicatorRepository;
import io.dashboard.repository.SubareaRepository;
import io.dashboard.repository.DataTypeRepository;
import io.dashboard.model.DataType;
import io.dashboard.exception.BadRequestException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    private final UnitRepository unitRepository;
    private final SubareaRepository subareaRepository;
    private final DataTypeRepository dataTypeRepository;
    
    public IndicatorBatchResponse createFromCsvData(IndicatorBatchRequest request) {
        List<IndicatorResponse> createdIndicators = new ArrayList<>();
        int totalFactRecords = 0;
        List<String> warnings = new ArrayList<>();
        
        for (CsvIndicatorData csvIndicator : request.getIndicators()) {
            try {
                // 1. Create or find indicator
                Indicator indicator = createOrFindIndicator(csvIndicator);
                
                // 2. Process all values and create fact records
                int factCount = processIndicatorValues(indicator, csvIndicator.getValues(), csvIndicator.getSubareaId());
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
        // Handle unit - find by code or create new
        if (csvIndicator.getUnit() != null && !csvIndicator.getUnit().trim().isEmpty()) {
            Unit unit = unitRepository.findByCode(csvIndicator.getUnit())
                .orElseGet(() -> {
                    Unit newUnit = new Unit();
                    newUnit.setCode(csvIndicator.getUnit());
                    newUnit.setDescription("Auto-generated from CSV import");
                    return unitRepository.save(newUnit);
                });
            indicator.setUnit(unit);
        }
        indicator.setUnitPrefix(csvIndicator.getUnitPrefix());
        indicator.setUnitSuffix(csvIndicator.getUnitSuffix());
            
        // Handle data type
        if (csvIndicator.getDataType() != null && !csvIndicator.getDataType().isBlank()) {
            DataType dataType = dataTypeRepository.findAll().stream()
                .filter(dt -> dt.getName().equalsIgnoreCase(csvIndicator.getDataType()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Unknown data type: " + csvIndicator.getDataType()));
            indicator.setDataType(dataType);
        }
        return indicatorRepository.save(indicator);
    }
    
    private int processIndicatorValues(Indicator indicator, List<IndicatorValue> values, Long subareaId) {
        int count = 0;
        Subarea subarea = null;
        
        // Fetch subarea if subareaId is provided
        if (subareaId != null) {
            subarea = subareaRepository.findById(subareaId)
                .orElseThrow(() -> new RuntimeException("Subarea with ID " + subareaId + " not found"));
        }
        
        for (IndicatorValue value : values) {
            try {
                // Create dimension records
                DimTime timeId = value.getTimeValue() != null ? 
                    createOrFindTimeValue(value.getTimeValue(), value.getTimeType()) : null;
                DimLocation locationId = value.getLocationValue() != null ?
                    createOrFindLocationValue(value.getLocationValue(), value.getLocationType()) : null;
                List<DimGeneric> generics = new ArrayList<>();
                if (value.getCustomDimensions() != null && !value.getCustomDimensions().isEmpty()) {
                    for (Map.Entry<String, String> entry : value.getCustomDimensions().entrySet()) {
                        generics.add(createOrFindGenericDimension(entry.getKey(), entry.getValue()));
                    }
                }
                // Create fact record
                FactIndicatorValue fact = FactIndicatorValue.builder()
                    .indicator(indicator)
                    .value(value.getValue())
                    .time(timeId)
                    .location(locationId)
                    .generics(generics)
                    .subarea(subarea)
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
    
    private DimGeneric createOrFindGenericDimension(String dimensionName, String dimensionValue) {
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
        response.setUnit(indicator.getUnit() != null ? indicator.getUnit().getCode() : null);
        response.setUnitId(indicator.getUnit() != null ? indicator.getUnit().getId() : null);
        response.setUnitPrefix(indicator.getUnitPrefix());
        response.setUnitSuffix(indicator.getUnitSuffix());
        return response;
    }
} 