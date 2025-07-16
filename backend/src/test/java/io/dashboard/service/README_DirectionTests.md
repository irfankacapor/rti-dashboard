# Direction Functionality Tests

This directory contains comprehensive tests for the direction functionality in the RTI Dashboard backend.

## Test Files

### 1. `IndicatorBatchServiceTest.java`
Tests the CSV processing functionality with direction assignment.

**Test Cases:**
- `testCreateFromCsvData_WithDirection`: Tests that direction is properly set during CSV processing
- `testCreateFromCsvData_WithDifferentDirectionsForSameIndicator`: Tests that the same indicator can have different directions in different subareas
- `testCreateFromCsvData_WithNullDirection`: Tests handling of null directions

### 2. `IndicatorServiceDirectionTest.java`
Tests the direction retrieval logic in the IndicatorService.

**Test Cases:**
- `testFindByFactSubareaId_WithInputDirection`: Tests retrieval of input direction
- `testFindByFactSubareaId_WithOutputDirection`: Tests retrieval of output direction
- `testFindByFactSubareaId_WithMixedDirections_ShouldReturnMostCommon`: Tests that the most common direction is returned when mixed
- `testFindByFactSubareaId_WithNullDirections`: Tests handling of null directions
- `testFindByFactSubareaId_WithMixedNullAndValidDirections`: Tests filtering of null directions
- `testFindAll_WithMixedDirectionsAcrossSubareas`: Tests direction aggregation across multiple subareas

### 3. `IndicatorDirectionIntegrationTest.java`
Integration tests that verify the complete flow from CSV processing to direction retrieval.

**Test Cases:**
- `testCompleteFlow_CSVProcessingToDirectionRetrieval`: Tests the complete flow with different directions in different subareas
- `testDirectionRetrieval_WithMixedDirectionsInSameSubarea`: Tests handling of mixed directions within the same subarea
- `testDirectionRetrieval_WithNullDirections`: Tests handling of null directions

### 4. `SubareaDataDirectionTest.java`
Tests the subarea data endpoint to ensure it returns correct directions.

**Test Cases:**
- `testSubareaDataEndpoint_ReturnsCorrectDirections`: Tests that subarea data endpoint returns correct directions for each subarea
- `testSubareaDataEndpoint_WithNullDirections`: Tests handling of null directions in subarea data

## Running the Tests

### Run All Direction Tests
```bash
mvn test -Dtest="*Direction*"
```

### Run Specific Test Classes
```bash
# Unit tests
mvn test -Dtest=IndicatorBatchServiceTest
mvn test -Dtest=IndicatorServiceDirectionTest

# Integration tests
mvn test -Dtest=IndicatorDirectionIntegrationTest
mvn test -Dtest=SubareaDataDirectionTest
```

### Run Specific Test Methods
```bash
mvn test -Dtest=IndicatorBatchServiceTest#testCreateFromCsvData_WithDirection
mvn test -Dtest=IndicatorServiceDirectionTest#testFindByFactSubareaId_WithInputDirection
```

## Test Scenarios Covered

### 1. CSV Processing with Direction
- ✅ Direction is properly set on FactIndicatorValue records during CSV processing
- ✅ Same indicator can have different directions in different subareas
- ✅ Null directions are handled gracefully

### 2. Direction Retrieval Logic
- ✅ Subarea-specific queries return the correct direction for that subarea
- ✅ Mixed directions within a subarea return the most common direction
- ✅ Null directions are filtered out and don't affect the result
- ✅ Global queries aggregate directions across all subareas

### 3. API Endpoints
- ✅ `/subareas/{id}/data` endpoint returns correct directions for each subarea
- ✅ Direction information is properly included in IndicatorResponse objects

### 4. Edge Cases
- ✅ Null directions are handled throughout the system
- ✅ Mixed directions (some null, some valid) are handled correctly
- ✅ Empty fact records don't cause errors

## Expected Behavior

### Frontend Display
- **Input indicators**: Show filled circle (●)
- **Output indicators**: Show outlined circle (○)
- **No direction**: Show no circle

### Backend Logic
- **Subarea-specific queries**: Return direction for that specific subarea
- **Global queries**: Return most common direction across all subareas
- **Mixed directions**: Return the direction that appears most frequently
- **Null directions**: Are filtered out and don't affect the result

## Database Schema

The direction is stored in the `fact_indicator_values` table:
```sql
ALTER TABLE fact_indicator_values ADD COLUMN direction VARCHAR(10);
```

This allows the same indicator to have different directions in different subareas, which is the core requirement for the RTI Dashboard. 