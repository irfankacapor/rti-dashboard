'use client';
import React, { useEffect, useState } from 'react';
import {
  Box,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
  FormControl,
  Select,
  MenuItem,
  Chip,
  Button,
  Alert,
} from '@mui/material';
import {
  Info as InfoIcon,
  CheckCircle as CheckIcon,
  Warning as WarningIcon
} from '@mui/icons-material';
import { ProcessedIndicator } from '@/types/csvProcessing';
import { Subarea } from '@/types/subareas';
import { indicatorManagementService } from '@/services/indicatorManagementService';

interface IndicatorAssignmentProps {
  indicators: ProcessedIndicator[];
  subareas: Subarea[];
  onAssign: (indicatorId: string, field: 'subareaId' | 'direction', value: string) => void;
  onSubmit: () => void;
  isLoading?: boolean;
}

export const IndicatorAssignment: React.FC<IndicatorAssignmentProps> = ({
  indicators,
  subareas,
  onAssign,
  onSubmit,
  isLoading = false
}) => {
  const [indicatorTypes, setIndicatorTypes] = useState<string[]>([]);
  const [loadingTypes, setLoadingTypes] = useState(false);
  const [typesError, setTypesError] = useState<string | null>(null);

  const formatUnitDisplay = (indicator: ProcessedIndicator) => {
    return indicator.unit || '';
  };

  useEffect(() => {
    setLoadingTypes(true);
    indicatorManagementService.getIndicatorTypes()
      .then(setIndicatorTypes)
      .catch(() => setTypesError('Failed to load indicator types'))
      .finally(() => setLoadingTypes(false));
  }, []);

  const getAssignmentStatus = (indicator: ProcessedIndicator) => {
    if (indicator.subareaId && indicator.direction) {
      return { status: 'complete', icon: <CheckIcon color="success" />, text: 'Complete' };
    } else if (indicator.subareaId || indicator.direction) {
      return { status: 'partial', icon: <WarningIcon color="warning" />, text: 'Partial' };
    } else {
      return { status: 'incomplete', icon: <InfoIcon color="info" />, text: 'Incomplete' };
    }
  };

  const getCompletenessStats = () => {
    const total = indicators.length;
    const complete = indicators.filter(i => i.subareaId && i.direction).length;
    const partial = indicators.filter(i => (i.subareaId || i.direction) && !(i.subareaId && i.direction)).length;
    const incomplete = total - complete - partial;

    return { total, complete, partial, incomplete };
  };

  const stats = getCompletenessStats();
  const canSubmit = stats.complete === stats.total && stats.total > 0;

  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        Assign Indicators to Subareas
      </Typography>
      
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Assign each processed indicator to a subarea and specify whether it's an input or output indicator.
      </Typography>

      {/* Assignment Statistics */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Typography variant="subtitle1" fontWeight="medium">
            Assignment Progress
          </Typography>
          <Box display="flex" gap={2}>
            <Chip 
              label={`${stats.complete}/${stats.total} Complete`}
              color="success"
              size="small"
            />
            {stats.partial > 0 && (
              <Chip 
                label={`${stats.partial} Partial`}
                color="warning"
                size="small"
              />
            )}
            {stats.incomplete > 0 && (
              <Chip 
                label={`${stats.incomplete} Incomplete`}
                color="info"
                size="small"
              />
            )}
          </Box>
        </Box>
      </Paper>

      {indicators.length === 0 ? (
        <Alert severity="info">
          No indicators available for assignment. Please complete the dimension mapping first.
        </Alert>
      ) : (
        <>
          <Paper>
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>Status</TableCell>
                    <TableCell sx={{ fontWeight: 'bold' }}>Indicator Name</TableCell>
                    <TableCell sx={{ fontWeight: 'bold' }}>Dimensions</TableCell>
                    <TableCell sx={{ fontWeight: 'bold' }}>Values</TableCell>
                    <TableCell sx={{ fontWeight: 'bold' }}>Assign to Subarea</TableCell>
                    <TableCell sx={{ fontWeight: 'bold' }}>Type</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {indicators.map((indicator) => {
                    const assignmentStatus = getAssignmentStatus(indicator);
                    
                    return (
                      <TableRow key={indicator.id}>
                        <TableCell>
                          <Box display="flex" alignItems="center" gap={1}>
                            {assignmentStatus.icon}
                            <Typography variant="body2" color="text.secondary">
                              {assignmentStatus.text}
                            </Typography>
                          </Box>
                        </TableCell>
                        
                        <TableCell>
                          <Box>
                            <Typography variant="body2" fontWeight="medium">
                              {indicator.name}
                            </Typography>
                            {indicator.unit && (
                              <Typography variant="caption" color="text.secondary">
                                Unit: {formatUnitDisplay(indicator)}
                              </Typography>
                            )}
                            {indicator.source && (
                              <Typography variant="caption" color="text.secondary" display="block">
                                Source: {indicator.source}
                              </Typography>
                            )}
                          </Box>
                        </TableCell>
                        
                        <TableCell>
                          <Box display="flex" gap={0.5} flexWrap="wrap">
                            {indicator.dimensions.map((dimension) => (
                              <Chip
                                key={dimension.displayName}
                                label={dimension.displayName}
                                size="small"
                                variant="outlined"
                              />
                            ))}
                          </Box>
                        </TableCell>
                        
                        <TableCell>
                          <Typography variant="body2">
                            {indicator.valueCount.toLocaleString()} values
                          </Typography>
                        </TableCell>
                        
                        <TableCell>
                          <FormControl fullWidth size="small">
                            <Select
                              value={indicator.subareaId || ''}
                              onChange={(e) => onAssign(indicator.id, 'subareaId', e.target.value)}
                              displayEmpty
                            >
                              <MenuItem value="" disabled>
                                Select subarea
                              </MenuItem>
                              {subareas.map((subarea) => (
                                <MenuItem key={subarea.id} value={subarea.id}>
                                  <Box>
                                    <Typography variant="body2">
                                      {subarea.name}
                                    </Typography>
                                    <Typography variant="caption" color="text.secondary">
                                      {subarea.areaName}
                                    </Typography>
                                  </Box>
                                </MenuItem>
                              ))}
                            </Select>
                          </FormControl>
                        </TableCell>
                        
                        <TableCell>
                          <FormControl fullWidth size="small">
                            <Select
                              value={indicator.direction || ''}
                              onChange={(e) => onAssign(indicator.id, 'direction', e.target.value)}
                              displayEmpty
                              disabled={loadingTypes || !!typesError}
                            >
                              <MenuItem value="" disabled>
                                {loadingTypes ? 'Loading types...' : typesError ? 'Error loading types' : 'Select type'}
                              </MenuItem>
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
                    );
                  })}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>

          <Box mt={3} display="flex" justifyContent="space-between" alignItems="center">
            <Box>
              <Typography variant="body2" color="text.secondary">
                {canSubmit 
                  ? 'All indicators are assigned and ready for submission.'
                  : 'Please complete all indicator assignments before submitting.'
                }
              </Typography>
            </Box>
            
            <Button
              variant="contained"
              size="large"
              onClick={onSubmit}
              disabled={!canSubmit || isLoading}
            >
              {isLoading ? 'Submitting...' : 'Submit Indicators'}
            </Button>
          </Box>
        </>
      )}
    </Box>
  );
}; 