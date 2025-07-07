import { IndicatorDataPoint, IndicatorChartData } from '@/types/indicators';

export interface ChartDataProcessorOptions {
  selectedDimension: string;
  defaultDimension: string;
  data: any;
}

export function processChartData(options: ChartDataProcessorOptions): IndicatorChartData[] {
  const { selectedDimension, defaultDimension, data } = options;
  
  if (!data) return [];

  // If we have pre-processed chart data, use it
  if (data.chartData) {
    return data.chartData;
  }

  // If selected dimension is default and we have raw data points, aggregate them
  if (
    selectedDimension === defaultDimension &&
    data.originalDataPoints &&
    data.originalDataPoints.length > 0
  ) {
    return aggregateDataPointsByTimestamp(data.originalDataPoints);
  }

  // If we have dimension-specific data, convert it to chart format
  if (selectedDimension && data[selectedDimension]) {
    return convertDimensionDataToChartFormat(data[selectedDimension]);
  }

  // Fallback to time series data
  if (data.timeSeries && data.timeSeries.length === 1) {
    return data.timeSeries;
  }

  return [];
}

function aggregateDataPointsByTimestamp(dataPoints: IndicatorDataPoint[]): IndicatorChartData[] {
  const grouped: Record<string, number[]> = {};
  
  dataPoints.forEach((point) => {
    const key = point.timestamp || 'Unknown';
    if (!grouped[key]) grouped[key] = [];
    grouped[key].push(point.value);
  });

  return Object.entries(grouped).map(([label, values]) => ({
    label,
    value: values.reduce((a, b) => a + b, 0) / values.length, // average
  }));
}

function convertDimensionDataToChartFormat(dimensionData: any): IndicatorChartData[] {
  if (typeof dimensionData === 'object' && !Array.isArray(dimensionData)) {
    return Object.entries(dimensionData).map(([key, value]) => ({ 
      label: key, 
      value: value as number 
    }));
  }
  
  if (Array.isArray(dimensionData)) {
    if (dimensionData.length > 0 && 
        typeof dimensionData[0] === 'object' && 
        'label' in dimensionData[0] && 
        'value' in dimensionData[0]) {
      return dimensionData;
    }
    return dimensionData.map((v: any, idx: number) => ({ 
      label: String(idx), 
      value: v 
    }));
  }

  return [];
}

export function determineTimeGranularity(chartData: IndicatorChartData[]): 'year' | 'month' | 'other' {
  if (chartData.length === 0) return 'other';
  
  const allLabels = chartData.map(d => d.label || d.name || '');
  
  if (allLabels.every(l => /^\d{4}$/.test(l))) {
    return 'year';
  }
  
  if (allLabels.every(l => /^\d{4}-\d{2}$/.test(l))) {
    return 'month';
  }
  
  return 'other';
}

export function buildTimeRangeOptions(granularity: 'year' | 'month' | 'other'): Array<{ label: string; value: string }> {
  switch (granularity) {
    case 'year':
      return [{ label: '1Y', value: '1Y' }];
    case 'month':
      return [
        { label: '1M', value: '1M' },
        { label: '3M', value: '3M' },
        { label: '6M', value: '6M' },
        { label: '1Y', value: '1Y' }
      ];
    default:
      return [];
  }
}

export function generateDisplayLabel(selectedDimension: string, chartData: IndicatorChartData[]): string {
  let displayLabel = selectedDimension.charAt(0).toUpperCase() + selectedDimension.slice(1);
  
  if (selectedDimension === 'time' && chartData.length > 0) {
    const years = chartData
      .map(d => {
        const match = String(d.label || d.name || '').match(/^(\d{4})/);
        return match ? parseInt(match[1], 10) : null;
      })
      .filter((y): y is number => y !== null);
    
    if (years.length === 1) {
      displayLabel = years[0].toString();
    } else if (years.length > 1) {
      const minYear = Math.min(...years);
      const maxYear = Math.max(...years);
      displayLabel = `${minYear}-${maxYear}`;
    }
  }
  
  return displayLabel;
}

export function cleanTimeLabel(label: string): string {
  if (!label) return '';
  const match = String(label).match(/^(\d{4})(?:-null)?$/);
  return match ? match[1] : label;
} 