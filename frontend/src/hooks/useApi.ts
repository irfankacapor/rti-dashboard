import { useState, useEffect } from 'react';

const API_BASE = process.env.NEXT_PUBLIC_API_URL;

function mapIndicatorApiToFrontend(apiIndicator: any) {
  return {
    id: String(apiIndicator.id),
    name: apiIndicator.name,
    description: apiIndicator.description,
    unit: apiIndicator.unit?.code || apiIndicator.latestValueUnit || '',
    direction: apiIndicator.direction,
    valueCount: apiIndicator.valueCount,
    dimensions: apiIndicator.dimensions || [],
    subareaId: String(apiIndicator.subareaId),
    subareaName: apiIndicator.subareaName,
    dataType: apiIndicator.dataType?.code || '',
    latestValue: apiIndicator.latestValue !== null ? apiIndicator.latestValue.toFixed(2) : '--',
    aggregationMethod: apiIndicator.aggregationMethod || 'N/A',
  };
}

export function useSubareaData(subareaId: string) {
  const [indicators, setIndicators] = useState<any[]>([]);
  const [subarea, setSubarea] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!subareaId) return;
    setLoading(true);
    setError(null);
    Promise.all([
      fetch(`${API_BASE}/subareas/${subareaId}/indicators`).then(res => res.json()),
      fetch(`${API_BASE}/subareas/${subareaId}`).then(res => res.json())
    ])
      .then(([indicatorsData, subareaData]) => {
        setIndicators(Array.isArray(indicatorsData) ? indicatorsData.map(mapIndicatorApiToFrontend) : []);
        setSubarea(subareaData);
      })
      .catch(() => setError('Failed to fetch subarea data'))
      .finally(() => setLoading(false));
  }, [subareaId]);

  return { indicators, subarea, loading, error };
}

export function useIndicatorData(
  indicatorId: string,
  timeRange: string,
  dimension?: string,
  defaultDimension: string = 'time',
  availableDimensions?: string[]
) {
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!indicatorId) return;
    setLoading(true);
    setError(null);

    // Determine which dimension to use as default
    let effectiveDefault = defaultDimension;
    if (availableDimensions && availableDimensions.length > 0 && !availableDimensions.includes(defaultDimension)) {
      effectiveDefault = availableDimensions[0];
    }

    // If the selected dimension is the default (or not set), fetch raw historical data
    if (!dimension || dimension === effectiveDefault) {
      let url = `${API_BASE}/indicators/${indicatorId}/historical?range=${timeRange}`;
      fetch(url)
        .then(res => {
          if (!res.ok) throw new Error(`HTTP ${res.status}: ${res.statusText}`);
          return res.json();
        })
        .then(responseData => {
          // The new DTO returns dataPoints, startDate, endDate, etc.
          if (responseData.dataPoints && Array.isArray(responseData.dataPoints)) {
            setData({
              timeSeries: responseData.dataPoints.map((point: any) => ({
                label: point.timestamp || point.label,
                value: point.value
              })),
              dimensions: responseData.dimensions || [effectiveDefault],
              startDate: responseData.startDate,
              endDate: responseData.endDate,
              originalDataPoints: responseData.dataPoints
            });
          } else {
            setData(responseData);
          }
        })
        .catch((err) => {
          console.error('Error fetching indicator data:', err);
          setError('Failed to fetch indicator data');
        })
        .finally(() => setLoading(false));
    } else {
      // Otherwise, fetch aggregated chart data from the new endpoint
      fetch(`${API_BASE}/indicators/${indicatorId}/chart?aggregateBy=${dimension}`)
        .then(res => {
          if (!res.ok) throw new Error(`HTTP ${res.status}: ${res.statusText}`);
          return res.json();
        })
        .then((result) => {
          // The new DTO returns dataPoints as an array
          const chartData = (result.dataPoints || []).map((point: any) => ({
            label: point.label,
            value: point.value
          }));
          setData({
            chartData,
            dimension,
            availableDimensions: result.availableDimensions || [dimension],
          });
        })
        .catch((err) => {
          console.error('Error fetching indicator aggregated data:', err);
          setError('Failed to fetch indicator aggregated data');
        })
        .finally(() => setLoading(false));
    }
  }, [indicatorId, timeRange, dimension, defaultDimension, availableDimensions && availableDimensions.join(',')]);

  return { data, loading, error };
}

