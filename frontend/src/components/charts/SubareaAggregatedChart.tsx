import React from 'react';
import { Box, Typography, CircularProgress } from '@mui/material';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts';

interface SubareaAggregatedChartProps {
  data: any;
  loading: boolean;
  error: string | null;
  dimensionLabel: string;
  onBarHover?: (dimensionValue: string | null) => void;
  highlightedBar?: string | null;
  filteredDimensionValues?: string[] | null;
}

export default function SubareaAggregatedChart({ data, loading, error, dimensionLabel, onBarHover, highlightedBar, filteredDimensionValues }: SubareaAggregatedChartProps) {
  const formatData = (data: any) => {
    if (!data || !data.data) return [];
    return Object.entries(data.data).map(([key, value]) => ({
      name: key,
      value: Number(value)
    }));
  };

  let chartData = formatData(data);
  if (filteredDimensionValues && filteredDimensionValues.length > 0) {
    chartData = chartData.filter(d => filteredDimensionValues.includes(d.name));
  }

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
        <Typography variant="h6">Aggregated Performance by {dimensionLabel}</Typography>
      </Box>
      <ResponsiveContainer width="100%" height={250}>
        <BarChart data={chartData}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="name" />
          <YAxis />
          <Tooltip />
          <Bar dataKey="value" fill="#8884d8">
            {chartData.map((entry, idx) => (
              <Cell
                key={`cell-${entry.name}`}
                fill={highlightedBar === entry.name ? 'rgba(90, 47, 194, 0.65)' : '#8884d8'}
                onMouseEnter={() => onBarHover && onBarHover(entry.name)}
                onMouseLeave={() => onBarHover && onBarHover(null)}
                style={{ cursor: 'pointer' }}
              />
            ))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </Box>
  );
} 