import React from 'react';
import { ResponsiveContainer, BarChart, LineChart, Bar, Line, XAxis, YAxis, Tooltip, CartesianGrid, Legend } from 'recharts';

interface TimeSeriesChartProps {
  data: any[];
  chartType?: 'bar' | 'line';
}

const getColor = (value: number) => {
  if (value > 0) return '#4caf50'; // green
  if (value < 0) return '#f44336'; // red
  return '#ff9800'; // orange
};

const TimeSeriesChart: React.FC<TimeSeriesChartProps> = ({ data, chartType = 'bar' }) => {
  if (!data || data.length === 0) return null;
  return (
    <ResponsiveContainer width="100%" height={250}>
      {chartType === 'bar' ? (
        <BarChart data={data} margin={{ top: 16, right: 16, left: 0, bottom: 16 }}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="label" />
          <YAxis />
          <Tooltip />
          <Legend />
          <Bar dataKey="value" fill="#1976d2" isAnimationActive={false} />
        </BarChart>
      ) : (
        <LineChart data={data} margin={{ top: 16, right: 16, left: 0, bottom: 16 }}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="label" />
          <YAxis />
          <Tooltip />
          <Legend />
          <Line type="monotone" dataKey="value" stroke="#1976d2" dot={false} />
        </LineChart>
      )}
    </ResponsiveContainer>
  );
};

export default TimeSeriesChart; 