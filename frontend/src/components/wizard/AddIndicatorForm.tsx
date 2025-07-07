'use client';
import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  ToggleButtonGroup,
  ToggleButton,
  Typography,
  Box,
  Chip,
  IconButton,
  Stepper,
  Step,
  StepLabel,
  InputAdornment,
} from '@mui/material';
import { ManualIndicatorData } from '@/types/indicators';
import { Subarea } from '@/types/subareas';
import { Add as AddIcon, Remove as RemoveIcon, Close as CloseIcon } from '@mui/icons-material';
import { indicatorManagementService } from '@/services/indicatorManagementService';

interface AddIndicatorFormProps {
  subareas: Subarea[];
  onIndicatorAdd: (indicator: ManualIndicatorData) => void;
  onCancel: () => void;
}

// Dimension types and subtypes (reuse from DimensionMappingPopup)
const DIMENSION_TYPES = [
  { value: 'time', label: 'Time', subTypes: [
    { value: 'year', label: 'Year' },
    { value: 'month', label: 'Month' },
    { value: 'day', label: 'Day' },
    { value: 'quarter', label: 'Quarter' },
  ] },
  { value: 'locations', label: 'Locations', subTypes: [
    { value: 'country', label: 'Country' },
    { value: 'state', label: 'State/Province' },
    { value: 'city', label: 'City' },
    { value: 'region', label: 'Region' },
    { value: 'district', label: 'District' },
  ] },
  { value: 'additional_dimension', label: 'Custom Dimension', subTypes: [] },
];

