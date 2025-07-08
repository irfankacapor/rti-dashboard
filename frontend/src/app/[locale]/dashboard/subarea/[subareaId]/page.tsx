"use client";
import React, { useMemo, useState } from 'react';
import { useParams } from 'next/navigation';
import { Box, Typography, CircularProgress, Divider, Container, Paper, ButtonGroup, Button, MenuItem, Select, FormControl, InputLabel, Checkbox, FormControlLabel } from '@mui/material';
import TimeSeriesChart from '@/components/charts/TimeSeriesChart';
import SubareaAggregatedChart from '@/components/charts/SubareaAggregatedChart';
import IndicatorListItem from '@/components/IndicatorListItem';
import { useSubareaData, useSubareaAggregatedValue, useSubareaAggregatedByTime, useSubareaAggregatedByLocation, useSubareaAggregatedByDimension, useMultipleIndicatorDimensionValues } from '@/hooks/useApi';
import { useDashboardWithRelationships } from '@/hooks/useDashboardWithRelationships';
import { GoalsSidebar } from '@/components/dashboard';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import Link from 'next/link';

export default function SubareaDetailPage() {
  const params = useParams();
  const subareaId = params?.subareaId as string;
  const locale = params?.locale as string || 'en';
  const { indicators, subarea, loading, error } = useSubareaData(subareaId);
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

  // Highlight and filter state
  const [hoveredDimensionValue, setHoveredDimensionValue] = useState<string | null>(null);
  const [showOnlyCommonValues, setShowOnlyCommonValues] = useState(false);

  // Get indicator IDs for this subarea
  const indicatorIds = indicators.map((indicator: any) => indicator.id);

  // Fetch dimension values for all indicators
  const { data: dimensionValuesData, loading: dimensionValuesLoading } = useMultipleIndicatorDimensionValues(indicatorIds);

  // Compute all dimension values for the selected dimension for each indicator
  const dimensionValueSets = useMemo(() => {
    return indicators.map((indicator: any, index: number) => {
      const dimensionData = dimensionValuesData[index];
      if (!dimensionData || !dimensionData.availableDimensions) return new Set<string>();
      // Find the dimension info object for the selected dimension
      const dimInfo = dimensionData.availableDimensions.find((dim: any) => dim.type === selectedDimension);
      if (!dimInfo || !dimInfo.values) return new Set<string>();
      return new Set<string>(dimInfo.values);
    });
  }, [indicators, dimensionValuesData, selectedDimension]);

  // Compute common dimension values (intersection)
  const commonDimensionValues = useMemo(() => {
    if (dimensionValueSets.length === 0) return [] as string[];
    let intersection = new Set<string>(dimensionValueSets[0] as Set<string>);
    for (let i = 1; i < dimensionValueSets.length; i++) {
      intersection = new Set<string>([...intersection].filter(x => (dimensionValueSets[i] as Set<string>).has(x)));
    }
    return Array.from(intersection) as string[];
  }, [dimensionValueSets]);

  // Compute all dimension values (union)
  const allDimensionValues = useMemo(() => {
    const union = new Set<string>();
    dimensionValueSets.forEach(set => (set as Set<string>).forEach(v => union.add(v)));
    return Array.from(union) as string[];
  }, [dimensionValueSets]);

  // Update selectedDimension if availableDimensions changes
  React.useEffect(() => {
    if (availableDimensions.length > 0 && !availableDimensions.includes(selectedDimension)) {
      setSelectedDimension(availableDimensions[0]);
    }
  }, [availableDimensions, selectedDimension]);

  const { data: aggregatedData, loading: aggregatedLoading, error: aggregatedError } = useSubareaAggregatedByDimension(subareaId, selectedDimension);
  const { data: totalAggregatedValue, loading: totalAggregatedLoading } = useSubareaAggregatedValue(subareaId);

  // Filtered dimension values for chart
  const filteredDimensionValues = useMemo(() => {
    if (!showOnlyCommonValues || !aggregatedData?.data) {
      return null; // Show all data from backend
    }
    
    // Get dimension values that exist in the aggregated data
    const availableDimensionValues = Object.keys(aggregatedData.data);
    
    // Filter common values to only include those that have aggregated data
    const filteredCommonValues = commonDimensionValues.filter(value => 
      availableDimensionValues.includes(value)
    );
    
    return filteredCommonValues.length > 0 ? filteredCommonValues : null;
  }, [showOnlyCommonValues, aggregatedData, commonDimensionValues]);

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
  }, [subarea]);

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      {/* Sidebar with linked goals */}
      {filteredGoals.length > 0 && (
        <Box sx={{ width: 320, borderRight: '1px solid #eee', bgcolor: '#fafbfc', p: 2 }}>
          <Typography variant="h6" sx={{ mb: 2 }}>Goals</Typography>
          <GoalsSidebar goals={filteredGoals} goalGroups={filteredGoalGroups} highlightedGoals={[]} onGoalHover={() => {}} onGoalLeave={() => {}} />
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
              {/* Checkbox for filtering common dimension values */}
              <FormControlLabel
                control={
                  <Checkbox
                    checked={showOnlyCommonValues}
                    onChange={e => setShowOnlyCommonValues(e.target.checked)}
                    color="primary"
                  />
                }
                label="Show only common dimension values"
              />
            </Box>
          </Box>

          {/* Subarea aggregated chart */}
          <SubareaAggregatedChart
            data={aggregatedData}
            loading={aggregatedLoading}
            error={aggregatedError}
            dimensionLabel={selectedDimension ? selectedDimension.charAt(0).toUpperCase() + selectedDimension.slice(1) : ''}
            onBarHover={setHoveredDimensionValue}
            highlightedBar={hoveredDimensionValue}
            filteredDimensionValues={filteredDimensionValues}
          />

          <Divider sx={{ my: 2 }} />

          {totalAggregatedLoading ? (
            <CircularProgress size={24} />
          ) : (
            <Typography variant="h5" color="primary" gutterBottom>
              Total Aggregated Value: {totalAggregatedValue?.aggregatedValue ? totalAggregatedValue.aggregatedValue.toFixed(2) : '--'}
            </Typography>
          )}
          <Divider sx={{ my: 2 }} />
          {loading || dimensionValuesLoading ? (
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
                  indicators.map((indicator: any, index: number) => {
                    const dimensionData = dimensionValuesData[index];
                    // For highlighting, check if the selected dimension's values include the hovered value
                    let hasDimensionValue = false;
                    if (dimensionData && dimensionData.availableDimensions) {
                      const dimInfo = dimensionData.availableDimensions.find((dim: any) => dim.type === selectedDimension);
                      hasDimensionValue = !!(dimInfo && dimInfo.values && dimInfo.values.includes(hoveredDimensionValue));
                    }
                    return (
                      <IndicatorListItem
                        key={indicator.id}
                        indicator={indicator}
                        isAggregated={(indicator.dimensions || []).includes(selectedDimension)}
                        highlightedDimensionValue={hoveredDimensionValue}
                        selectedDimension={selectedDimension}
                        hasHighlightedDimensionValue={hasDimensionValue}
                      />
                    );
                  })
                )}
              </Box>
            </>
          )}
        </Paper>
      </Container>
    </Box>
  );
} 