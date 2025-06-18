package io.dashboard.service;

import io.dashboard.dto.VisualizationConfigRequest;
import io.dashboard.dto.VisualizationConfigResponse;
import io.dashboard.entity.VisualizationConfig;
import io.dashboard.entity.VisualizationType;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.repository.VisualizationConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VisualizationConfigService {

    @Autowired
    private VisualizationConfigRepository visualizationConfigRepository;

    public List<VisualizationConfigResponse> findByIndicatorId(Long indicatorId) {
        if (indicatorId == null || indicatorId <= 0) {
            throw new BadRequestException("Invalid indicator ID");
        }

        List<VisualizationConfig> configs = visualizationConfigRepository.findByIndicatorId(indicatorId);
        return configs.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public VisualizationConfigResponse getDefaultConfig(Long indicatorId) {
        if (indicatorId == null || indicatorId <= 0) {
            throw new BadRequestException("Invalid indicator ID");
        }

        List<VisualizationConfig> defaultConfigs = visualizationConfigRepository.findByIndicatorIdAndIsDefault(indicatorId, true);
        
        if (defaultConfigs.isEmpty()) {
            return null;
        }
        
        return convertToResponse(defaultConfigs.get(0));
    }

    public VisualizationConfigResponse createConfig(VisualizationConfigRequest request) {
        validateConfigRequest(request);
        
        VisualizationConfig config = new VisualizationConfig();
        config.setIndicatorId(request.getIndicatorId());
        config.setVisualizationType(request.getVisualizationType());
        config.setConfig(request.getConfig());
        config.setDefault(request.isDefault());
        config.setCreatedAt(LocalDateTime.now());

        // If this is set as default, unset other defaults for this indicator
        if (request.isDefault()) {
            setAsDefaultForIndicator(request.getIndicatorId());
        }

        VisualizationConfig savedConfig = visualizationConfigRepository.save(config);
        return convertToResponse(savedConfig);
    }

    public VisualizationConfigResponse updateConfig(Long id, VisualizationConfigRequest request) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid config ID");
        }

        VisualizationConfig existingConfig = visualizationConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visualization config not found with ID: " + id));

        validateConfigRequest(request);

        existingConfig.setIndicatorId(request.getIndicatorId());
        existingConfig.setVisualizationType(request.getVisualizationType());
        existingConfig.setConfig(request.getConfig());
        
        // Handle default setting
        if (request.isDefault() && !existingConfig.isDefault()) {
            setAsDefaultForIndicator(request.getIndicatorId());
            existingConfig.setDefault(true);
        } else if (!request.isDefault() && existingConfig.isDefault()) {
            existingConfig.setDefault(false);
        }

        VisualizationConfig updatedConfig = visualizationConfigRepository.save(existingConfig);
        return convertToResponse(updatedConfig);
    }

    public void deleteConfig(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid config ID");
        }

        if (!visualizationConfigRepository.existsById(id)) {
            throw new ResourceNotFoundException("Visualization config not found with ID: " + id);
        }

        visualizationConfigRepository.deleteById(id);
    }

    public VisualizationConfigResponse setAsDefault(Long configId) {
        if (configId == null || configId <= 0) {
            throw new BadRequestException("Invalid config ID");
        }

        VisualizationConfig config = visualizationConfigRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("Visualization config not found with ID: " + configId));

        // Unset all other defaults for this indicator
        setAsDefaultForIndicator(config.getIndicatorId());
        
        // Set this config as default
        config.setDefault(true);
        VisualizationConfig savedConfig = visualizationConfigRepository.save(config);
        
        return convertToResponse(savedConfig);
    }

    public VisualizationConfigResponse cloneConfig(Long configId, String newTitle) {
        if (configId == null || configId <= 0) {
            throw new BadRequestException("Invalid config ID");
        }
        if (newTitle == null || newTitle.trim().isEmpty()) {
            throw new BadRequestException("New title is required");
        }

        VisualizationConfig originalConfig = visualizationConfigRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("Visualization config not found with ID: " + configId));

        VisualizationConfig clonedConfig = new VisualizationConfig();
        clonedConfig.setIndicatorId(originalConfig.getIndicatorId());
        clonedConfig.setVisualizationType(originalConfig.getVisualizationType());
        clonedConfig.setConfig(originalConfig.getConfig());
        clonedConfig.setDefault(false); // Cloned configs are not default
        clonedConfig.setCreatedAt(LocalDateTime.now());

        // Update the config with new title if it's JSON
        if (originalConfig.getConfig() != null && originalConfig.getConfig().contains("\"title\"")) {
            String updatedConfig = originalConfig.getConfig().replaceAll(
                    "\"title\"\\s*:\\s*\"[^\"]*\"", 
                    "\"title\":\"" + newTitle + "\""
            );
            clonedConfig.setConfig(updatedConfig);
        }

        VisualizationConfig savedConfig = visualizationConfigRepository.save(clonedConfig);
        return convertToResponse(savedConfig);
    }

    public boolean validateConfig(VisualizationConfig config) {
        if (config == null) {
            return false;
        }

        // Validate required fields
        if (config.getIndicatorId() == null || config.getIndicatorId() <= 0) {
            return false;
        }

        if (config.getVisualizationType() == null) {
            return false;
        }

        // Validate visualization type
        try {
            VisualizationType.valueOf(config.getVisualizationType().name());
        } catch (IllegalArgumentException e) {
            return false;
        }

        // Validate config JSON if present
        if (config.getConfig() != null && !config.getConfig().trim().isEmpty()) {
            try {
                // Basic JSON validation - check if it starts with { and ends with }
                String trimmedConfig = config.getConfig().trim();
                if (!trimmedConfig.startsWith("{") || !trimmedConfig.endsWith("}")) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }

        return true;
    }

    private void validateConfigRequest(VisualizationConfigRequest request) {
        if (request == null) {
            throw new BadRequestException("Request cannot be null");
        }

        if (request.getIndicatorId() == null || request.getIndicatorId() <= 0) {
            throw new BadRequestException("Valid indicator ID is required");
        }

        if (request.getVisualizationType() == null) {
            throw new BadRequestException("Visualization type is required");
        }

        // Validate visualization type
        try {
            VisualizationType.valueOf(request.getVisualizationType().name());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid visualization type: " + request.getVisualizationType());
        }

        // Validate config JSON if present
        if (request.getConfig() != null && !request.getConfig().trim().isEmpty()) {
            try {
                // Basic JSON validation
                String trimmedConfig = request.getConfig().trim();
                if (!trimmedConfig.startsWith("{") || !trimmedConfig.endsWith("}")) {
                    throw new BadRequestException("Invalid JSON format in config");
                }
            } catch (Exception e) {
                throw new BadRequestException("Invalid JSON format in config");
            }
        }
    }

    private void setAsDefaultForIndicator(Long indicatorId) {
        List<VisualizationConfig> existingDefaults = visualizationConfigRepository.findByIndicatorIdAndIsDefault(indicatorId, true);
        for (VisualizationConfig config : existingDefaults) {
            config.setDefault(false);
            visualizationConfigRepository.save(config);
        }
    }

    private VisualizationConfigResponse convertToResponse(VisualizationConfig config) {
        VisualizationConfigResponse response = new VisualizationConfigResponse();
        response.setId(config.getId());
        response.setIndicatorId(config.getIndicatorId());
        response.setVisualizationType(config.getVisualizationType());
        response.setConfig(config.getConfig());
        response.setDefault(config.isDefault());
        response.setCreatedAt(config.getCreatedAt());
        return response;
    }
} 