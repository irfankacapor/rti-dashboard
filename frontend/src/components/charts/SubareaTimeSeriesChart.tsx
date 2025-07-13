import React, { useMemo } from 'react';
import { Box, Typography, Alert } from '@mui/material';
import TimeSeriesChart from './TimeSeriesChart';
import { IndicatorTimeSeriesDataPoint, TimeSeriesDataPoint } from '@/types/subareas';
import { Indicator } from '@/types/indicators';

interface SubareaTimeSeriesChartProps {
  timeSeriesData: TimeSeriesDataPoint[];
  indicators: Indicator[];
  indicatorTimeSeriesData: Record<string, IndicatorTimeSeriesDataPoint[]>;
}

function toSafeKey(name: string) {
  return name.replace(/[^a-zA-Z0-9_]/g, '_');
}

const SubareaTimeSeriesChart: React.FC<SubareaTimeSeriesChartProps> = ({ 
  timeSeriesData, 
  indicators,
  indicatorTimeSeriesData
}) => {
  // Use original indicator names for chart keys
  const allIndicatorNames = useMemo(() => indicators.map(ind => ind.name), [indicators]);

  const transformedTimeSeriesData = useMemo(() => {
    if (indicatorTimeSeriesData && Object.keys(indicatorTimeSeriesData).length > 0) {
      // Get all unique years
      const allYears = new Set<string>();
      Object.values(indicatorTimeSeriesData).forEach(series => {
        series.forEach(point => allYears.add(point.year));
      });
      // Transform to the expected format with original names
      return Array.from(allYears).sort().map(year => {
        const yearData: any = { year, indicators: {} };
        allIndicatorNames.forEach(name => {
          const indicator = indicators.find(ind => ind.name === name);
          const indicatorId = indicator?.id?.toString();
          if (indicatorId) {
            const dataPoint = indicatorTimeSeriesData[indicatorId]?.find(point => point.year === year);
            yearData.indicators[name] = dataPoint ? dataPoint.value : null;
          } else {
            yearData.indicators[name] = null;
          }
        });
        return yearData;
      });
    }
    return timeSeriesData || [];
  }, [indicatorTimeSeriesData, indicators, timeSeriesData, allIndicatorNames]);

  // Check if any indicators have time dimension data
  const hasTimeData = useMemo(() => {
    return transformedTimeSeriesData && transformedTimeSeriesData.length > 0;
  }, [transformedTimeSeriesData]);

  // Check for scale differences between indicators
  const scaleWarning = useMemo(() => {
    if (!transformedTimeSeriesData || transformedTimeSeriesData.length === 0) return false;
    
    const allValues: number[] = [];
    transformedTimeSeriesData.forEach(yearData => {
      if (yearData.indicators) {
        Object.values(yearData.indicators).forEach(value => {
          if (typeof value === 'number' && value > 0) {
            allValues.push(value);
          }
        });
      }
    });
    
    if (allValues.length < 2) return false;
    
    const minValue = Math.min(...allValues);
    const maxValue = Math.max(...allValues);
    const scaleRatio = maxValue / minValue;
    
    return scaleRatio > 1000; // Show warning if scale ratio exceeds 1000x
  }, [transformedTimeSeriesData]);

  if (!hasTimeData) {
    return (
      <Box sx={{ p: 2, textAlign: 'center' }}>
        <Typography variant="body1" color="text.secondary">
          No time series data available for indicators in this subarea
        </Typography>
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h6" sx={{ mb: 2 }}>
        Indicator Trends Over Time
      </Typography>
      
      {scaleWarning && (
        <Alert severity="info" sx={{ mb: 2 }}>
          Note: Indicators have different scales. Consider the units when comparing values.
        </Alert>
      )}
      
      <TimeSeriesChart 
        data={transformedTimeSeriesData}
        chartType="bar"
        isGrouped={true}
        xAxisFormatter={(label) => label}
      />
    </Box>
  );
};

export default SubareaTimeSeriesChart; 