export function usePerformanceMetrics(areaId: string) {
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!areaId) return;
    setLoading(true);
    setError(null);
    fetch(`${API_BASE}/dashboard-data/performance-metrics/${areaId}`)
      .then(res => res.json())
      .then(setData)
      .catch(() => setError('Failed to fetch performance metrics'))
      .finally(() => setLoading(false));
  }, [areaId]);

  return { data, loading, error };
}

export function useSubareaAggregatedValue(subareaId: string) {
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!subareaId) return;
    setLoading(true);
    setError(null);
    fetch(`${API_BASE}/subareas/${subareaId}/aggregated-value`)
      .then(res => {
        if (!res.ok) {
          throw new Error(`HTTP ${res.status}: ${res.statusText}`);
        }
        return res.json();
      })
      .then(setData)
      .catch((err) => {
        console.error('Error fetching subarea aggregated value:', err);
        setError('Failed to fetch aggregated value');
      })
      .finally(() => setLoading(false));
  }, [subareaId]);

  return { data, loading, error };
}

export function useSubareaAggregatedByTime(subareaId: string) {
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!subareaId) return;
    setLoading(true);
    setError(null);
    fetch(`${API_BASE}/subareas/${subareaId}/aggregated-by-time`)
      .then(res => {
        if (!res.ok) {
          throw new Error(`HTTP ${res.status}: ${res.statusText}`);
        }
        return res.json();
      })
      .then(setData)
      .catch((err) => {
        console.error('Error fetching subarea aggregated by time:', err);
        setError('Failed to fetch time data');
      })
      .finally(() => setLoading(false));
  }, [subareaId]);

  return { data, loading, error };
}

export function useSubareaAggregatedByLocation(subareaId: string) {
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!subareaId) return;
    setLoading(true);
    setError(null);
    fetch(`${API_BASE}/subareas/${subareaId}/aggregated-by-location`)
      .then(res => {
        if (!res.ok) {
          throw new Error(`HTTP ${res.status}: ${res.statusText}`);
        }
        return res.json();
      })
      .then(setData)
      .catch((err) => {
        console.error('Error fetching subarea aggregated by location:', err);
        setError('Failed to fetch location data');
      })
      .finally(() => setLoading(false));
  }, [subareaId]);

  return { data, loading, error };
}

export function useSubareaAggregatedByDimension(subareaId: string, dimension: string) {
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!subareaId || !dimension) return;
    setLoading(true);
    setError(null);
    fetch(`${API_BASE}/subareas/${subareaId}/aggregated-by-${dimension}`)
      .then(res => {
        if (!res.ok) {
          throw new Error(`HTTP ${res.status}: ${res.statusText}`);
        }
        return res.json();
      })
      .then(setData)
      .catch((err) => {
        console.error(`Error fetching subarea aggregated by ${dimension}:`, err);
        setError(`Failed to fetch ${dimension} data`);
      })
      .finally(() => setLoading(false));
  }, [subareaId, dimension]);

  return { data, loading, error };
}

export function useIndicatorDimensionValues(indicatorId: string) {
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!indicatorId) return;
    setLoading(true);
    setError(null);
    fetch(`${API_BASE}/indicators/${indicatorId}/dimensions`)
      .then(res => {
        if (!res.ok) {
          throw new Error(`HTTP ${res.status}: ${res.statusText}`);
        }
        return res.json();
      })
      .then(setData)
      .catch((err) => {
        console.error('Error fetching indicator dimensions:', err);
        setError('Failed to fetch indicator dimensions');
      })
      .finally(() => setLoading(false));
  }, [indicatorId]);

  return { data, loading, error };
}

export function useMultipleIndicatorDimensionValues(indicatorIds: string[]) {
  const [data, setData] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!indicatorIds || indicatorIds.length === 0) {
      setData([]);
      return;
    }
    
    setLoading(true);
    setError(null);
    
    // Fetch dimension metadata for all indicators in parallel
    Promise.all(
      indicatorIds.map(id => 
        fetch(`${API_BASE}/indicators/${id}/dimensions`)
          .then(res => {
            if (!res.ok) {
              throw new Error(`HTTP ${res.status}: ${res.statusText}`);
            }
            return res.json();
          })
          .catch((err) => {
            console.error(`Error fetching indicator ${id} dimensions:`, err);
            return null; // Return null for failed requests
          })
      )
    )
      .then(results => {
        setData(results.filter(result => result !== null));
      })
      .catch((err) => {
        console.error('Error fetching multiple indicator dimensions:', err);
        setError('Failed to fetch indicator dimensions');
      })
      .finally(() => setLoading(false));
  }, [indicatorIds.join(',')]); // Use join to create a stable dependency

  return { data, loading, error };
} 