'use client';
import React, { useState } from 'react';
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
} from '@mui/material';
import { ManualIndicatorData } from '@/types/indicators';
import { Subarea } from '@/types/subareas';

interface AddIndicatorFormProps {
  subareas: Subarea[];
  onIndicatorAdd: (indicator: ManualIndicatorData) => void;
  onCancel: () => void;
}

export const AddIndicatorForm: React.FC<AddIndicatorFormProps> = ({
  subareas,
  onIndicatorAdd,
  onCancel,
}) => {
  const [formData, setFormData] = useState<ManualIndicatorData>({
    name: '',
    description: '',
    unit: '',
    source: '',
    dataType: 'decimal',
    subareaId: '',
    direction: 'input',
    aggregationWeight: 1.0,
    estimatedValues: 0,
  });

  const [errors, setErrors] = useState<{ [key: string]: string }>({});

  const validateForm = () => {
    const newErrors: { [key: string]: string } = {};

    if (!formData.name.trim()) {
      newErrors.name = 'Indicator name is required';
    } else if (formData.name.trim().length < 3) {
      newErrors.name = 'Indicator name must be at least 3 characters long';
    }

    if (!formData.description.trim()) {
      newErrors.description = 'Description is required';
    } else if (formData.description.trim().length < 10) {
      newErrors.description = 'Description must be at least 10 characters long';
    }

    if (formData.estimatedValues && formData.estimatedValues < 0) {
      newErrors.estimatedValues = 'Estimated values must be non-negative';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSave = () => {
    if (validateForm()) {
      onIndicatorAdd(formData);
    }
  };

  const handleCancel = () => {
    onCancel();
  };

  return (
    <Dialog open={true} maxWidth="md" fullWidth>
      <DialogTitle>Add New Indicator</DialogTitle>
      <DialogContent>
        <Grid container spacing={2} sx={{ mt: 1 }}>
          <Grid item xs={12}>
            <TextField
              label="Indicator Name"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              required
              fullWidth
              error={!!errors.name}
              helperText={errors.name}
            />
          </Grid>
          
          <Grid item xs={12}>
            <TextField
              label="Description"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              multiline
              rows={3}
              fullWidth
              required
              error={!!errors.description}
              helperText={errors.description}
            />
          </Grid>
          
          <Grid item xs={6}>
            <TextField
              label="Unit"
              value={formData.unit}
              onChange={(e) => setFormData({ ...formData, unit: e.target.value })}
              fullWidth
              placeholder="e.g., %, EUR, kg"
            />
          </Grid>
          
          <Grid item xs={6}>
            <TextField
              label="Source"
              value={formData.source}
              onChange={(e) => setFormData({ ...formData, source: e.target.value })}
              fullWidth
              placeholder="e.g., Eurostat, National Statistics"
            />
          </Grid>
          
          <Grid item xs={6}>
            <FormControl fullWidth>
              <InputLabel>Data Type</InputLabel>
              <Select
                label="Data Type"
                value={formData.dataType}
                onChange={(e) => setFormData({ ...formData, dataType: e.target.value })}
              >
                <MenuItem value="integer">Integer</MenuItem>
                <MenuItem value="decimal">Decimal</MenuItem>
                <MenuItem value="percentage">Percentage</MenuItem>
                <MenuItem value="index">Index</MenuItem>
              </Select>
            </FormControl>
          </Grid>
          
          <Grid item xs={6}>
            <FormControl fullWidth>
              <InputLabel>Subarea</InputLabel>
              <Select
                label="Subarea"
                value={formData.subareaId}
                onChange={(e) => setFormData({ ...formData, subareaId: e.target.value })}
              >
                <MenuItem value="">No Subarea</MenuItem>
                {subareas.map(subarea => (
                  <MenuItem key={subarea.id} value={subarea.id}>
                    {subarea.name}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
          
          <Grid item xs={6}>
            <Box>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Direction
              </Typography>
              <ToggleButtonGroup
                value={formData.direction}
                exclusive
                onChange={(e, value) => value && setFormData({ ...formData, direction: value })}
                fullWidth
              >
                <ToggleButton value="input">Input</ToggleButton>
                <ToggleButton value="output">Output</ToggleButton>
              </ToggleButtonGroup>
            </Box>
          </Grid>
          
          <Grid item xs={6}>
            <TextField
              label="Estimated Values"
              type="number"
              value={formData.estimatedValues}
              onChange={(e) => setFormData({ ...formData, estimatedValues: parseInt(e.target.value) || 0 })}
              fullWidth
              error={!!errors.estimatedValues}
              helperText={errors.estimatedValues || 'Expected number of data points'}
            />
          </Grid>
          
          <Grid item xs={12}>
            <TextField
              label="Aggregation Weight"
              type="number"
              value={formData.aggregationWeight}
              onChange={(e) => setFormData({ ...formData, aggregationWeight: parseFloat(e.target.value) || 1.0 })}
              fullWidth
              inputProps={{ min: 0, step: 0.1 }}
              helperText="Weight for aggregation calculations (default: 1.0)"
            />
          </Grid>
        </Grid>
      </DialogContent>
      <DialogActions>
        <Button onClick={handleCancel}>Cancel</Button>
        <Button onClick={handleSave} variant="contained">
          Add Indicator
        </Button>
      </DialogActions>
    </Dialog>
  );
}; 