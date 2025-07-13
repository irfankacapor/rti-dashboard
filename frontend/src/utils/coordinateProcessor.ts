import { DimensionMapping, DataTuple } from '@/types/csvProcessing';

// Core processing function to generate data tuples from CSV data and dimension mappings
export const generateDataTuples = (
  mappings: DimensionMapping[],
  csvData: string[][]
): DataTuple[] => {
  const tuples: DataTuple[] = [];
  
  // Find value mapping
  const valueMapping = mappings.find(m => m.dimensionType === 'indicator_values');
  if (!valueMapping) {
    throw new Error('No indicator values mapped');
  }
  
  // Process each value cell
  valueMapping.selection.selectedCells.forEach(valueCell => {
    const tuple: DataTuple = {
      coordinates: {},
      value: valueCell.value,
      sourceRow: valueCell.row,
      sourceCol: valueCell.col
    };
    
    // For each dimension mapping, find the corresponding value for this cell
    mappings.forEach(mapping => {
      if (mapping.dimensionType === 'indicator_values') return;
      
      const dimensionValue = findDimensionValueForCell(
        valueCell.row, 
        valueCell.col, 
        mapping, 
        csvData
      );
      
      if (mapping.dimensionType === 'additional_dimension') {
        tuple.coordinates[mapping.customDimensionName!] = dimensionValue;
      } else {
        tuple.coordinates[mapping.dimensionType] = dimensionValue;
      }
    });
    
    tuples.push(tuple);
  });
  
  return tuples;
};

// Find dimension value for a specific value cell based on mapping
// 
// This function handles both full and partial row/column selections:
// 
// 1. FULL SELECTIONS: When user selects entire rows/columns
//    - Row mapping: selection spans entire row (e.g., row 2, cols 1-8)
//    - Column mapping: selection spans entire column (e.g., col 1, rows 1-5)
// 
// 2. PARTIAL SELECTIONS: When user selects only part of rows/columns
//    - Row mapping: selection spans partial row (e.g., row 2, cols 3-6 only)
//    - Column mapping: selection spans partial column (e.g., col 1, rows 2-4 only)
// 
// For partial selections, values outside the selected range use the closest selected cell:
// - If value column < selection start: use first selected column
// - If value column > selection end: use last selected column
// - If value row < selection start: use first selected row  
// - If value row > selection end: use last selected row
//
// Examples:
// - Gender mapped to row 2, cols 3-6: value in col 2 gets gender from col 3
// - Indicator names mapped to col 1, rows 2-4: value in row 5 gets indicator from row 4
const findDimensionValueForCell = (
  valueRow: number,
  valueCol: number,
  dimensionMapping: DimensionMapping,
  csvData: string[][]
): string => {
  const { selection, dimensionType, mappingDirection } = dimensionMapping;

  // Handle special case: single cell mapping
  if (selection.startRow === selection.endRow && selection.startCol === selection.endCol) {
    return csvData[selection.startRow][selection.startCol];
  }

  // Clear direction-based logic
  if (mappingDirection === 'row') {
    // Dimension is mapped to a row (e.g., gender in row 2, year in row 3)
    // Check if the value cell's column falls within the selected column range
    if (selection.startCol <= valueCol && valueCol <= selection.endCol) {
      // Value cell's column is within the selected range, look in the mapped row
      return csvData[selection.startRow][valueCol];
    } else {
      // Value cell's column is outside the selected range
      // Find the closest column within the selected range
      if (valueCol < selection.startCol) {
        return csvData[selection.startRow][selection.startCol];
      } else {
        return csvData[selection.startRow][selection.endCol];
      }
    }
  } else if (mappingDirection === 'column') {
    // Dimension is mapped to a column (e.g., indicator names in column 0)
    // Check if the value cell's row falls within the selected row range
    if (selection.startRow <= valueRow && valueRow <= selection.endRow) {
      // Value cell's row is within the selected range, look in the mapped column
      return csvData[valueRow][selection.startCol];
    } else {
      // Value cell's row is outside the selected range
      // Find the closest row within the selected range
      if (valueRow < selection.startRow) {
        return csvData[selection.startRow][selection.startCol];
      } else {
        return csvData[selection.endRow][selection.startCol];
      }
    }
  }

  // Fallback for legacy mappings without direction (should not happen with new UI)
  return findLegacyDimensionValue(valueRow, valueCol, dimensionMapping, csvData);
};

