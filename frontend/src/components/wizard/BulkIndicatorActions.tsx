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
}

export const BulkIndicatorActions: React.FC<BulkIndicatorActionsProps> = ({
  selectedIndicators,
  subareas,
  onBulkUpdate,
  onBulkDelete,
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
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowDeleteConfirm(false)}>Cancel</Button>
          <Button onClick={handleDeleteConfirm} color="error" variant="contained">
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
}; 