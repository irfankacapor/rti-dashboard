'use client';
import React, { useState } from 'react';
import {
  Paper,
  Box,
  Typography,
  Button,
  Select,
  MenuItem,
  ToggleButtonGroup,
  ToggleButton,
  FormControl,
  InputLabel,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
} from '@mui/material';
import { Delete as DeleteIcon } from '@mui/icons-material';
import { ManagedIndicator } from '@/types/indicators';
import { Subarea } from '@/types/subareas';

interface BulkIndicatorActionsProps {
  selectedIndicators: ManagedIndicator[];
  subareas: Subarea[];
  onBulkUpdate: (updates: { id: string; updates: Partial<ManagedIndicator> }[]) => void;
  onBulkDelete: (ids: string[]) => void;
  onBulkDeleteWithData?: (ids: string[]) => void;
}

export const BulkIndicatorActions: React.FC<BulkIndicatorActionsProps> = ({
  selectedIndicators,
  subareas,
  onBulkUpdate,
  onBulkDelete,
  onBulkDeleteWithData,
}) => {
  const [selectedSubarea, setSelectedSubarea] = useState<string>('');
  const [selectedDirection, setSelectedDirection] = useState<'input' | 'output' | ''>('');
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

  const handleAssignSubarea = () => {
    if (selectedSubarea) {
      const updates = selectedIndicators.map(indicator => ({
        id: indicator.id,
        updates: { subareaId: selectedSubarea }
      }));
      onBulkUpdate(updates);
      setSelectedSubarea('');
    }
  };

  const handleSetDirection = () => {
    if (selectedDirection) {
      const updates = selectedIndicators.map(indicator => ({
        id: indicator.id,
        updates: { direction: selectedDirection }
      }));
      onBulkUpdate(updates);
      setSelectedDirection('');
    }
  };

  const handleDeleteConfirm = () => {
    onBulkDelete(selectedIndicators.map(i => i.id));
    setShowDeleteConfirm(false);
  };

  const handleDeleteWithDataConfirm = () => {
    if (onBulkDeleteWithData) {
      onBulkDeleteWithData(selectedIndicators.map(i => i.id));
    } else {
      onBulkDelete(selectedIndicators.map(i => i.id));
    }
    setShowDeleteConfirm(false);
  };

  const handleDeleteOnlyWithoutData = () => {
    onBulkDelete(indicatorsWithoutData.map(i => i.id));
    setShowDeleteConfirm(false);
  };

  // Check if any selected indicators have related data
  const hasRelatedData = selectedIndicators.some(indicator => indicator.valueCount > 0);
  const totalDataValues = selectedIndicators.reduce((sum, indicator) => sum + indicator.valueCount, 0);
  
  // Separate indicators with and without data
  const indicatorsWithData = selectedIndicators.filter(indicator => indicator.valueCount > 0);
  const indicatorsWithoutData = selectedIndicators.filter(indicator => indicator.valueCount === 0);
  const hasMixedData = indicatorsWithData.length > 0 && indicatorsWithoutData.length > 0;

  return (
    <>
      <Paper sx={{ p: 2, mb: 2, display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
        <Typography variant="body2" sx={{ mr: 2 }}>
          {selectedIndicators.length} indicator(s) selected
        </Typography>
        
        <FormControl size="small" sx={{ minWidth: 200 }}>
          <InputLabel>Assign to Subarea</InputLabel>
          <Select
            value={selectedSubarea}
            onChange={(e) => setSelectedSubarea(e.target.value)}
            label="Assign to Subarea"
          >
            <MenuItem value="">Select Subarea</MenuItem>
            {subareas.map(subarea => (
              <MenuItem key={subarea.id} value={subarea.id}>
                {subarea.name}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
        
        <Button
          variant="outlined"
          size="small"
          onClick={handleAssignSubarea}
          disabled={!selectedSubarea}
        >
          Assign
        </Button>
        
        <Box>
          <Typography variant="body2" color="text.secondary" gutterBottom>
            Set Direction
          </Typography>
          <ToggleButtonGroup
            size="small"
            value={selectedDirection}
            exclusive
            onChange={(e, value) => setSelectedDirection(value)}
          >
            <ToggleButton value="input">Input</ToggleButton>
            <ToggleButton value="output">Output</ToggleButton>
          </ToggleButtonGroup>
        </Box>
        
        <Button
          variant="outlined"
          size="small"
          onClick={handleSetDirection}
          disabled={!selectedDirection}
        >
          Set
        </Button>
        
        <Button
          variant="outlined"
          color="error"
          size="small"
          startIcon={<DeleteIcon />}
          onClick={() => setShowDeleteConfirm(true)}
          sx={{ ml: 'auto' }}
        >
          Delete Selected
        </Button>
      </Paper>

      {/* Delete confirmation dialog */}
      <Dialog open={showDeleteConfirm} onClose={() => setShowDeleteConfirm(false)}>
        <DialogTitle>Confirm Deletion</DialogTitle>
        <DialogContent>
          <Alert severity="warning" sx={{ mb: 2 }}>
            This action cannot be undone.
          </Alert>
          <Typography>
            Are you sure you want to delete {selectedIndicators.length} indicator(s)?
          </Typography>
          
          {hasMixedData && (
            <Alert severity="info" sx={{ mt: 2 }}>
              <Typography variant="body2">
                You have selected indicators with mixed data:
              </Typography>
              <Typography variant="body2" sx={{ mt: 1 }}>
                • {indicatorsWithData.length} indicator(s) with {indicatorsWithData.reduce((sum, i) => sum + i.valueCount, 0)} data values
              </Typography>
              <Typography variant="body2">
                • {indicatorsWithoutData.length} indicator(s) without data
              </Typography>
            </Alert>
          )}
          
          {hasRelatedData && !hasMixedData && (
            <Alert severity="info" sx={{ mt: 2 }}>
              <Typography variant="body2">
                {totalDataValues} associated data values will also be deleted.
              </Typography>
            </Alert>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowDeleteConfirm(false)}>Cancel</Button>
          
          {hasMixedData ? (
            <>
              <Button 
                onClick={handleDeleteOnlyWithoutData} 
                color="error" 
                variant="outlined"
              >
                Delete Only Without Data ({indicatorsWithoutData.length})
              </Button>
              <Button 
                onClick={handleDeleteWithDataConfirm} 
                color="error" 
                variant="contained"
              >
                Delete All ({selectedIndicators.length})
              </Button>
            </>
          ) : hasRelatedData ? (
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