'use client';
import React, { useState, useEffect } from 'react';
import {
  Popover,
  Box,
  Typography,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Button,
  Chip,
  Alert,
  Divider
} from '@mui/material';
import { CellSelection, DimensionMapping } from '@/types/csvProcessing';
import { extractUniqueValues } from '@/utils/coordinateProcessor';

interface DimensionMappingPopupProps {
  open: boolean;
  anchorEl: HTMLElement | null;
  selection: CellSelection | null;
  onConfirm: (mapping: DimensionMapping) => void;
  onCancel: () => void;
  existingMappings: DimensionMapping[];
}

const DIMENSION_TYPES = [
  { value: 'time', label: 'Time' },
  { value: 'locations', label: 'Locations' },
  { value: 'indicator_names', label: 'Indicator Names' },
  { value: 'indicator_values', label: 'Indicator Values' },
  { value: 'source', label: 'Source' },
  { value: 'unit', label: 'Unit' },
  { value: 'goals', label: 'Goals' },
  { value: 'additional_dimension', label: 'Additional Dimension' }
];

const TIME_SUBTYPES = [
  { value: 'year', label: 'Year' },
  { value: 'month', label: 'Month' },
  { value: 'day', label: 'Day' },
  { value: 'quarter', label: 'Quarter' }
];

const LOCATION_SUBTYPES = [
  { value: 'country', label: 'Country' },
  { value: 'state', label: 'State/Province' },
  { value: 'city', label: 'City' },
  { value: 'region', label: 'Region' },
  { value: 'district', label: 'District' }
];

const MAPPING_COLORS = [
  '#2196f3', // Blue
  '#9c27b0', // Purple
  '#4caf50', // Green
  '#ff9800', // Orange
  '#e91e63', // Pink
  '#8bc34a', // Light Green
  '#00bcd4', // Cyan
  '#795548', // Brown
  '#607d8b', // Blue Grey
  '#f44336'  // Red
];

