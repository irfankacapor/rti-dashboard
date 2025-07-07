export interface ValidationResult {
  isValid: boolean;
  error?: string;
  warning?: string;
}

export const validateValueChange = (
  oldValue: number | undefined,
  newValue: string,
  currentDataType: string,
  dimensionType?: string
): ValidationResult => {
  if (newValue.trim() === '') {
    return { isValid: false, error: 'Value is required' };
  }
  const num = Number(newValue);
  if (isNaN(num)) {
    return { isValid: false, error: 'Must be a number' };
  }
  if (currentDataType === 'integer' && !Number.isInteger(num)) {
    return { isValid: false, error: 'Must be an integer' };
  }
  if (dimensionType === 'time' && (!Number.isInteger(num) || num < 1900 || num > 2100)) {
    return { isValid: false, error: 'Year must be a valid integer (1900-2100)' };
  }
  if (oldValue !== undefined && typeof oldValue === 'number') {
    if (Number.isInteger(oldValue) && !Number.isInteger(num)) {
      return { isValid: true, warning: 'Changing from integer to decimal' };
    }
  }
  return { isValid: true };
}; 