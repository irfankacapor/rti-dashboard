import React from 'react';
import { ResponsiveContainer, BarChart, LineChart, Bar, Line, XAxis, YAxis, Tooltip, CartesianGrid, Legend } from 'recharts';

interface TimeSeriesChartProps {
  data: any[];
  chartType?: 'bar' | 'line';
  xAxisFormatter?: (label: string) => string;
  isGrouped?: boolean; // For grouped bar charts with multiple indicators
  colors?: string[]; // Colors for different indicators
}

const getColor = (value: number) => {
  if (value > 0) return '#4caf50'; // green
  if (value < 0) return '#f44336'; // red
  return '#ff9800'; // orange
};

const defaultColors = ['#1976d2', '#dc004e', '#388e3c', '#f57c00', '#7b1fa2', '#d32f2f', '#1976d2', '#388e3c'];

const TimeSeriesChart: React.FC<TimeSeriesChartProps> = ({ 
  data, 
  chartType = 'bar', 
  xAxisFormatter,
  isGrouped = false,
  colors = defaultColors
}) => {
  if (!data || data.length === 0) return null;

  // For grouped bar charts, we need to transform the data
  const chartData = isGrouped ? data : data;

  // Compute all indicator names across all years (original names)
  const allIndicatorNames = Array.from(
    new Set(
      chartData.flatMap(d => Object.keys(d.indicators || {}))
    )
  );

  console.log("chartData", chartData);
  console.log("allIndicatorNames", allIndicatorNames);

  return (
    <ResponsiveContainer width="100%" height={250}>
      {chartType === 'bar' ? (
        <BarChart data={chartData} margin={{ top: 16, right: 16, left: 0, bottom: 16 }}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="year" tickFormatter={xAxisFormatter} />
          <YAxis />
          <Tooltip 
            labelFormatter={xAxisFormatter}
            formatter={(value: any, name: string) => [value, name]}
          />
          <Legend />
          {isGrouped ? (
            // Render grouped bars for multiple indicators using original names as string literals
            allIndicatorNames.map((indicatorName, index) => (
              <Bar 
                key={indicatorName}
                dataKey={`indicators[\"${indicatorName}\"]`}
                name={indicatorName}
                fill={colors[index % colors.length]}
                isAnimationActive={false}
              />
            ))
          ) : (
            <Bar dataKey="value" fill="#1976d2" isAnimationActive={false} />
          )}
        </BarChart>
      ) : (
        <LineChart data={chartData} margin={{ top: 16, right: 16, left: 0, bottom: 16 }}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="year" tickFormatter={xAxisFormatter} />
          <YAxis />
          <Tooltip 
            labelFormatter={xAxisFormatter}
            formatter={(value: any, name: string) => [value, name]}
          />
          <Legend />
          {isGrouped ? (
            // Render multiple lines for different indicators using original names as string literals
            allIndicatorNames.map((indicatorName, index) => (
              <Line 
                key={indicatorName}
                type="monotone" 
                dataKey={`indicators[\"${indicatorName}\"]`}
                name={indicatorName}
                stroke={colors[index % colors.length]}
                dot={{ fill: colors[index % colors.length] }}
                isAnimationActive={false}
              />
            ))
          ) : (
            <Line type="monotone" dataKey="value" stroke="#1976d2" dot={false} />
          )}
        </LineChart>
      )}
    </ResponsiveContainer>
  );
};

export default TimeSeriesChart; 