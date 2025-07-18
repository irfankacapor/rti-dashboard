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
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
  Button,
} from '@mui/material';
import {
  Edit as EditIcon,
  Delete as DeleteIcon,
  Save as SaveIcon,
  Cancel as CancelIcon
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
  onDeleteWithData?: () => void;
  isSaving: boolean;
  onEditValues?: (indicatorId: string) => void;
  onOpenUnitPicker: () => void;
  onOpenDirectionModal: () => void;
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
  onDeleteWithData,
  isSaving,
  onEditValues,
  onOpenUnitPicker,
  onOpenDirectionModal,
}) => {
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [formData, setFormData] = useState<IndicatorFormData>({
    name: indicator.name,
    description: indicator.description || '',
    unit: indicator.unit || '',
    unitId: indicator.unitId || undefined,
    unitPrefix: indicator.unitPrefix || '',
    unitSuffix: indicator.unitSuffix || '',
    source: indicator.source || '',
    dataType: indicator.dataType || 'decimal',
    subareaId: indicator.subareaId || '',
    direction: indicator.direction,
    aggregationWeight: indicator.aggregationWeight || 1.0,
  });

  useEffect(() => {
    if (isEditing) {
      setFormData({
        name: indicator.name,
        description: indicator.description || '',
        unit: indicator.unit || '',
        unitId: indicator.unitId || undefined,
        unitPrefix: indicator.unitPrefix || '',
        unitSuffix: indicator.unitSuffix || '',
        source: indicator.source || '',
        dataType: indicator.dataType || 'decimal',
        subareaId: indicator.subareaId || '',
        direction: indicator.direction,
        aggregationWeight: indicator.aggregationWeight || 1.0,
      });
    }
  }, [isEditing, indicator]);

  // Update formData when indicator changes (for unit picker updates)
  useEffect(() => {
    setFormData(prev => ({
      ...prev,
      unit: indicator.unit || '',
      unitId: indicator.unitId || undefined,
      unitPrefix: indicator.unitPrefix || '',
      unitSuffix: indicator.unitSuffix || '',
    }));
  }, [indicator.unit, indicator.unitId, indicator.unitPrefix, indicator.unitSuffix]);

  const handleSave = () => {
    onSave(formData);
  };

  const handleCancel = () => {
    onCancel();
  };

  const handleDeleteConfirm = () => {
    // If there's no related data, use regular delete
    // If there is related data but no onDeleteWithData function, still use regular delete (will fail gracefully)
    onDelete();
    setShowDeleteConfirm(false);
  };

  const handleDeleteWithDataConfirm = () => {
    // Always use delete with data when there's related data
    if (onDeleteWithData) {
      onDeleteWithData();
    } else {
      // Fallback to regular delete if onDeleteWithData is not available
      onDelete();
    }
    setShowDeleteConfirm(false);
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

  const formatUnitDisplay = (indicator: ManagedIndicator) => {
    const parts = [];
    if (indicator.unitPrefix) parts.push(indicator.unitPrefix);
    if (indicator.unit) parts.push(indicator.unit);
    if (indicator.unitSuffix) parts.push(indicator.unitSuffix);
    return parts.length > 0 ? parts.join(' ') : '-';
  };

  const editingRow = (
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
          sx={{ height: 40 }}
        />
      </TableCell>
      <TableCell>
        <TextField
          value={formData.description}
          onChange={(e) => setFormData({ ...formData, description: e.target.value })}
          size="small"
          fullWidth
          multiline
          rows={1}
          sx={{ height: 40 }}
        />
      </TableCell>
      <TableCell>
        <Typography variant="body2" color="text.secondary">
          Unit can be set outside of edit mode
        </Typography>
      </TableCell>
      <TableCell>
        <TextField
          value={formData.source}
          onChange={(e) => setFormData({ ...formData, source: e.target.value })}
          size="small"
          fullWidth
          sx={{ height: 40 }}
        />
      </TableCell>
      <TableCell>
        <FormControl size="small" fullWidth sx={{ height: 40 }}>
          <Select
            value={formData.dataType}
            onChange={(e) => setFormData({ ...formData, dataType: e.target.value })}
            sx={{ height: 40 }}
          >
            <MenuItem value="integer">Integer</MenuItem>
            <MenuItem value="decimal">Decimal</MenuItem>
            <MenuItem value="percentage">Percentage</MenuItem>
            <MenuItem value="index">Index</MenuItem>
          </Select>
        </FormControl>
      </TableCell>
      <TableCell>
        <FormControl size="small" fullWidth sx={{ height: 40 }}>
          <Select
            value={formData.subareaId}
            onChange={(e) => setFormData({ ...formData, subareaId: e.target.value })}
            sx={{ height: 40 }}
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
        <Typography variant="body2" color="text.secondary">
          Direction can be managed per subarea
        </Typography>
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
              label={typeof dimension === 'string' ? dimension : dimension.displayName}
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
          {isEditing ? (
            <>
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
            </>
          ) : (
            <Tooltip title="Edit">
              <IconButton
                size="small"
                onClick={onEdit}
                disabled={isSaving}
              >
                <EditIcon />
              </IconButton>
            </Tooltip>
          )}
          <Tooltip title="Delete">
            <IconButton
              size="small"
              onClick={() => setShowDeleteConfirm(true)}
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

  const displayRow = (
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
        <Box display="flex" alignItems="center" gap={1}>
          <Typography 
            variant="body2" 
            color={!indicator.unitId ? 'error' : 'inherit'}
            sx={{ 
              flex: 1,
              fontStyle: !indicator.unitId ? 'italic' : 'normal'
            }}
          >
            {formatUnitDisplay(indicator)}
          </Typography>
          <Button
            size="small"
            variant={!indicator.unitId ? "contained" : "outlined"}
            color={!indicator.unitId ? "error" : "primary"}
            onClick={onOpenUnitPicker}
            sx={{ 
              minWidth: 'auto', 
              px: 1,
              py: 0.5,
              fontSize: '0.75rem'
            }}
          >
            {!indicator.unitId ? 'SET' : 'CHANGE'}
          </Button>
        </Box>
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
        {/* Show all subareas as chips if available, otherwise show 'Unassigned' */}
        {Array.isArray(indicator.subareaNames) && indicator.subareaNames.length > 0 ? (
          <Box display="flex" gap={0.5} flexWrap="wrap">
            {indicator.subareaNames.map((name) => (
              <Chip key={name} label={name} size="small" color="primary" />
            ))}
          </Box>
        ) : (
          <Typography variant="body2" color="text.secondary" fontStyle="italic">
            Unassigned
          </Typography>
        )}
      </TableCell>
      <TableCell>
        <Button
          variant="outlined"
          size="small"
          onClick={onOpenDirectionModal}
          sx={{ minWidth: 'auto', px: 1, py: 0.5, fontSize: '0.75rem' }}
        >
          MANAGE
        </Button>
      </TableCell>
      <TableCell>
        <Button 
          variant="text" 
          onClick={() => onEditValues?.(indicator.id)}
          disabled={indicator.valueCount === 0}
          sx={{ minWidth: 'auto', p: 0.5, '&:hover': { backgroundColor: 'action.hover' } }}
        >
          <Typography variant="body2" color="primary">
            {indicator.valueCount}
          </Typography>
        </Button>
      </TableCell>
      <TableCell>
        <Box display="flex" gap={0.5} flexWrap="wrap">
          {indicator.dimensions?.map((dimension, index) => (
            <Chip
              key={index}
              label={typeof dimension === 'string' ? dimension : dimension.displayName}
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
              onClick={() => setShowDeleteConfirm(true)}
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

  return (
    <>
      {isEditing ? editingRow : displayRow}
      
      {/* Delete confirmation dialog */}
      <Dialog open={showDeleteConfirm} onClose={() => setShowDeleteConfirm(false)}>
        <DialogTitle>Confirm Deletion</DialogTitle>
        <DialogContent>
          <Alert severity="warning" sx={{ mb: 2 }}>
            This action cannot be undone.
          </Alert>
          <Typography>
            Are you sure you want to delete the indicator "{indicator.name}"?
          </Typography>
          {indicator.valueCount > 0 && (
            <Alert severity="info" sx={{ mt: 2 }}>
              <Typography variant="body2">
                This indicator has {indicator.valueCount} associated data values that will also be deleted.
              </Typography>
            </Alert>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowDeleteConfirm(false)}>Cancel</Button>
          {indicator.valueCount > 0 ? (
            <Button onClick={handleDeleteWithDataConfirm} color="error" variant="contained">
              Delete with Data
            </Button>
          ) : (
            <Button onClick={handleDeleteConfirm} color="error" variant="contained">
              Delete
            </Button>
          )}
        </DialogActions>
      </Dialog>
    </>
  );
}; 