export const AddIndicatorForm: React.FC<AddIndicatorFormProps> = ({
  subareas,
  onIndicatorAdd,
  onCancel,
}) => {
  // Stepper state
  const [step, setStep] = useState(0); // 0: details, 1: data input

  // Indicator details state
  const [formData, setFormData] = useState<ManualIndicatorData>({
    name: '',
    description: '',
    unit: '',
    source: '',
    dataType: 'decimal',
    subareaId: '',
    type: 'input', // renamed from direction
  });

  // Dimensions state
  const [dimensions, setDimensions] = useState<{
    value: string;
    label: string;
    subType?: string;
    customName?: string;
  }[]>([]);
  const [dimensionSelect, setDimensionSelect] = useState('');
  const [dimensionSubType, setDimensionSubType] = useState('');
  const [customDimensionName, setCustomDimensionName] = useState('');

  // Data input state
  const [dataRows, setDataRows] = useState<any[]>([]); // Each row: { [dim]: value, ..., value: number }

  // Validation
  const [errors, setErrors] = useState<{ [key: string]: string }>({});

  // Indicator types state
  const [indicatorTypes, setIndicatorTypes] = useState<string[]>([]);
  const [loadingTypes, setLoadingTypes] = useState(false);
  const [typesError, setTypesError] = useState<string | null>(null);

  useEffect(() => {
    setLoadingTypes(true);
    indicatorManagementService.getIndicatorTypes()
      .then(setIndicatorTypes)
      .catch(() => setTypesError('Failed to load indicator types'))
      .finally(() => setLoadingTypes(false));
  }, []);

  // --- Step 1: Indicator Details ---
  const handleAddDimension = () => {
    if (!dimensionSelect) return;
    const dimType = DIMENSION_TYPES.find(d => d.value === dimensionSelect);
    if (!dimType) return;
    if (dimensionSelect === 'additional_dimension' && !customDimensionName.trim()) return;
    // Prevent duplicates
    if (dimensions.some(d => d.value === dimensionSelect && (d.customName === customDimensionName.trim() || !d.customName))) return;
    setDimensions([
      ...dimensions,
      {
        value: dimensionSelect,
        label: dimType.label,
        subType: dimensionSubType || undefined,
        customName: dimensionSelect === 'additional_dimension' ? customDimensionName.trim() : undefined,
      },
    ]);
    setDimensionSelect('');
    setDimensionSubType('');
    setCustomDimensionName('');
  };
  const handleRemoveDimension = (idx: number) => {
    setDimensions(dimensions.filter((_, i) => i !== idx));
  };

  // --- Step 2: Data Input ---
  const handleAddRow = () => {
    const emptyRow: any = {};
    dimensions.forEach(dim => {
      emptyRow[dim.customName || dim.value] = '';
    });
    emptyRow.value = '';
    setDataRows([...dataRows, emptyRow]);
  };
  const handleRemoveRow = (idx: number) => {
    setDataRows(dataRows.filter((_, i) => i !== idx));
  };
  const handleRowChange = (idx: number, key: string, value: string) => {
    setDataRows(dataRows.map((row, i) => i === idx ? { ...row, [key]: value } : row));
  };

  // --- Navigation ---
  const handleNext = () => {
    // Validate step 1
    const newErrors: { [key: string]: string } = {};
    if (!formData.name.trim()) newErrors.name = 'Indicator name is required';
    if (!formData.description?.trim()) newErrors.description = 'Description is required';
    if (dimensions.length === 0) newErrors.dimensions = 'At least one dimension is required';
    setErrors(newErrors);
    if (Object.keys(newErrors).length === 0) setStep(1);
  };
  const handleBack = () => setStep(0);

  // --- Submit ---
  const handleSave = () => {
    // Validate data input
    const newErrors: { [key: string]: string } = {};
    if (dataRows.length === 0) {
      newErrors.dataRows = 'At least one data row is required';
    } else {
      // Validate each row: value must be present and numeric, and all dimension fields must be filled
      dataRows.forEach((row, idx) => {
        if (row.value === undefined || row.value === '' || isNaN(Number(row.value))) {
          newErrors[`dataRow_value_${idx}`] = 'Value is required and must be a number';
        }
        dimensions.forEach(dim => {
          const key = dim.customName || dim.value;
          if (!row[key] || row[key].toString().trim() === '') {
            newErrors[`dataRow_${key}_${idx}`] = `${dim.label} is required`;
          }
        });
      });
    }
    setErrors(newErrors);
    if (Object.keys(newErrors).length > 0) return;

    // Standardize dimensions: convert 'locations' to 'location'
    const standardizedDimensions = dimensions.map(d => {
      const value = d.customName || d.value;
      return value === 'locations' ? 'location' : value;
    });
    // Standardize dataRows keys as well
    const standardizedDataRows = dataRows.map(row => {
      const newRow: any = {};
      Object.keys(row).forEach(key => {
        let stdKey = key;
        if (key === 'locations') stdKey = 'location';
        newRow[stdKey] = row[key];
      });
      return newRow;
    });
    onIndicatorAdd({ ...formData, dimensions: standardizedDimensions, dataRows: standardizedDataRows });
  };

  // --- Render ---
  return (
    <Dialog open={true} maxWidth="md" fullWidth>
      <DialogTitle>Add New Indicator</DialogTitle>
      <DialogContent>
        <Stepper activeStep={step} alternativeLabel sx={{ mb: 3 }}>
          <Step key="details"><StepLabel>Details</StepLabel></Step>
          <Step key="data"><StepLabel>Data Input</StepLabel></Step>
        </Stepper>
        {step === 0 && (
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid size={12}>
              <TextField
                label="Indicator Name"
                value={formData.name}
                onChange={e => setFormData({ ...formData, name: e.target.value })}
                required
                fullWidth
                error={!!errors.name}
                helperText={errors.name}
              />
            </Grid>
            <Grid size={12}>
              <TextField
                label="Description"
                value={formData.description}
                onChange={e => setFormData({ ...formData, description: e.target.value })}
                multiline
                rows={3}
                fullWidth
                required
                error={!!errors.description}
                helperText={errors.description}
              />
            </Grid>
            <Grid size={6}>
              <TextField
                label="Unit"
                value={formData.unit}
                onChange={e => setFormData({ ...formData, unit: e.target.value })}
                fullWidth
                placeholder="e.g., %, EUR, kg"
              />
            </Grid>
            <Grid size={6}>
              <TextField
                label="Source"
                value={formData.source}
                onChange={e => setFormData({ ...formData, source: e.target.value })}
                fullWidth
                placeholder="e.g., Eurostat, National Statistics"
              />
            </Grid>
            <Grid size={6}>
              <FormControl fullWidth>
                <InputLabel>Data Type</InputLabel>
                <Select
                  label="Data Type"
                  value={formData.dataType}
                  onChange={e => setFormData({ ...formData, dataType: e.target.value })}
                >
                  <MenuItem value="integer">Integer</MenuItem>
                  <MenuItem value="decimal">Decimal</MenuItem>
                  <MenuItem value="percentage">Percentage</MenuItem>
                  <MenuItem value="index">Index</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid size={6}>
              <FormControl fullWidth>
                <InputLabel>Subarea</InputLabel>
                <Select
                  label="Subarea"
                  value={formData.subareaId}
                  onChange={e => setFormData({ ...formData, subareaId: e.target.value })}
                >
                  <MenuItem value="">No Subarea</MenuItem>
                  {subareas.map(subarea => (
                    <MenuItem key={subarea.id} value={subarea.id}>{subarea.name}</MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid size={6}>
              <FormControl fullWidth>
                <InputLabel>Type</InputLabel>
                <Select
                  label="Type"
                  value={formData.type}
                  onChange={e => setFormData({ ...formData, type: e.target.value })}
                  disabled={loadingTypes || !!typesError}
                >
                  <MenuItem value="" disabled>
                    {loadingTypes ? 'Loading types...' : typesError ? 'Error loading types' : 'Select type'}
                  </MenuItem>
                  {indicatorTypes.map(type => (
                    <MenuItem key={type.toLowerCase()} value={type.toLowerCase()}>{type.charAt(0) + type.slice(1).toLowerCase()}</MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid size={12}>
              <Box display="flex" alignItems="center" gap={2}>
                <FormControl sx={{ minWidth: 200 }}>
                  <InputLabel>Dimension</InputLabel>
                  <Select
                    value={dimensionSelect}
                    label="Dimension"
                    onChange={e => setDimensionSelect(e.target.value)}
                  >
                    {DIMENSION_TYPES.map(dim => (
                      <MenuItem key={dim.value} value={dim.value}>{dim.label}</MenuItem>
                    ))}
                  </Select>
                </FormControl>
                {dimensionSelect && (DIMENSION_TYPES.find(d => d.value === dimensionSelect)?.subTypes?.length ?? 0) > 0 && (
                  <FormControl sx={{ minWidth: 160 }}>
                    <InputLabel>Subtype</InputLabel>
                    <Select
                      value={dimensionSubType}
                      label="Subtype"
                      onChange={e => setDimensionSubType(e.target.value)}
                    >
                      {(DIMENSION_TYPES.find(d => d.value === dimensionSelect)?.subTypes ?? []).map(st => (
                        <MenuItem key={st.value} value={st.value}>{st.label}</MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                )}
                {dimensionSelect === 'additional_dimension' && (
                  <TextField
                    label="Custom Name"
                    value={customDimensionName}
                    onChange={e => setCustomDimensionName(e.target.value)}
                  />
                )}
                <Button variant="outlined" onClick={handleAddDimension} disabled={!dimensionSelect || (dimensionSelect === 'additional_dimension' && !customDimensionName.trim())}>
                  Add
                </Button>
              </Box>
              <Box mt={2} display="flex" gap={1} flexWrap="wrap">
                {dimensions.map((dim, idx) => (
                  <Chip
                    key={idx}
                    label={dim.customName ? `${dim.label}: ${dim.customName}` : dim.label + (dim.subType ? ` (${dim.subType})` : '')}
                    onDelete={() => handleRemoveDimension(idx)}
                    onMouseOver={e => (e.currentTarget.style.opacity = '0.7')}
                    onMouseOut={e => (e.currentTarget.style.opacity = '1')}
                  />
                ))}
                {errors.dimensions && <Typography color="error">{errors.dimensions}</Typography>}
              </Box>
            </Grid>
          </Grid>
        )}
        {step === 1 && (
          <Box>
            <Box display="flex" alignItems="center" mb={2}>
              <Typography variant="subtitle1">Input Data</Typography>
              <Button startIcon={<AddIcon />} onClick={handleAddRow} sx={{ ml: 2 }}>Add Row</Button>
            </Box>
            <Box sx={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                  <tr>
                    {dimensions.map(dim => (
                      <th key={dim.customName || dim.value} style={{ border: '1px solid #ccc', padding: 8 }}>{dim.customName || dim.label}{dim.subType ? ` (${dim.subType})` : ''}</th>
                    ))}
                    <th style={{ border: '1px solid #ccc', padding: 8 }}>Value</th>
                    <th style={{ border: '1px solid #ccc', padding: 8 }}></th>
                  </tr>
                </thead>
                <tbody>
                  {dataRows.map((row, idx) => (
                    <tr key={idx}>
                      {dimensions.map(dim => (
                        <td key={dim.customName || dim.value} style={{ border: '1px solid #ccc', padding: 8 }}>
                          <TextField
                            value={row[dim.customName || dim.value]}
                            onChange={e => handleRowChange(idx, dim.customName || dim.value, e.target.value)}
                            size="small"
                          />
                        </td>
                      ))}
                      <td style={{ border: '1px solid #ccc', padding: 8 }}>
                        <TextField
                          value={row.value}
                          onChange={e => handleRowChange(idx, 'value', e.target.value)}
                          size="small"
                          InputProps={{
                            endAdornment: <InputAdornment position="end">{formData.unit}</InputAdornment>
                          }}
                        />
                      </td>
                      <td style={{ border: '1px solid #ccc', padding: 8 }}>
                        <IconButton onClick={() => handleRemoveRow(idx)} size="small"><RemoveIcon /></IconButton>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </Box>
            {errors.dataRows && (
              <Typography color="error" sx={{ mb: 1 }}>{errors.dataRows}</Typography>
            )}
            <Button variant="outlined" onClick={handleAddRow} sx={{ mt: 1, mb: 2 }}>
              Add Data Row
            </Button>
          </Box>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onCancel}>Cancel</Button>
        {step === 1 && <Button onClick={handleBack}>Back</Button>}
        {step === 0 && <Button onClick={handleNext} variant="contained">Next</Button>}
        {step === 1 && <Button onClick={handleSave} variant="contained">Add Indicator</Button>}
      </DialogActions>
    </Dialog>
  );
}; 