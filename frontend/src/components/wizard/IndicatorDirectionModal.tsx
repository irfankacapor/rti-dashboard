'use client';
import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  FormControl,
  Select,
  MenuItem,
  Typography,
  Box,
  Alert,
  CircularProgress,
} from '@mui/material';
import { ManagedIndicator } from '@/types/indicators';

interface IndicatorSubareaDirection {
  subareaId: number;
  subareaName: string;
  direction: string;
  valueCount: number;
}

interface IndicatorDirectionModalProps {
  open: boolean;
  onClose: () => void;
  indicator: ManagedIndicator | null;
}

const API_BASE = process.env.NEXT_PUBLIC_API_URL;

export const IndicatorDirectionModal: React.FC<IndicatorDirectionModalProps> = ({
  open,
  onClose,
  indicator,
}) => {
  const [subareaDirections, setSubareaDirections] = useState<IndicatorSubareaDirection[]>([]);
  const [indicatorTypes, setIndicatorTypes] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    if (open && indicator) {
      fetchSubareaDirections();
      fetchIndicatorTypes();
    }
  }, [open, indicator]);

  const fetchSubareaDirections = async () => {
    if (!indicator) return;
    
    setLoading(true);
    setError(null);
    
    try {
      const response = await fetch(`${API_BASE}/indicators/${indicator.id}/subarea-directions`);
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      const data = await response.json();
      setSubareaDirections(data);
    } catch (err) {
      console.error('Failed to fetch subarea directions:', err);
      setError('Failed to load subarea directions');
    } finally {
      setLoading(false);
    }
  };

  const fetchIndicatorTypes = async () => {
    try {
      const response = await fetch(`${API_BASE}/indicator-types`);
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      const data = await response.json();
      setIndicatorTypes(data);
    } catch (err) {
      console.error('Failed to fetch indicator types:', err);
      setError('Failed to load indicator types');
    }
  };

  const handleDirectionChange = async (subareaId: number, newDirection: string) => {
    if (!indicator) return;
    
    setSaving(true);
    setError(null);
    setSuccess(null);
    
    try {
      const response = await fetch(`${API_BASE}/indicators/${indicator.id}/subareas/${subareaId}/direction`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ direction: newDirection }),
      });
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      
      // Update local state
      setSubareaDirections(prev => 
        prev.map(item => 
          item.subareaId === subareaId 
            ? { ...item, direction: newDirection }
            : item
        )
      );
      
      setSuccess(`Direction updated successfully for ${subareaDirections.find(s => s.subareaId === subareaId)?.subareaName}`);
      
      // Clear success message after 3 seconds
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      console.error('Failed to update direction:', err);
      setError('Failed to update direction');
    } finally {
      setSaving(false);
    }
  };

  const handleClose = () => {
    setError(null);
    setSuccess(null);
    onClose();
  };

  if (!indicator) return null;

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Typography variant="h6" component="div">
          Manage Indicator Types: {indicator.name}
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Set the indicator type (input/output) for each subarea where this indicator has data.
        </Typography>
      </DialogTitle>
      
      <DialogContent>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}
        
        {success && (
          <Alert severity="success" sx={{ mb: 2 }}>
            {success}
          </Alert>
        )}
        
        {loading ? (
          <Box display="flex" justifyContent="center" p={3}>
            <CircularProgress />
          </Box>
        ) : subareaDirections.length === 0 ? (
          <Alert severity="info">
            This indicator is not assigned to any subareas with data.
          </Alert>
        ) : (
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell sx={{ fontWeight: 'bold' }}>Subarea</TableCell>
                  <TableCell sx={{ fontWeight: 'bold' }}>Number of Values</TableCell>
                  <TableCell sx={{ fontWeight: 'bold' }}>Type</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {subareaDirections.map((subarea) => (
                  <TableRow key={subarea.subareaId}>
                    <TableCell>
                      <Typography variant="body2" fontWeight="medium">
                        {subarea.subareaName}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2">
                        {subarea.valueCount.toLocaleString()}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <FormControl size="small" sx={{ minWidth: 120 }}>
                        <Select
                          value={subarea.direction || ''}
                          onChange={(e) => handleDirectionChange(subarea.subareaId, e.target.value)}
                          disabled={saving}
                        >
                          {indicatorTypes.map((type) => (
                            <MenuItem key={type.toLowerCase()} value={type.toLowerCase()}>
                              <Box display="flex" alignItems="center" gap={1}>
                                <Box
                                  sx={{
                                    width: 12,
                                    height: 12,
                                    borderRadius: '50%',
                                    backgroundColor: type.toLowerCase() === 'input' ? '#4caf50' : '#2196f3'
                                  }}
                                />
                                {type.charAt(0) + type.slice(1).toLowerCase()}
                              </Box>
                            </MenuItem>
                          ))}
                        </Select>
                      </FormControl>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </DialogContent>
      
      <DialogActions>
        <Button onClick={handleClose} disabled={saving}>
          Close
        </Button>
      </DialogActions>
    </Dialog>
  );
}; 