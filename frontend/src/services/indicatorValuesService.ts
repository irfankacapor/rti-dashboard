import { IndicatorValuesResponse, IndicatorValueUpdate } from '@/types/indicatorValues';

const API_BASE = process.env.NEXT_PUBLIC_API_URL || '/api/v1';

export const indicatorValuesService = {
  getIndicatorValues: async (indicatorId: string): Promise<IndicatorValuesResponse> => {
    const response = await fetch(`${API_BASE}/indicators/${indicatorId}/values`);
    if (!response.ok) {
      throw new Error(`Failed to fetch indicator values: ${response.statusText}`);
    }
    return response.json();
  },

  updateIndicatorValues: async (indicatorId: string, updates: IndicatorValueUpdate[]): Promise<void> => {
    const response = await fetch(`${API_BASE}/indicators/${indicatorId}/values`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(updates),
    });
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(`Failed to update indicator values: ${errorData.message}`);
    }
  },
}; 