// Legacy fallback function for backward compatibility
const findLegacyDimensionValue = (
  valueRow: number,
  valueCol: number,
  dimensionMapping: DimensionMapping,
  csvData: string[][]
): string => {
  const { selection, dimensionType } = dimensionMapping;

  // Special handling for indicator_names: if mapping is a single cell, always return that cell's value
  if (dimensionType === 'indicator_names') {
    const isSingleCell =
      selection.startRow === selection.endRow && selection.startCol === selection.endCol;
    if (isSingleCell) {
      return csvData[selection.startRow][selection.startCol];
    }
    // If it's a row (single row, multiple columns)
    if (selection.startRow === selection.endRow) {
      return csvData[selection.startRow][valueCol];
    }
    // If it's a column (single column, multiple rows)
    if (selection.startCol === selection.endCol) {
      return csvData[valueRow][selection.startCol];
    }
  }

  // Special handling for additional_dimension: always return value from mapped column(s) in the same row
  if (dimensionType === 'additional_dimension') {
    for (let col = selection.startCol; col <= selection.endCol; col++) {
      if (csvData[valueRow] && csvData[valueRow][col] !== undefined && csvData[valueRow][col] !== '') {
        return csvData[valueRow][col];
      }
    }
    return '';
  }
  
  // Check if this is a row-based dimension (like year, state)
  if (selection.startRow === selection.endRow) {
    // This is a header row, find the corresponding column value
    return csvData[selection.startRow][valueCol];
  }
  
  // Check if this is a column-based dimension (like indicator names)
  if (selection.startCol === selection.endCol) {
    // This is a header column, find the corresponding row value
    return csvData[valueRow][selection.startCol];
  }
  
  // Default case: try to find the closest match
  return findClosestDimensionValue(valueRow, valueCol, dimensionMapping, csvData);
};

// Fallback function to find the closest dimension value
const findClosestDimensionValue = (
  valueRow: number,
  valueCol: number,
  dimensionMapping: DimensionMapping,
  csvData: string[][]
): string => {
  const { selection } = dimensionMapping;
  
  // Try to find the dimension value in the same row
  if (selection.startRow <= valueRow && valueRow <= selection.endRow) {
    return csvData[valueRow][selection.startCol];
  }
  
  // Try to find the dimension value in the same column
  if (selection.startCol <= valueCol && valueCol <= selection.endCol) {
    return csvData[selection.startRow][valueCol];
  }
  
  // If no direct match, return the first value from the selection
  return csvData[selection.startRow][selection.startCol];
};

// Extract unique values from a cell selection
export const extractUniqueValues = (
  selection: DimensionMapping['selection'],
  csvData: string[][]
): string[] => {
  const values = new Set<string>();
  
  selection.selectedCells.forEach(cell => {
    const strValue = (cell.value ?? '').toString();
    if (strValue.trim() !== '') {
      values.add(strValue.trim());
    }
  });
  
  return Array.from(values).sort();
};

// Validate dimension mappings
export const validateDimensionMappings = (
  mappings: DimensionMapping[]
): { isValid: boolean; errors: string[] } => {
  const errors: string[] = [];
  // Check if we have at least one indicator_values mapping
  const hasValueMapping = mappings.some(m => m.dimensionType === 'indicator_values');
  if (!hasValueMapping) {
    errors.push('At least one indicator_values mapping is required');
  }
  // Check if we have at least one other dimension
  const hasOtherDimension = mappings.some(m => m.dimensionType !== 'indicator_values');
  if (!hasOtherDimension) {
    errors.push('At least one dimension mapping (other than indicator values) is required');
  }
  // Check for duplicate dimension types (except indicator_values)
  const dimensionTypes = mappings
    .filter(m => m.dimensionType !== 'indicator_values')
    .map(m => m.dimensionType);
  
  // For additional_dimension, check for duplicate customDimensionName instead of dimensionType
  const additionalDimensions = mappings.filter(m => m.dimensionType === 'additional_dimension');
  const otherDimensions = mappings.filter(m => 
    m.dimensionType !== 'indicator_values' && m.dimensionType !== 'additional_dimension'
  );
  
  // Check for duplicate dimension types in non-additional dimensions
  const otherDimensionTypes = otherDimensions.map(m => m.dimensionType);
  const duplicates = otherDimensionTypes.filter((type, index) => 
    otherDimensionTypes.indexOf(type) !== index
  );
  
  // Check for duplicate customDimensionName in additional dimensions
  const additionalDimensionNames = additionalDimensions
    .map(m => m.customDimensionName)
    .filter(name => name && name.trim() !== '');
  const duplicateAdditionalNames = additionalDimensionNames.filter((name, index) => 
    additionalDimensionNames.indexOf(name) !== index
  );
  
  if (duplicates.length > 0) {
    errors.push(`Duplicate dimension types found: ${duplicates.join(', ')}`);
  }
  
  if (duplicateAdditionalNames.length > 0) {
    errors.push(`Duplicate additional dimension names found: ${duplicateAdditionalNames.join(', ')}`);
  }
  
  // Check for additional_dimension without custom name
  additionalDimensions.forEach(mapping => {
    if (!mapping.customDimensionName || mapping.customDimensionName.trim() === '') {
      errors.push('Additional dimensions must have a custom name');
    }
  });
  
  return {
    isValid: errors.length === 0,
    errors
  };
};

// Generate a preview of the data tuples
export const generateTuplePreview = (
  mappings: DimensionMapping[],
  csvData: string[][],
  maxTuples: number = 10
): DataTuple[] => {
  const allTuples = generateDataTuples(mappings, csvData);
  return allTuples.slice(0, maxTuples);
}; 