'use client';
import React, { useState, useEffect } from 'react';
import {
  TableRow,
  TableCell,
  Checkbox,
  IconButton,
  Chip,
  Tooltip,
  Box,
  Typography,
  Select,
  MenuItem,
  FormControl,
  TextField,
  ToggleButtonGroup,
  ToggleButton,
} from '@mui/material';
import {
  Edit as EditIcon,
  Delete as DeleteIcon,
  Save as SaveIcon,
  Cancel as CancelIcon,
  Visibility as ViewIcon,
} from '@mui/icons-material';
import { ManagedIndicator, IndicatorFormData } from '@/types/indicators';
import { Subarea } from '@/types/subareas';

interface IndicatorTableRowProps {
  indicator: ManagedIndicator;
  isEditing: boolean;
  isSelected: boolean;
  subareas: Subarea[];
  onEdit: () => void;
  onSave: (data: Partial<ManagedIndicator>) => void;
  onCancel: () => void;
  onSelect: (selected: boolean) => void;
  onDelete: () => void;
  isSaving: boolean;
}

export const IndicatorTableRow: React.FC<IndicatorTableRowProps> = ({
  indicator,
  isEditing,
  isSelected,
  subareas,
  onEdit,
  onSave,
  onCancel,
  onSelect,
  onDelete,
  isSaving,
}) => {
  const [formData, setFormData] = useState<IndicatorFormData>({
    name: indicator.name,
    description: indicator.description || '',
    unit: indicator.unit || '',
    source: indicator.source || '',
    dataType: indicator.dataType || 'decimal',
    subareaId: indicator.subareaId || '',
    direction: indicator.direction || 'input',
    aggregationWeight: indicator.aggregationWeight || 1.0,
  });

  useEffect(() => {
    if (isEditing) {
      setFormData({
        name: indicator.name,
        description: indicator.description || '',
        unit: indicator.unit || '',
        source: indicator.source || '',
        dataType: indicator.dataType || 'decimal',
        subareaId: indicator.subareaId || '',
        direction: indicator.direction || 'input',
        aggregationWeight: indicator.aggregationWeight || 1.0,
      });
    }
  }, [isEditing, indicator]);

  const handleSave = () => {
    onSave(formData);
  };

  const handleCancel = () => {
    onCancel();
  };

  const getSubareaName = (subareaId: string) => {
    const subarea = subareas.find(s => s.id === subareaId);
    return subarea?.name || 'Unknown';
  };

  const getDirectionColor = (direction: string) => {
    return direction === 'input' ? 'primary' : 'secondary';
  };

  const getDataTypeColor = (dataType: string) => {
    switch (dataType) {
      case 'integer': return 'default';
      case 'decimal': return 'primary';
      case 'percentage': return 'success';
      case 'index': return 'warning';
      default: return 'default';
    }
  };

  if (isEditing) {
    return (
      <TableRow>
        <TableCell padding="checkbox">
          <Checkbox
            checked={isSelected}
            onChange={(e) => onSelect(e.target.checked)}
            disabled={isSaving}
          />
        </TableCell>
        <TableCell>
          <TextField
            value={formData.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            size="small"
            fullWidth
            required
          />
        </TableCell>
        <TableCell>
          <TextField
            value={formData.description}
            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            size="small"
            fullWidth
            multiline
            rows={2}
          />
        </TableCell>
        <TableCell>
          <TextField
            value={formData.unit}
            onChange={(e) => setFormData({ ...formData, unit: e.target.value })}
            size="small"
            fullWidth
          />
        </TableCell>
        <TableCell>
          <TextField
            value={formData.source}
            onChange={(e) => setFormData({ ...formData, source: e.target.value })}
            size="small"
            fullWidth
          />
        </TableCell>
        <TableCell>
          <FormControl size="small" fullWidth>
            <Select
              value={formData.dataType}
              onChange={(e) => setFormData({ ...formData, dataType: e.target.value })}
            >
              <MenuItem value="integer">Integer</MenuItem>
              <MenuItem value="decimal">Decimal</MenuItem>
              <MenuItem value="percentage">Percentage</MenuItem>
              <MenuItem value="index">Index</MenuItem>
            </Select>
          </FormControl>
        </TableCell>
        <TableCell>
          <FormControl size="small" fullWidth>
            <Select
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
        </TableCell>
        <TableCell>
          <ToggleButtonGroup
            value={formData.direction}
            exclusive
            size="small"
            onChange={(e, value) => value && setFormData({ ...formData, direction: value })}
          >
            <ToggleButton value="input">Input</ToggleButton>
            <ToggleButton value="output">Output</ToggleButton>
          </ToggleButtonGroup>
        </TableCell>
        <TableCell>
          <Typography variant="body2" color="text.secondary">
            {indicator.valueCount}
          </Typography>
        </TableCell>
        <TableCell>
          <Box display="flex" gap={0.5} flexWrap="wrap">
            {indicator.dimensions?.map((dimension, index) => (
              <Chip
                key={index}
                label={dimension}
                size="small"
                variant="outlined"
              />
            )) || (
              <Typography variant="body2" color="text.secondary">
                No dimensions
              </Typography>
            )}
          </Box>
        </TableCell>
        <TableCell>
          <Box display="flex" gap={0.5}>
            <Tooltip title="Save">
              <IconButton
                size="small"
                onClick={handleSave}
                disabled={isSaving}
                color="primary"
              >
                <SaveIcon />
              </IconButton>
            </Tooltip>
            <Tooltip title="Cancel">
              <IconButton
                size="small"
                onClick={handleCancel}
                disabled={isSaving}
              >
                <CancelIcon />
              </IconButton>
            </Tooltip>
          </Box>
        </TableCell>
      </TableRow>
    );
  }

  return (
    <TableRow>
      <TableCell padding="checkbox">
        <Checkbox
          checked={isSelected}
          onChange={(e) => onSelect(e.target.checked)}
          disabled={isSaving}
        />
      </TableCell>
      <TableCell>
        <Typography variant="body2" fontWeight="medium">
          {indicator.name}
        </Typography>
        {indicator.isModified && (
          <Chip
            label="Modified"
            size="small"
            color="warning"
            sx={{ mt: 0.5 }}
          />
        )}
      </TableCell>
      <TableCell>
        <Typography variant="body2" color="text.secondary">
          {indicator.description || 'No description'}
        </Typography>
      </TableCell>
      <TableCell>
        <Typography variant="body2">
          {indicator.unit || '-'}
        </Typography>
      </TableCell>
      <TableCell>
        <Typography variant="body2">
          {indicator.source || '-'}
        </Typography>
      </TableCell>
      <TableCell>
        <Chip
          label={indicator.dataType || 'decimal'}
          size="small"
          color={getDataTypeColor(indicator.dataType || 'decimal')}
        />
      </TableCell>
      <TableCell>
        {indicator.subareaId ? (
          <Typography variant="body2">
            {getSubareaName(indicator.subareaId)}
          </Typography>
        ) : (
          <Typography variant="body2" color="text.secondary">
            Unassigned
          </Typography>
        )}
      </TableCell>
      <TableCell>
        <Chip
          label={indicator.direction || 'input'}
          size="small"
          color={getDirectionColor(indicator.direction || 'input')}
        />
      </TableCell>
      <TableCell>
        <Typography variant="body2" color="text.secondary">
          {indicator.valueCount}
        </Typography>
      </TableCell>
      <TableCell>
        <Box display="flex" gap={0.5} flexWrap="wrap">
          {indicator.dimensions?.map((dimension, index) => (
            <Chip
              key={index}
              label={dimension}
              size="small"
              variant="outlined"
            />
          )) || (
            <Typography variant="body2" color="text.secondary">
              No dimensions
            </Typography>
          )}
        </Box>
      </TableCell>
      <TableCell>
        <Box display="flex" gap={0.5}>
          <Tooltip title="Edit">
            <IconButton
              size="small"
              onClick={onEdit}
              disabled={isSaving}
            >
              <EditIcon />
            </IconButton>
          </Tooltip>
          <Tooltip title="Delete">
            <IconButton
              size="small"
              onClick={onDelete}
              disabled={isSaving}
              color="error"
            >
              <DeleteIcon />
            </IconButton>
          </Tooltip>
        </Box>
      </TableCell>
    </TableRow>
  );
}; 