import React from 'react';
import { ResponsiveContainer, BarChart, LineChart, Bar, Line, XAxis, YAxis, Tooltip, CartesianGrid, Legend } from 'recharts';
import { Typography } from '@mui/material';
import { formatNumber } from '../../utils/formatNumber';

interface IndividualIndicatorChartProps {
  data: any[];
  chartType?: 'bar' | 'line';
  xAxisFormatter?: (label: string) => string;
  aggregationType?: string; // For legend/tooltip
  height?: number;
}

const IndividualIndicatorChart: React.FC<IndividualIndicatorChartProps> = ({ 
  data, 
  chartType = 'bar', 
  xAxisFormatter,
  aggregationType,
  height = 260
}) => {
  if (!data || data.length === 0) {
    return (
      <Typography variant="body2" color="text.secondary" sx={{ p: 2, textAlign: 'center' }}>
        No data available for this chart.
      </Typography>
    );
  }

  // Legend label
  const legendLabel = aggregationType && aggregationType !== 'sum'
    ? `Value (${aggregationType.charAt(0).toUpperCase() + aggregationType.slice(1)})`
    : 'Value';

  return (
    <ResponsiveContainer width="100%" height={height}>
      {chartType === 'bar' ? (
        <BarChart data={data} margin={{ top: 16, right: 16, left: 0, bottom: 16 }} aria-label="Bar chart">
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="label" tickFormatter={xAxisFormatter} />
          <YAxis tickFormatter={formatNumber} />
          <Tooltip 
            labelFormatter={xAxisFormatter}
            formatter={(value: number) => formatNumber(value)}
            contentStyle={{ fontSize: 14 }}
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
            labelFormatter={xAxisFormatter}
            formatter={(value: number) => formatNumber(value)}
            contentStyle={{ fontSize: 14 }}
          />
          <Legend formatter={() => legendLabel} />
          <Line type="monotone" dataKey="value" stroke="#1976d2" dot={false} name={legendLabel} />
        </LineChart>
      )}
    </ResponsiveContainer>
  );
};

export default IndividualIndicatorChart; 