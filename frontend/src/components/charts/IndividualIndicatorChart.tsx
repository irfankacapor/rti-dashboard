import React from 'react';
import { ResponsiveContainer, BarChart, LineChart, Bar, Line, XAxis, YAxis, Tooltip, CartesianGrid, Legend } from 'recharts';
import { Typography, Box } from '@mui/material';
import { formatNumber } from '../../utils/formatNumber';
import { IndicatorChartData } from '../../types/indicators';

interface IndividualIndicatorChartProps {
  data: IndicatorChartData[];
  chartType?: 'bar' | 'line';
  xAxisFormatter?: (label: string) => string;
  aggregationType?: string; // For legend/tooltip
  unit?: string; // Unit for the indicator (e.g., "m²", "km²", "%")
  height?: number;
}

// Custom tooltip component for showing additional dimensions
const CustomTooltip = ({ active, payload, label, xAxisFormatter, aggregationType }: any) => {
  if (active && payload && payload.length) {
    const dataPoint = payload[0].payload as IndicatorChartData;
    const value = payload[0].value;
    
    return (
      <Box
        sx={{
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          border: '1px solid #ccc',
          borderRadius: 1,
          padding: 1,
          boxShadow: 2,
        }}
      >
        <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
          {xAxisFormatter ? xAxisFormatter(label) : label}
        </Typography>
        <Typography variant="body2">
          {aggregationType && aggregationType !== 'sum' 
            ? `${aggregationType.charAt(0).toUpperCase() + aggregationType.slice(1)}: ${formatNumber(value)}`
            : `${formatNumber(value)}`
          }
        </Typography>
        
        {/* Show additional dimensions if available */}
        {dataPoint.additionalDimensions && Object.keys(dataPoint.additionalDimensions).length > 0 && (
          <Box sx={{ mt: 1, pt: 1, borderTop: '1px solid #eee' }}>
            <Typography variant="caption" sx={{ color: 'text.secondary' }}>
              Additional dimensions:
            </Typography>
            {Object.entries(dataPoint.additionalDimensions).map(([dimKey, dimValue]) => (
              <Typography key={dimKey} variant="caption" sx={{ display: 'block' }}>
                {dimKey}: {dimValue}
              </Typography>
            ))}
          </Box>
        )}
      </Box>
    );
  }
  return null;
};

const IndividualIndicatorChart: React.FC<IndividualIndicatorChartProps> = ({ 
  data, 
  chartType = 'bar', 
  xAxisFormatter,
  aggregationType,
  unit,
  height = 260
}) => {
  if (!data || data.length === 0) {
    return (
      <Typography variant="body2" color="text.secondary" sx={{ p: 2, textAlign: 'center' }}>
        No data available for this chart.
      </Typography>
    );
  }

  // Legend label with unit
  const legendLabel = aggregationType && aggregationType !== 'sum'
    ? `${unit ? `${unit} (${aggregationType.charAt(0).toUpperCase() + aggregationType.slice(1)})` : `Value (${aggregationType.charAt(0).toUpperCase() + aggregationType.slice(1)})`}`
    : unit || 'Value';

  return (
    <ResponsiveContainer width="100%" height={height}>
      {chartType === 'bar' ? (
        <BarChart data={data} margin={{ top: 16, right: 16, left: 0, bottom: 16 }} aria-label="Bar chart">
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="label" tickFormatter={xAxisFormatter} />
          <YAxis tickFormatter={formatNumber} />
          <Tooltip 
            content={<CustomTooltip xAxisFormatter={xAxisFormatter} aggregationType={aggregationType} />}
          />
          <Legend formatter={() => legendLabel} />
          <Bar dataKey="value" fill="#1976d2" isAnimationActive={false} name={legendLabel} />
        </BarChart>
      ) : (
        <LineChart data={data} margin={{ top: 16, right: 16, left: 0, bottom: 16 }} aria-label="Line chart">
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="label" tickFormatter={xAxisFormatter} />
          <YAxis tickFormatter={formatNumber} />
          <Tooltip 
            content={<CustomTooltip xAxisFormatter={xAxisFormatter} aggregationType={aggregationType} />}
          />
          <Legend formatter={() => legendLabel} />
          <Line type="monotone" dataKey="value" stroke="#1976d2" dot={false} name={legendLabel} />
        </LineChart>
      )}
    </ResponsiveContainer>
  );
};

export default IndividualIndicatorChart; 