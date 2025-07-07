import { 
  ProcessedIndicator,
  IndicatorBatchResponse
} from '@/types/csvProcessing';

const API_BASE = process.env.NEXT_PUBLIC_API_URL;

export const csvProcessingService = {
  // Submit processed indicators to the new simplified endpoint
  submitProcessedIndicators: async (indicators: any[]): Promise<IndicatorBatchResponse> => {
    // If the objects already have a 'values' field, assume they're CsvIndicatorData and send as-is
    const request = {
      indicators: indicators.map(indicator => {
        if ('values' in indicator) {
          return indicator;
        }
        // Fallback: map ProcessedIndicator to backend DTO
        return {
          name: indicator.name,
          description: indicator.description,
          unit: indicator.unit,
          source: indicator.source,
          subareaId: indicator.subareaId ? parseInt(indicator.subareaId) : null,
          direction: indicator.direction?.toUpperCase(),
          aggregationWeight: 1.0,
          values: indicator.dataPoints || []
        };
      })
    };

    console.log('Submitting indicators to backend:', request);
    console.log('API_BASE:', API_BASE);

    const response = await fetch(`${API_BASE}/indicators/create-from-csv`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request)
    });

    console.log('Response status:', response.status);
    console.log('Response headers:', response.headers);

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: response.statusText }));
      console.error('Backend error:', errorData);
      throw new Error(`Failed to create indicators: ${errorData.message}`);
    }

    const result = await response.json();
    console.log('Backend response:', result);
    return result;
  }
}; 