"use client";
import React, { useState, useMemo } from 'react';
import { useParams } from 'next/navigation';
import { Box, Typography, Paper, Grid, IconButton } from '@mui/material';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import { useSubareaData } from '@/hooks/useSubareaData';
import { useDashboardWithRelationships } from '@/hooks/useDashboardWithRelationships';
import { GoalsSidebar } from '@/components/dashboard';
import IndicatorListItem from '@/components/IndicatorListItem';
import IndividualIndicatorModal from '@/components/IndividualIndicatorModal';
import SubareaTimeSeriesChart from '@/components/charts/SubareaTimeSeriesChart';
import { Indicator } from '@/types/indicators';

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
    timeSeriesData,
    indicatorTimeSeriesData,
    loading, 
    error 
  } = useSubareaData(subareaId);
  
  const { goals, goalGroups, relationships } = useDashboardWithRelationships();

  // Get all unique dimensions from indicators
  const availableDimensions = useMemo(() => {
    const dimSet = new Set<string>();
    indicators.forEach((indicator: Indicator) => {
      (indicator.dimensions || []).forEach((dim) => dimSet.add(dim.type));
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

  const [sidebarOpen, setSidebarOpen] = useState(true);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <Typography>Loading...</Typography>
      </Box>
    );
  }

  if (error) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <Typography color="error">Error: {error}</Typography>
      </Box>
    );
  }

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
      <Box sx={{ 
        flex: 1, 
        p: 3,
        marginLeft: 'auto',
        marginRight: 'auto',
        position: 'relative',
        '@media (min-width: 600px)': {
          maxWidth: '720px',
        },
        '@media (min-width: 900px)': {
          maxWidth: '1236px',
        },
        '@media (max-width: 600px)': {
          padding: '1rem',
        },
      }}>
        {/* Page Header */}
        <Box sx={{ mb: 4 }}>
          <Typography variant="h4" component="h1" gutterBottom>
            {subarea?.name || 'Subarea'}
          </Typography>
          {subarea?.description && (
            <Typography variant="body1" color="text.secondary">
              {subarea.description}
            </Typography>
          )}
        </Box>

        {/* Time Series Section */}
        <Paper sx={{ p: 3, mb: 3 }}>
          <SubareaTimeSeriesChart 
            timeSeriesData={timeSeriesData || []}
            indicators={indicators}
            indicatorTimeSeriesData={indicatorTimeSeriesData}
          />
        </Paper>

        {/* Indicators Section */}
        <Paper sx={{ p: 3 }}>
          <Typography variant="h6" sx={{ mb: 2 }}>
            Indicators ({indicators.length})
          </Typography>
          <Box>
            {indicators.map((indicator: Indicator) => (
              <Box key={indicator.id} sx={{ mb: 2 }}>
                <IndicatorListItem
                  indicator={indicator}
                  selectedDimension={selectedDimension}
                  subareaData={{
                    subarea,
                    indicators,
                    aggregatedData,
                    totalAggregatedValue,
                    dimensionMetadata,
                    timeSeriesData,
                    indicatorTimeSeriesData,
                    errors: {}
                  }}
                />
              </Box>
            ))}
          </Box>
        </Paper>
      </Box>

      {/* Individual Indicator Modal */}
      {/* The IndividualIndicatorModal component is now rendered within IndicatorListItem */}
    </Box>
  );
} 