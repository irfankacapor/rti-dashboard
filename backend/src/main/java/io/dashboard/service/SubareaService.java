package io.dashboard.service;

import io.dashboard.dto.SubareaCreateRequest;
import io.dashboard.dto.SubareaResponse;
import io.dashboard.dto.SubareaUpdateRequest;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.Area;
import io.dashboard.model.FactIndicatorValue;
import io.dashboard.model.Subarea;
import io.dashboard.model.SubareaIndicator;
import io.dashboard.repository.AreaRepository;
import io.dashboard.repository.FactIndicatorValueRepository;
import io.dashboard.repository.SubareaIndicatorRepository;
import io.dashboard.repository.SubareaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubareaService {
    private final SubareaRepository subareaRepository;
    private final AreaRepository areaRepository;
    private final FactIndicatorValueRepository factIndicatorValueRepository;
    private final SubareaIndicatorRepository subareaIndicatorRepository;
    private final AggregationService aggregationService;

    public List<SubareaResponse> findAll() {
        try {
            return subareaRepository.findAllWithAreaAndIndicators().stream().map(this::toResponse).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch subareas: " + e.getMessage(), e);
        }
    }

    public List<SubareaResponse> findByAreaId(Long areaId) {
        return subareaRepository.findByAreaId(areaId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public SubareaResponse findById(Long id) {
        Subarea subarea = subareaRepository.findByIdWithArea(id)
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
        if (subarea.getSubareaIndicators() != null && !subarea.getSubareaIndicators().isEmpty()) {
            throw new BadRequestException("Cannot delete subarea with indicators");
        }
        subareaRepository.delete(subarea);
    }

    @Transactional
    public void deleteWithData(Long id) {
        Subarea subarea = subareaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subarea", "id", id));
        
        // Get all indicators associated with this subarea
        List<SubareaIndicator> subareaIndicators = subareaIndicatorRepository.findBySubareaId(id);
        
        // For each indicator, delete its associated fact values
        for (SubareaIndicator subareaIndicator : subareaIndicators) {
            Long indicatorId = subareaIndicator.getId().getIndicatorId();
            List<FactIndicatorValue> factValues = factIndicatorValueRepository.findByIndicatorId(indicatorId);
            factIndicatorValueRepository.deleteAll(factValues);
        }
        
        // Remove all SubareaIndicator relationships
        subareaIndicatorRepository.deleteAll(subareaIndicators);
        
        // Finally delete the subarea
        subareaRepository.delete(subarea);
        
        log.info("Deleted subarea {} with {} associated indicators and their data", id, subareaIndicators.size());
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

    public double calculateAggregatedValue(Long subareaId) {
        try {
            log.debug("Calculating aggregated value for subarea ID: {}", subareaId);
            
            // First check if subarea exists
            if (!subareaRepository.existsById(subareaId)) {
                log.warn("Subarea with ID {} not found", subareaId);
                throw new ResourceNotFoundException("Subarea", "id", subareaId);
            }
            
            double aggregatedValue = aggregationService.calculateSubareaAggregatedValue(subareaId);
            log.debug("Calculated aggregated value {} for subarea ID: {}", aggregatedValue, subareaId);
            return aggregatedValue;
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
        } catch (Exception e) {
            log.error("Error getting aggregated data by {} for subarea ID {}: {}", dimension, subareaId, e.getMessage(), e);
            throw new RuntimeException("Failed to get aggregated data by " + dimension + " for subarea: " + e.getMessage(), e);
        }
    }
} 