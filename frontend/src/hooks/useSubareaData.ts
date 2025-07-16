import { useEffect } from 'react';
import { useSubareaCache } from '@/store/subareaCache';

const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

export function useSubareaData(subareaId: string) {
  const {
    cache,
    loading,
    errors,
    setSubareaData,
    setLoading,
    setError,
    getSubareaData,
    isCached
  } = useSubareaCache();

  useEffect(() => {
    if (!subareaId) return;

    // Check if data is already cached
    if (isCached(subareaId)) {
      return;
    }

    // Fetch subarea data
    setLoading(subareaId, true);
    setError(subareaId, null);

    fetch(`${API_BASE}/subareas/${subareaId}/data`)
      .then(res => {
        if (!res.ok) {
          throw new Error(`HTTP ${res.status}: ${res.statusText}`);
        }
        return res.json();
      })
      .then(data => {
        // Log the raw JSON response data
        console.log(`Subarea data response for subareaId ${subareaId}:`, JSON.stringify(data, null, 2));
        
        // Transform the data to match our cache structure
        const transformedData = {
          subarea: data.subarea,
          indicators: data.indicators || [],
          aggregatedData: data.aggregatedData || {},
          totalAggregatedValue: data.totalAggregatedValue || 0,
          dimensionMetadata: data.dimensionMetadata || {},
          timeSeriesData: data.timeSeriesData || [],
          indicatorTimeSeriesData: data.indicatorTimeSeriesData || {},
          indicatorDimensionData: data.indicatorDimensionData || {},
          errors: data.errors || {}
        };
        
        setSubareaData(subareaId, transformedData);
      })
      .catch(err => {
        console.error('Error fetching subarea data:', err);
        setError(subareaId, 'Failed to fetch subarea data');
      });
  }, [subareaId, setSubareaData, setLoading, setError, isCached]);

  const data = getSubareaData(subareaId);
  const isLoading = loading[subareaId] || false;
  const error = errors[subareaId] || null;

  return {
    data,
    loading: isLoading,
    error,
    // Convenience getters
    subarea: data?.subarea,
    indicators: data?.indicators || [],
    aggregatedData: data?.aggregatedData || {},
    totalAggregatedValue: data?.totalAggregatedValue || 0,
    dimensionMetadata: data?.dimensionMetadata || {},
    timeSeriesData: data?.timeSeriesData || [],
    indicatorTimeSeriesData: data?.indicatorTimeSeriesData || {},
    indicatorDimensionData: data?.indicatorDimensionData || {},
    errors: data?.errors || {}
  };
} 