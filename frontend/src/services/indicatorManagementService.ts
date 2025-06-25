import { 
  ManagedIndicator, 
  IndicatorFormData, 
  IndicatorUpdateRequest, 
  BulkIndicatorUpdate,
  IndicatorValidationResult 
} from '@/types/indicators';

const API_BASE = process.env.NEXT_PUBLIC_API_URL;

export const indicatorManagementService = {
  // Get all indicators
  getIndicators: async (): Promise<ManagedIndicator[]> => {
    const response = await fetch(`${API_BASE}/indicators`);
    if (!response.ok) {
      throw new Error(`Failed to fetch indicators: ${response.statusText}`);
    }
    return response.json();
  },

  // Get single indicator
  getIndicator: async (id: string): Promise<ManagedIndicator> => {
    const response = await fetch(`${API_BASE}/indicators/${id}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch indicator: ${response.statusText}`);
    }
    return response.json();
  },

  // Create new indicator
  createIndicator: async (indicator: IndicatorFormData): Promise<ManagedIndicator> => {
    const response = await fetch(`${API_BASE}/indicators`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(indicator)
    });
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(`Failed to create indicator: ${errorData.message}`);
    }
    return response.json();
  },

  // Update indicator
  updateIndicator: async (id: string, updates: Partial<ManagedIndicator>): Promise<ManagedIndicator> => {
    const response = await fetch(`${API_BASE}/indicators/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(updates)
    });
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(`Failed to update indicator: ${errorData.message}`);
    }
    return response.json();
  },

  // Delete indicator
  deleteIndicator: async (id: string): Promise<void> => {
    const response = await fetch(`${API_BASE}/indicators/${id}`, { 
      method: 'DELETE' 
    });
    if (!response.ok) {
      throw new Error(`Failed to delete indicator: ${response.statusText}`);
    }
  },

  // Bulk update indicators
  bulkUpdateIndicators: async (bulkUpdate: BulkIndicatorUpdate): Promise<ManagedIndicator[]> => {
    const response = await fetch(`${API_BASE}/indicators/bulk-update`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(bulkUpdate)
    });
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(`Failed to bulk update indicators: ${errorData.message}`);
    }
    return response.json();
  },

  // Bulk delete indicators
  bulkDeleteIndicators: async (ids: string[]): Promise<void> => {
    const response = await fetch(`${API_BASE}/indicators/bulk-delete`, {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ids })
    });
    if (!response.ok) {
      throw new Error(`Failed to bulk delete indicators: ${response.statusText}`);
    }
  },

  // Validate indicator data
  validateIndicator: async (indicator: IndicatorFormData): Promise<IndicatorValidationResult> => {
    const response = await fetch(`${API_BASE}/indicators/validate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(indicator)
    });
    if (!response.ok) {
      throw new Error(`Failed to validate indicator: ${response.statusText}`);
    }
    return response.json();
  },

  // Get indicator statistics
  getIndicatorStats: async (): Promise<{
    total: number;
    byDirection: { input: number; output: number };
    bySubarea: { [subareaId: string]: number };
    byDataType: { [dataType: string]: number };
  }> => {
    const response = await fetch(`${API_BASE}/indicators/stats`);
    if (!response.ok) {
      throw new Error(`Failed to fetch indicator stats: ${response.statusText}`);
    }
    return response.json();
  },

  // Search indicators
  searchIndicators: async (query: string): Promise<ManagedIndicator[]> => {
    const response = await fetch(`${API_BASE}/indicators/search?q=${encodeURIComponent(query)}`);
    if (!response.ok) {
      throw new Error(`Failed to search indicators: ${response.statusText}`);
    }
    return response.json();
  },

  // Export indicators
  exportIndicators: async (format: 'csv' | 'json' = 'json'): Promise<Blob> => {
    const response = await fetch(`${API_BASE}/indicators/export?format=${format}`);
    if (!response.ok) {
      throw new Error(`Failed to export indicators: ${response.statusText}`);
    }
    return response.blob();
  }
};

export default indicatorManagementService; 