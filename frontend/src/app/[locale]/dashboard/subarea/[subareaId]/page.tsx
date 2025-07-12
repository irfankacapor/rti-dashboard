"use client";
import React, { useMemo, useState } from 'react';
import { useParams } from 'next/navigation';
import { Box, Typography, CircularProgress, Divider, Container, Paper, ButtonGroup, Button, MenuItem, Select, FormControl, InputLabel, IconButton } from '@mui/material';
import SubareaAggregatedChart from '@/components/charts/SubareaAggregatedChart';
import IndicatorListItem from '@/components/IndicatorListItem';
import { useSubareaData } from '@/hooks/useSubareaData';
import { useDashboardWithRelationships } from '@/hooks/useDashboardWithRelationships';
import { GoalsSidebar } from '@/components/dashboard';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import Link from 'next/link';

export default function SubareaDetailPage() {
  const params = useParams();
  const subareaId = params?.subareaId as string;
  const locale = params?.locale as string || 'en';
  
  // Use the new subarea data hook
  const { 
    subarea, 
    indicators, 
    aggregatedData, 
    totalAggregatedValue, 
    dimensionMetadata, 
    loading, 
    error 
  } = useSubareaData(subareaId);
  
  const { goals, goalGroups, relationships } = useDashboardWithRelationships();

  // Get all unique dimensions from indicators
  const availableDimensions = useMemo(() => {
    const dimSet = new Set<string>();
    indicators.forEach((indicator: any) => {
      (indicator.dimensions || []).forEach((dim: string) => dimSet.add(dim));
    });
    return Array.from(dimSet);
  }, [indicators]);

  // Default to first dimension if available
  const [selectedDimension, setSelectedDimension] = useState<string>(availableDimensions[0] || '');

  // Update selectedDimension if availableDimensions changes
  React.useEffect(() => {
    if (availableDimensions.length > 0 && !availableDimensions.includes(selectedDimension)) {
      setSelectedDimension(availableDimensions[0]);
    }
  }, [availableDimensions, selectedDimension]);

  // Get aggregated data for the selected dimension
  const selectedDimensionData = useMemo(() => {
    if (!selectedDimension || !aggregatedData[selectedDimension]) {
      return null;
    }
    return {
      data: aggregatedData[selectedDimension],
      dimension: selectedDimension
    };
  }, [selectedDimension, aggregatedData]);

  // Get goal IDs linked to this subarea from relationships
  const goalIdsForSubarea = relationships?.subareaToGoals?.[subareaId] || [];
  const filteredGoals = (goals || []).filter(goal => goalIdsForSubarea.includes(goal.id));
  const filteredGoalGroups = (goalGroups || [])
    .map(group => ({
      ...group,
      goals: group.goals.filter(goal => goalIdsForSubarea.includes(goal.id))
    }))
    .filter(group => group.goals.length > 0);

  // Debug: log subarea object
  React.useEffect(() => {
    // eslint-disable-next-line no-console
    console.log('Subarea object:', subarea);
    console.log('Subarea data:', { subarea, indicators, aggregatedData, totalAggregatedValue, dimensionMetadata });
  }, [subarea, indicators, aggregatedData, totalAggregatedValue, dimensionMetadata]);

  const [sidebarOpen, setSidebarOpen] = useState(true);

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      {/* Sidebar with linked goals */}
      {filteredGoals.length > 0 && sidebarOpen && (
        <Box sx={{ width: 320, borderRight: '1px solid #eee', bgcolor: '#fafbfc', p: 2, position: 'relative', minHeight: '100vh' }}>
          <Typography variant="h6" sx={{ mb: 2 }}>Goals</Typography>
          <GoalsSidebar goals={filteredGoals} goalGroups={filteredGoalGroups} highlightedGoals={[]} onGoalHover={() => {}} onGoalLeave={() => {}} />
          {/* Close button, vertically centered on the outer right edge of the sidebar */}
          <Box
            sx={{
              position: 'fixed',
              top: '50%',
              left: 320 - 24, // 24 = half the button size for overlap
              transform: 'translateY(-50%)',
              zIndex: 1300,
            }}
          >
            <IconButton onClick={() => setSidebarOpen(false)} size="large" title="Close sidebar" sx={{ bgcolor: '#fff', border: '1px solid #eee', boxShadow: 2 }}>
              <ChevronLeftIcon />
            </IconButton>
          </Box>
        </Box>
      )}
      {/* Sidebar open button (when closed) */}
      {filteredGoals.length > 0 && !sidebarOpen && (
        <Box
          sx={{
            position: 'fixed',
            top: '50%',
            left: 0,
            transform: 'translateY(-50%)',
            zIndex: 1300,
          }}
        >
          <IconButton onClick={() => setSidebarOpen(true)} size="large" color="primary" title="Open sidebar" sx={{ bgcolor: '#fff', border: '1px solid #eee', boxShadow: 2 }}>
            <ChevronRightIcon />
          </IconButton>
        </Box>
      )}
      {/* Main content */}
      <Container maxWidth="md" sx={{ flex: 1, py: 4 }}>
        {/* Back to dashboard button */}
        <Box mb={2}>
          <Link href={`/${locale}/dashboard`} style={{ textDecoration: 'none' }}>
            <Button startIcon={<ArrowBackIcon />} variant="outlined" color="primary">
              Back to Dashboard
            </Button>
          </Link>
        </Box>
        <Paper sx={{ p: 4, mb: 4 }}>
          <Typography variant="h4" gutterBottom>
            {subarea?.name || ''}
          </Typography>
          <Typography variant="subtitle1" color="text.secondary" gutterBottom>{subarea?.description}</Typography>
          <Divider sx={{ my: 2 }} />

          {/* Aggregated Performance heading and dimension picker */}
          <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
            <Typography variant="h6">Aggregated Performance</Typography>
            <Box display="flex" alignItems="center" gap={2}>
              {availableDimensions.length > 0 && (
                availableDimensions.length <= 4 ? (
                  <ButtonGroup variant="outlined" size="small" sx={{ bgcolor: '#f5f5f5' }}>
                    {availableDimensions.map((dim) => (
                      <Button
                        key={dim}
                        variant={selectedDimension === dim ? 'contained' : 'outlined'}
                        onClick={() => setSelectedDimension(dim)}
                        sx={{
                          bgcolor: selectedDimension === dim ? '#e0e0e0' : '#f5f5f5',
                          color: '#333',
                          borderColor: '#ccc',
                          '&:hover': { bgcolor: '#e0e0e0' }
                        }}
                      >
                        {dim.charAt(0).toUpperCase() + dim.slice(1)}
                      </Button>
                    ))}
                  </ButtonGroup>
                ) : (
                  <FormControl size="small" sx={{ minWidth: 120, bgcolor: '#f5f5f5' }}>
                    <InputLabel id="dimension-select-label">Dimension</InputLabel>
                    <Select
                      labelId="dimension-select-label"
                      value={selectedDimension}
                      label="Dimension"
                      onChange={e => setSelectedDimension(e.target.value)}
                      sx={{ bgcolor: '#f5f5f5', color: '#333', borderColor: '#ccc' }}
                    >
                      {availableDimensions.map((dim) => (
                        <MenuItem key={dim} value={dim}>
                          {dim.charAt(0).toUpperCase() + dim.slice(1)}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                )
              )}
            </Box>
          </Box>

          {/* Subarea aggregated chart */}
          <SubareaAggregatedChart
            data={selectedDimensionData}
            loading={loading}
            error={error}
            dimensionLabel={selectedDimension ? selectedDimension.charAt(0).toUpperCase() + selectedDimension.slice(1) : ''}
            onBarHover={() => {}}
            highlightedBar={null}
            filteredDimensionValues={null} // Removed filteredDimensionValues as it's not needed here
          />

          <Divider sx={{ my: 2 }} />

          {loading ? (
            <CircularProgress size={24} />
          ) : (
            <Typography variant="h5" color="primary" gutterBottom>
              Total Aggregated Value: {totalAggregatedValue ? totalAggregatedValue.toFixed(2) : '--'}
            </Typography>
          )}
          <Divider sx={{ my: 2 }} />
          {loading ? (
            <Box display="flex" justifyContent="center"><CircularProgress /></Box>
          ) : error ? (
            <Typography color="error">Failed to load subarea data.</Typography>
          ) : (
            <>
              <Typography variant="subtitle2" sx={{ mt: 2 }}>Indicators</Typography>
              <Box>
                {indicators.length === 0 ? (
                  <Typography>No indicators found for this subarea.</Typography>
                ) : (
                  indicators.map((indicator: any) => (
                    <IndicatorListItem
                      key={indicator.id}
                      indicator={indicator}
                      isAggregated={(indicator.dimensions || []).includes(selectedDimension)}
                      highlightedDimensionValue={null}
                      selectedDimension={selectedDimension}
                      hasHighlightedDimensionValue={false} // Simplified since we're not pre-fetching dimension data
                      subareaId={subareaId}
                      comprehensiveData={{ aggregatedData, dimensionMetadata }} // Pass subarea data to avoid API calls
                    />
                  ))
                )}
              </Box>
            </>
          )}
        </Paper>
      </Container>
    </Box>
  );
} 