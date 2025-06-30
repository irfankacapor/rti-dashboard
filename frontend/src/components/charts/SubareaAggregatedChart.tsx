import React, { useState } from 'react';
import { Box, Typography, ToggleButton, ToggleButtonGroup, CircularProgress } from '@mui/material';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

interface SubareaAggregatedChartProps {
  timeData: any;
  locationData: any;
  loading: boolean;
  error: string | null;
}

export default function SubareaAggregatedChart({ timeData, locationData, loading, error }: SubareaAggregatedChartProps) {
  const [dimension, setDimension] = useState<'time' | 'location'>('time');

  const handleDimensionChange = (event: React.MouseEvent<HTMLElement>, newDimension: 'time' | 'location') => {
    if (newDimension !== null) {
      setDimension(newDimension);
    }
  };

  const formatData = (data: any) => {
    if (!data || !data.data) return [];
    
    return Object.entries(data.data).map(([key, value]) => ({
      name: key,
      value: Number(value)
    }));
  };

  const chartData = dimension === 'time' ? formatData(timeData) : formatData(locationData);

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" height={300}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" height={300}>
        <Typography color="error">{error}</Typography>
      </Box>
    );
  }

  if (chartData.length === 0) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" height={300}>
        <Typography color="text.secondary">No data available for this dimension</Typography>
      </Box>
    );
  }

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
        <Typography variant="h6">Aggregated Performance</Typography>
        <ToggleButtonGroup
          value={dimension}
          exclusive
          onChange={handleDimensionChange}
          size="small"
        >
          <ToggleButton value="time">Time</ToggleButton>
          <ToggleButton value="location">Location</ToggleButton>
        </ToggleButtonGroup>
      </Box>
      
      <ResponsiveContainer width="100%" height={250}>
        <BarChart data={chartData}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="name" />
          <YAxis />
          <Tooltip />
          <Bar dataKey="value" fill="#8884d8" />
        </BarChart>
      </ResponsiveContainer>
    </Box>
  );
} 