export const DimensionMappingPopup: React.FC<DimensionMappingPopupProps> = ({
  open,
  anchorEl,
  selection,
  onConfirm,
  onCancel,
  existingMappings
}) => {
  const [dimensionType, setDimensionType] = useState<string>('');
  const [subType, setSubType] = useState<string>('');
  const [customDimensionName, setCustomDimensionName] = useState<string>('');
  const [mappingDirection, setMappingDirection] = useState<'row' | 'column'>('row');
  const [uniqueValues, setUniqueValues] = useState<string[]>([]);
  const [error, setError] = useState<string>('');

  // Generate a unique color for this mapping
  const getAvailableColor = (): string => {
    const usedColors = existingMappings.map(m => m.color);
    const availableColor = MAPPING_COLORS.find(color => !usedColors.includes(color));
    return availableColor || MAPPING_COLORS[existingMappings.length % MAPPING_COLORS.length];
  };

  // Auto-detect mapping direction based on selection shape
  const detectMappingDirection = (selection: CellSelection): 'row' | 'column' => {
    const rowSpan = selection.endRow - selection.startRow + 1;
    const colSpan = selection.endCol - selection.startCol + 1;
    
    // If it spans more columns than rows, it's likely a row mapping
    if (colSpan > rowSpan) {
      return 'row';
    }
    // If it spans more rows than columns, it's likely a column mapping
    else if (rowSpan > colSpan) {
      return 'column';
    }
    // For square selections, default to row (more common for headers)
    else {
      return 'row';
    }
  };

  useEffect(() => {
    if (selection) {
      // Extract unique values from the selection
      const values = extractUniqueValues(selection, []);
      setUniqueValues(values);
      
      // Auto-detect mapping direction
      setMappingDirection(detectMappingDirection(selection));
      
      // Reset form
      setDimensionType('');
      setSubType('');
      setCustomDimensionName('');
      setError('');
    }
  }, [selection]);

  const handleConfirm = () => {
    if (!selection) return;

    // Validation
    if (!dimensionType) {
      setError('Please select a dimension type');
      return;
    }

    if (dimensionType === 'time' && !subType) {
      setError('Please select a time subtype');
      return;
    }

    if (dimensionType === 'locations' && !subType) {
      setError('Please select a location subtype');
      return;
    }

    if (dimensionType === 'additional_dimension' && !customDimensionName.trim()) {
      setError('Please enter a custom dimension name');
      return;
    }
    if (dimensionType === 'additional_dimension' && customDimensionName.trim().toLowerCase() === 'generic') {
      setError('"generic" is not allowed as a custom dimension name. Please use a descriptive name.');
      return;
    }

    // Check for duplicate dimension types (except indicator_values)
    if (dimensionType !== 'indicator_values') {
      if (dimensionType === 'additional_dimension') {
        // For additional_dimension, check for duplicate customDimensionName
        const existingAdditionalDimension = existingMappings.find(m => 
          m.dimensionType === 'additional_dimension' && 
          m.customDimensionName === customDimensionName.trim()
        );
        if (existingAdditionalDimension) {
          setError(`A mapping for additional dimension "${customDimensionName.trim()}" already exists`);
          return;
        }
      } else {
        // For other dimension types, check for duplicate dimensionType
        const existingType = existingMappings.find(m => m.dimensionType === dimensionType);
        if (existingType) {
          setError(`A mapping for ${dimensionType} already exists`);
          return;
        }
      }
    }

    const mapping: DimensionMapping = {
      id: `mapping_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
      selection,
      dimensionType: dimensionType as DimensionMapping['dimensionType'],
      subType: subType || undefined,
      customDimensionName: customDimensionName || undefined,
      uniqueValues,
      color: getAvailableColor(),
      mappingDirection: mappingDirection
    };

    onConfirm(mapping);
  };

  const handleCancel = () => {
    setDimensionType('');
    setSubType('');
    setCustomDimensionName('');
    setMappingDirection('row');
    setError('');
    onCancel();
  };

  if (!selection) return null;

  return (
    <Popover
      open={open}
      anchorEl={anchorEl}
      onClose={handleCancel}
      anchorOrigin={{
        vertical: 'bottom',
        horizontal: 'left',
      }}
      transformOrigin={{
        vertical: 'top',
        horizontal: 'left',
      }}
      PaperProps={{
        sx: { width: 400, maxHeight: 600 }
      }}
    >
      <Box sx={{ p: 3 }}>
        <Typography variant="h6" gutterBottom>
          Map Cell Selection
        </Typography>
        
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          Selected {selection.selectedCells.length} cells from row {selection.startRow} to {selection.endRow}, 
          column {selection.startCol} to {selection.endCol}
          {selection.startRow !== selection.endRow && selection.startCol !== selection.endCol && (
            <span style={{ display: 'block', marginTop: '4px', fontStyle: 'italic' }}>
              Note: You can select partial rows or columns. Values outside the selection will use the closest selected cell.
            </span>
          )}
        </Typography>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        <FormControl fullWidth sx={{ mb: 2 }}>
          <InputLabel>Dimension Type</InputLabel>
          <Select
            value={dimensionType}
            label="Dimension Type"
            onChange={(e) => {
              setDimensionType(e.target.value);
              setSubType('');
              setError('');
            }}
          >
            {DIMENSION_TYPES.map((type) => (
              <MenuItem key={type.value} value={type.value}>
                {type.label}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <FormControl fullWidth sx={{ mb: 2 }}>
          <InputLabel>Mapping Direction</InputLabel>
          <Select
            value={mappingDirection}
            label="Mapping Direction"
            onChange={(e) => setMappingDirection(e.target.value as 'row' | 'column')}
          >
            <MenuItem value="row">Row (e.g., Gender in row 2, Year in row 3)</MenuItem>
            <MenuItem value="column">Column (e.g., Indicator names in column 0)</MenuItem>
          </Select>
        </FormControl>

        {dimensionType === 'time' && (
          <FormControl fullWidth sx={{ mb: 2 }}>
            <InputLabel>Time Type</InputLabel>
            <Select
              value={subType}
              label="Time Type"
              onChange={(e) => setSubType(e.target.value)}
            >
              {TIME_SUBTYPES.map((type) => (
                <MenuItem key={type.value} value={type.value}>
                  {type.label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        )}

        {dimensionType === 'locations' && (
          <FormControl fullWidth sx={{ mb: 2 }}>
            <InputLabel>Location Type</InputLabel>
            <Select
              value={subType}
              label="Location Type"
              onChange={(e) => setSubType(e.target.value)}
            >
              {LOCATION_SUBTYPES.map((type) => (
                <MenuItem key={type.value} value={type.value}>
                  {type.label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        )}

        {dimensionType === 'additional_dimension' && (
          <TextField
            fullWidth
            label="Dimension Name"
            placeholder="e.g., Gender, Sector, Age Group"
            value={customDimensionName}
            onChange={(e) => setCustomDimensionName(e.target.value)}
            sx={{ mb: 2 }}
          />
        )}

        <Divider sx={{ my: 2 }} />

        <Typography variant="subtitle2" gutterBottom>
          Unique Values Found ({uniqueValues.length}):
        </Typography>
        
        <Box sx={{ mb: 2, maxHeight: 100, overflow: 'auto' }}>
          {uniqueValues.length > 0 ? (
            <Box display="flex" gap={0.5} flexWrap="wrap">
              {uniqueValues.slice(0, 10).map((value, index) => (
                <Chip
                  key={index}
                  label={value}
                  size="small"
                  variant="outlined"
                />
              ))}
                              {uniqueValues.length > 10 && (
                  <Chip
                    label={`+${uniqueValues.length - 10} more`}
                    size="small"
                    variant="outlined"
                    color="default"
                  />
                )}
              </Box>
            ) : (
              <Typography variant="body2" color="text.secondary">
                No unique values found
              </Typography>
            )}
          </Box>

          <Box display="flex" gap={1} justifyContent="flex-end">
            <Button onClick={handleCancel} variant="outlined">
              Cancel
            </Button>
            <Button 
              onClick={handleConfirm} 
              variant="contained"
              disabled={!dimensionType}
            >
              Confirm Mapping
            </Button>
          </Box>
        </Box>
      </Popover>
    );
  };