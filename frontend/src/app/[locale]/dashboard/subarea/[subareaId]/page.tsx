"use client";
import React from 'react';
import { useParams } from 'next/navigation';
import { Box, Typography, CircularProgress, Divider, Container, Paper } from '@mui/material';
import TimeSeriesChart from '@/components/charts/TimeSeriesChart';
import SubareaAggregatedChart from '@/components/charts/SubareaAggregatedChart';
import IndicatorListItem from '@/components/IndicatorListItem';
import { useSubareaData, useSubareaAggregatedValue, useSubareaAggregatedByTime, useSubareaAggregatedByLocation } from '@/hooks/useApi';
import { useGoalsData } from '@/hooks/useGoalsData';
import { GoalsSidebar } from '@/components/dashboard';

export default function SubareaDetailPage() {
  const params = useParams();
  const subareaId = params?.subareaId as string;
  const { indicators, subarea, loading, error } = useSubareaData(subareaId);
  const { data: aggregatedData, loading: aggregatedLoading } = useSubareaAggregatedValue(subareaId);
  const { data: timeData, loading: timeLoading } = useSubareaAggregatedByTime(subareaId);
  const { data: locationData, loading: locationLoading } = useSubareaAggregatedByLocation(subareaId);
  const { goals, goalGroups } = useGoalsData();

  // Filter goals linked to this subarea
  const linkedGoals = (goals || []).filter(goal => goal.linkedSubareaIds?.includes(subareaId));
  const linkedGoalGroups = (goalGroups || []).map(group => ({
    ...group,
    goals: group.goals.filter(goal => goal.linkedSubareaIds?.includes(subareaId))
  })).filter(group => group.goals.length > 0);

  const chartLoading = timeLoading || locationLoading;

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      {/* Sidebar with linked goals */}
      <Box sx={{ width: 320, borderRight: '1px solid #eee', bgcolor: '#fafbfc', p: 2 }}>
        <Typography variant="h6" sx={{ mb: 2 }}>Goals</Typography>
        <GoalsSidebar goals={linkedGoals} goalGroups={linkedGoalGroups} highlightedGoals={[]} onGoalHover={() => {}} />
      </Box>
      {/* Main content */}
      <Container maxWidth="md" sx={{ flex: 1, py: 4 }}>
        <Paper sx={{ p: 4, mb: 4 }}>
          <Typography variant="h4" gutterBottom>
            {subarea?.name || 'Subarea'}
          </Typography>
          <Typography variant="subtitle1" color="text.secondary" gutterBottom>{subarea?.description}</Typography>
          <Divider sx={{ my: 2 }} />
          
          {/* Subarea aggregated chart */}
          <SubareaAggregatedChart 
            timeData={timeData}
            locationData={locationData}
            loading={chartLoading}
            error={null}
          />
          
          <Divider sx={{ my: 2 }} />
          
          {aggregatedLoading ? (
            <CircularProgress size={24} />
          ) : (
            <Typography variant="h5" color="primary" gutterBottom>
              Total Aggregated Value: {aggregatedData?.aggregatedValue ? aggregatedData.aggregatedValue.toFixed(2) : '--'}
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
                    <IndicatorListItem key={indicator.id} indicator={indicator} />
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