import React from 'react';
import { Box, Typography, CircularProgress } from '@mui/material';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts';
import { formatNumber } from '../../utils/formatNumber';

interface SubareaAggregatedChartProps {
  data: any;
  loading: boolean;
  error: string | null;
  dimensionLabel: string;
  onBarHover?: (dimensionValue: string | null) => void;
  highlightedBar?: string | null;
  filteredDimensionValues?: string[] | null;
}

// Formatter to clean up keys like '2023-null' to '2023'
function cleanTimeLabel(label: string) {
  const match = String(label).match(/^(\d{4})(?:-null)?$/);
  if (match) return match[1];
  return label;
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

  // Fix time label: if only one year, show just that year; if multiple, show minYear-maxYear
  let displayLabel = dimensionLabel;
  if (data?.dimension?.toLowerCase() === 'time' && chartData.length > 0) {
    // Extract years from keys like '2023', '2023-01', '2023-null', etc.
    const years = chartData.map(d => {
      const match = String(d.name).match(/^(\d{4})/);
      return match ? parseInt(match[1], 10) : null;
    }).filter(y => y !== null);
    if (years.length === 1) {
      displayLabel = years[0].toString();
    } else if (years.length > 1) {
      const minYear = Math.min(...years);
      const maxYear = Math.max(...years);
      displayLabel = `${minYear}-${maxYear}`;
    }
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
        <Typography variant="h6">Aggregated Performance by {displayLabel}</Typography>
      </Box>
      <ResponsiveContainer width="100%" height={250}>
        <BarChart data={chartData}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="name" tickFormatter={cleanTimeLabel} />
          <YAxis tickFormatter={formatNumber} />
          <Tooltip formatter={(value, name, props) => formatNumber(value as number)} labelFormatter={cleanTimeLabel} />
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