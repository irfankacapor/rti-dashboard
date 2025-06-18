package io.dashboard.dto;

import java.util.List;

public class DataValidationResponse {
    private Long indicatorId;
    private Long totalRecords;
    private Long validRecords;
    private Long invalidRecords;
    private List<String> validationErrors;
    private Boolean isValid;

    public DataValidationResponse() {}

    public DataValidationResponse(Long indicatorId, Long totalRecords, Long validRecords, Long invalidRecords, List<String> validationErrors, Boolean isValid) {
        this.indicatorId = indicatorId;
        this.totalRecords = totalRecords;
        this.validRecords = validRecords;
        this.invalidRecords = invalidRecords;
        this.validationErrors = validationErrors;
        this.isValid = isValid;
    }

    // Getters and setters
    public Long getIndicatorId() { return indicatorId; }
    public void setIndicatorId(Long indicatorId) { this.indicatorId = indicatorId; }
    
    public Long getTotalRecords() { return totalRecords; }
    public void setTotalRecords(Long totalRecords) { this.totalRecords = totalRecords; }
    
    public Long getValidRecords() { return validRecords; }
    public void setValidRecords(Long validRecords) { this.validRecords = validRecords; }
    
    public Long getInvalidRecords() { return invalidRecords; }
    public void setInvalidRecords(Long invalidRecords) { this.invalidRecords = invalidRecords; }
    
    public List<String> getValidationErrors() { return validationErrors; }
    public void setValidationErrors(List<String> validationErrors) { this.validationErrors = validationErrors; }
    
    public Boolean getIsValid() { return isValid; }
    public void setIsValid(Boolean isValid) { this.isValid = isValid; }
} 