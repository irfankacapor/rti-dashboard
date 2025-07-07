import { 
  ManagedIndicator, 
  IndicatorFormData, 
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
    const data = await response.json();
    return data.map((item: any) => ({
      id: item.id,
      name: item.name,
      description: item.description,
      code: item.code,
      unit: item.unit?.code || '',
      source: item.source || '',
      dataType: item.dataType?.code || 'decimal',
      subareaId: item.subareaId?.toString() || '',
      subareaName: item.subareaName || '',
      direction: (item.direction || 'input').toLowerCase(),
      aggregationWeight: item.aggregationWeight || 1.0,
      valueCount: item.valueCount || 0,
      dimensions: item.dimensions || [],
      isFromCsv: false,
      isManual: false,
      isModified: false,
      createdAt: item.createdAt ? new Date(item.createdAt) : new Date(),
      lastModified: item.lastModified ? new Date(item.lastModified) : undefined,
    }));
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
    // Generate a code from the name
    const code = indicator.name.toLowerCase().replace(/[^a-z0-9]/g, '_').substring(0, 50);
    
    const backendRequest = {
      code: code,
      name: indicator.name,
      description: indicator.description,
      isComposite: false,
      unitId: null, // Would need to be mapped from unit string to ID
      dataTypeId: null, // Would need to be mapped from dataType string to ID
    };

    const response = await fetch(`${API_BASE}/indicators`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(backendRequest)
    });
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(`Failed to create indicator: ${errorData.message}`);
    }
    return response.json();
  },

  // Update indicator - only updates basic indicator fields
  updateIndicator: async (id: string, updates: Partial<ManagedIndicator>): Promise<ManagedIndicator> => {
    // Extract only the fields that the backend IndicatorUpdateRequest supports
    const backendUpdate = {
      name: updates.name,
      description: updates.description,
      isComposite: false, // Default value since frontend doesn't manage this
      unitId: null, // Would need to be mapped from unit string to ID
      dataTypeId: null, // Would need to be mapped from dataType string to ID
    };

    const response = await fetch(`${API_BASE}/indicators/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(backendUpdate)
    });
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(`Failed to update indicator: ${errorData.message}`);
    }
    return response.json();
  },

  // Assign indicator to subarea
  assignIndicatorToSubarea: async (indicatorId: string, subareaId: string, direction: string, aggregationWeight: number = 1.0): Promise<void> => {
    // Convert string IDs to numbers for the backend
    const numericIndicatorId = parseInt(indicatorId, 10);
    const numericSubareaId = parseInt(subareaId, 10);
    
    if (isNaN(numericIndicatorId) || isNaN(numericSubareaId)) {
      throw new Error('Invalid indicator or subarea ID');
    }
    
    const response = await fetch(`${API_BASE}/indicators/${numericIndicatorId}/subareas/${numericSubareaId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        direction: direction.toUpperCase(),
        aggregationWeight: aggregationWeight
      })
    });
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(`Failed to assign indicator to subarea: ${errorData.message}`);
    }
  },

  // Remove indicator from subarea
  removeIndicatorFromSubarea: async (indicatorId: string, subareaId: string): Promise<void> => {
    // Convert string IDs to numbers for the backend
    const numericIndicatorId = parseInt(indicatorId, 10);
    const numericSubareaId = parseInt(subareaId, 10);
    
    if (isNaN(numericIndicatorId) || isNaN(numericSubareaId)) {
      throw new Error('Invalid indicator or subarea ID');
    }
    
    const response = await fetch(`${API_BASE}/indicators/${numericIndicatorId}/subareas/${numericSubareaId}`, {
      method: 'DELETE'
    });
    if (!response.ok) {
      throw new Error(`Failed to remove indicator from subarea: ${response.statusText}`);
    }
  },

  // Update indicator with subarea relationship handling
  updateIndicatorWithRelationships: async (id: string, updates: Partial<ManagedIndicator>): Promise<ManagedIndicator> => {
    // First update the basic indicator fields
    await indicatorManagementService.updateIndicator(id, updates);
    
    // Then handle subarea relationship if it changed
    if (updates.subareaId !== undefined) {
      const currentIndicator = await indicatorManagementService.getIndicator(id);
      
      // If there was a previous subarea assignment, remove it
      if (currentIndicator.subareaId && currentIndicator.subareaId !== updates.subareaId) {
        await indicatorManagementService.removeIndicatorFromSubarea(id, currentIndicator.subareaId);
      }
      
      // If there's a new subarea assignment, create it
      if (updates.subareaId) {
        await indicatorManagementService.assignIndicatorToSubarea(
          id, 
          updates.subareaId, 
          updates.direction || 'input',
          updates.aggregationWeight || 1.0
        );
      }
    }
    
    // Return the updated indicator
    return indicatorManagementService.getIndicator(id);
  },

  // Delete indicator
  deleteIndicator: async (id: string): Promise<void> => {
    const response = await fetch(`${API_BASE}/indicators/${id}`, { 
      method: 'DELETE' 
    });
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(errorData.message || `Failed to delete indicator: ${response.statusText}`);
    }
  },

  // Delete indicator with all associated data
  deleteIndicatorWithData: async (id: string): Promise<void> => {
    const response = await fetch(`${API_BASE}/indicators/${id}/with-data`, { 
      method: 'DELETE' 
    });
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(errorData.message || `Failed to delete indicator with data: ${response.statusText}`);
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
      const errorData = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(errorData.message || `Failed to bulk delete indicators: ${response.statusText}`);
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
  },

  // Fetch allowed indicator types from backend
  getIndicatorTypes: async (): Promise<string[]> => {
    const response = await fetch(`${API_BASE}/indicator-types`);
    if (!response.ok) {
      throw new Error(`Failed to fetch indicator types: ${response.statusText}`);
    }
    return response.json();
  },
};

export default indicatorManagementService; 