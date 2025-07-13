import { IndicatorDataPoint, IndicatorChartData } from '@/types/indicators';

export type AggregationType = 'sum' | 'average' | 'min' | 'max' | 'median' | 'count';

export interface ChartDataProcessorOptions {
  selectedDimension: string;
  defaultDimension: string;
  data: any;
  aggregationType?: AggregationType;
  indicatorValues?: Array<{ value: number; [key: string]: any }>; // For direct aggregation
}

export function processChartData(options: ChartDataProcessorOptions): IndicatorChartData[] {
  const { selectedDimension, defaultDimension, data, aggregationType = 'sum', indicatorValues } = options;
  
  if (indicatorValues && selectedDimension) {
    return aggregateByDimension(indicatorValues, selectedDimension, aggregationType);
  }

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

function aggregateByDimension(
  indicatorValues: Array<{ value: number; [key: string]: any }>,
  dimension: string,
  aggregationType: AggregationType
): IndicatorChartData[] {
  // Group values by dimension value
  const grouped: Record<string, number[]> = {};
  indicatorValues.forEach((item) => {
    let dimValue = '';
    if (
      ['time', 'year', 'month', 'day', 'timestamp'].includes(dimension) &&
      (item.year || item.month || item.day || item.timeValue || item.timestamp)
    ) {
      dimValue = item.year || item.month || item.day || item.timeValue || item.timestamp;
    } else if (dimension === 'location' && item.locationValue) {
      dimValue = item.locationValue;
    } else if (item[dimension] !== undefined) {
      dimValue = item[dimension];
    } else if (item.customDimensions && item.customDimensions[dimension]) {
      dimValue = item.customDimensions[dimension];
    }
    if (!dimValue) return; // Omit if no value
    if (!grouped[dimValue]) grouped[dimValue] = [];
    grouped[dimValue].push(item.value);
  });

  // Helper for median
  const median = (arr: number[]) => {
    const sorted = [...arr].sort((a, b) => a - b);
    const mid = Math.floor(sorted.length / 2);
    return sorted.length % 2 !== 0
      ? sorted[mid]
      : (sorted[mid - 1] + sorted[mid]) / 2;
  };

  // Aggregate per dimension value
  let result = Object.entries(grouped).map(([label, values]) => {
    let value: number;
    switch (aggregationType) {
      case 'sum':
        value = values.reduce((a, b) => a + b, 0);
        break;
      case 'average':
        value = values.reduce((a, b) => a + b, 0) / values.length;
        break;
      case 'min':
        value = Math.min(...values);
        break;
      case 'max':
        value = Math.max(...values);
        break;
      case 'median':
        value = median(values);
        break;
      case 'count':
        value = values.length;
        break;
      default:
        value = values.reduce((a, b) => a + b, 0);
    }
    return { label, value };
  });

  // Sort time/year/month/day labels ascending if dimension is time-based and labels are numbers
  if (['time', 'year', 'month', 'day', 'timestamp'].includes(dimension)) {
    result = result.sort((a, b) => {
      const aNum = parseInt(a.label, 10);
      const bNum = parseInt(b.label, 10);
      if (!isNaN(aNum) && !isNaN(bNum)) return aNum - bNum;
      return a.label.localeCompare(b.label);
    });
  }

  return result;
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