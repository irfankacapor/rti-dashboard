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

export function useIndicatorData(indicatorId: string, timeRange: string, dimension?: string) {
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!indicatorId) return;
    setLoading(true);
    setError(null);
    let url = `${API_BASE}/dashboard-data/historical/${indicatorId}?range=${timeRange}`;
    if (dimension) url += `&dimension=${dimension}`;
    fetch(url)
      .then(res => {
        if (!res.ok) {
          throw new Error(`HTTP ${res.status}: ${res.statusText}`);
        }
        return res.json();
      })
      .then(responseData => {
        // Transform the historical data response to match frontend expectations
        if (responseData.dataPoints && Array.isArray(responseData.dataPoints)) {
          const transformedData = {
            timeSeries: responseData.dataPoints.map((point: any) => ({
              label: point.timestamp,
              value: point.value
            })),
            dimensions: ['time'],
            startDate: responseData.startDate,
            endDate: responseData.endDate
          };
          setData(transformedData);
        } else {
          setData(responseData);
        }
      })
      .catch((err) => {
        console.error('Error fetching indicator data:', err);
        setError('Failed to fetch indicator data');
      })
      .finally(() => setLoading(false));
  }, [indicatorId, timeRange, dimension]);

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