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
const findDimensionValueForCell = (
  valueRow: number,
  valueCol: number,
  dimensionMapping: DimensionMapping,
  csvData: string[][]
): string => {
  const { selection, dimensionType } = dimensionMapping;
  
  // Special handling for additional_dimension: always return value from mapped column(s) in the same row
  if (dimensionType === 'additional_dimension') {
    for (let col = selection.startCol; col <= selection.endCol; col++) {
      if (csvData[valueRow] && csvData[valueRow][col] !== undefined && csvData[valueRow][col] !== '') {
        return csvData[valueRow][col];
      }
    }
    return '';
  }
  
  // If dimension is in same row (e.g., year, state for each row)
  if (selection.startRow <= valueRow && valueRow <= selection.endRow) {
    // Find the column that intersects with the dimension mapping
    for (let col = selection.startCol; col <= selection.endCol; col++) {
      if (col === valueCol) {
        return csvData[valueRow][col];
      }
    }
  }
  
  // If dimension is in same column (e.g., indicator names in header row)
  if (selection.startCol <= valueCol && valueCol <= selection.endCol) {
    // Find the row that intersects with the dimension mapping
    for (let row = selection.startRow; row <= selection.endRow; row++) {
      if (row === valueRow) {
        return csvData[row][valueCol];
      }
    }
  }
  
  // Handle cross-referencing: find the dimension value based on row/column position
  // For example, if we have year in column 0 and state in column 1, 
  // and values start from column 2, we need to find the corresponding year and state
  
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
  
  // For additional dimensions or other mappings that span multiple rows/columns,
  // find the value in the same row as the value cell
  if (selection.startRow <= valueRow && valueRow <= selection.endRow) {
    // Find the first column in the dimension mapping for this row
    for (let col = selection.startCol; col <= selection.endCol; col++) {
      if (csvData[valueRow] && csvData[valueRow][col] !== undefined) {
        return csvData[valueRow][col];
      }
    }
  }
  
  // For more complex mappings, we need to determine the relationship
  // This handles cases where dimensions are in different positions
  
  // If the dimension mapping spans multiple rows and columns,
  // we need to find the intersection point
  if (selection.startRow <= valueRow && valueRow <= selection.endRow &&
      selection.startCol <= valueCol && valueCol <= selection.endCol) {
    return csvData[valueRow][valueCol];
  }
  
  // Default case: try to find the closest match
  // This is a fallback for complex mappings
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
    if (cell.value && cell.value.trim() !== '') {
      values.add(cell.value.trim());
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
  const duplicates = dimensionTypes.filter((type, index) => 
    dimensionTypes.indexOf(type) !== index
  );
  if (duplicates.length > 0) {
    errors.push(`Duplicate dimension types found: ${duplicates.join(', ')}`);
  }
  // Check for additional_dimension without custom name
  const additionalDimensions = mappings.filter(m => m.dimensionType === 'additional_dimension');
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